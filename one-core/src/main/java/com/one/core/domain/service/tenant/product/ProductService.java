package com.one.core.domain.service.tenant.product;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.application.dto.tenant.product.ProductFilterDTO;
import com.one.core.application.dto.tenant.product.ProductRecipeDTO;
import com.one.core.application.exception.DuplicateFieldException;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.product.ProductMapper;
import com.one.core.application.security.AuthenticationFacade;
import com.one.core.config.multitenancy.TenantContext;
import com.one.core.domain.model.enums.IndustryType;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.UnitOfMeasure;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductCategory;
import com.one.core.domain.model.tenant.product.ProductRecipe;
import com.one.core.domain.model.tenant.supplier.Supplier;
import com.one.core.domain.repository.tenant.product.ProductCategoryRepository;
import com.one.core.domain.repository.tenant.product.ProductRecipeRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.supplier.SupplierRepository;
import com.one.core.domain.service.tenant.inventory.InventoryService;
import com.one.core.domain.service.tenant.product.criteria.ProductSpecification;
import com.one.core.domain.service.tenant.util.ProductUtils;
import com.one.core.domain.service.common.UnitConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);


    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final ProductMapper productMapper;
    private final ProductUtils productUtils;
    private final AuthenticationFacade authenticationFacade;
    private final InventoryService inventoryService;
    private final UnitConversionService unitConversionService;



    @Autowired
    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          ProductRecipeRepository productRecipeRepository,
                          ProductMapper productMapper,
                          ProductUtils productUtils,
                          AuthenticationFacade authenticationFacade,
                          InventoryService inventoryService,
                          UnitConversionService unitConversionService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.productMapper = productMapper;
        this.productUtils = productUtils;
        this.authenticationFacade = authenticationFacade;
        this.inventoryService = inventoryService;
        this.unitConversionService = unitConversionService;
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = productMapper.toEntityForCreation(productDTO);

        if (product.getProductType() == null) {
            String industryType = TenantContext.getCurrentTenantIndustryType();
            if (IndustryType.GYM.name().equals(industryType)) {
                product.setProductType(ProductType.SUBSCRIPTION);
            } else if (IndustryType.SERVICES.name().equals(industryType)) {
                product.setProductType(ProductType.SERVICE);
            } else {
                product.setProductType(ProductType.PHYSICAL_GOOD);
            }
        }

        if (product.getProductType() == ProductType.COMPOUND) {
            product.setBarcode(null);
            if (StringUtils.hasText(product.getSku()) && productRepository.existsBySku(product.getSku().trim())) {
                throw new DuplicateFieldException("Product SKU", product.getSku().trim());
            }
        } else if (product.getProductType() == ProductType.PHYSICAL_GOOD || product.getProductType() == ProductType.PACKAGING) {
            if (StringUtils.hasText(product.getSku())) {
                if(productRepository.existsBySku(product.getSku().trim())) {
                    throw new DuplicateFieldException("Product SKU", product.getSku().trim());
                }
            } else {
                product.setSku(productUtils.generateSku());
            }
            if (!StringUtils.hasText(product.getBarcode())) {
                product.setBarcode(productUtils.generateBarcode());
            }
        }

        if (product.getProductType() == ProductType.SERVICE || product.getProductType() == ProductType.SUBSCRIPTION || product.getProductType() == ProductType.DIGITAL) {
            product.setCurrentStock(BigDecimal.ZERO);
            product.setMinimumStockLevel(BigDecimal.ZERO);
        }

        if (productDTO.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", productDTO.getCategoryId()));
            product.setCategory(category);
        }
        if (productDTO.getDefaultSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(productDTO.getDefaultSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", productDTO.getDefaultSupplierId()));
            product.setDefaultSupplier(supplier);
        }

        product.setCurrentStock(unitConversionService.toBaseUnit(product.getCurrentStock(), product.getUnitOfMeasure()));
        product.setMinimumStockLevel(unitConversionService.toBaseUnit(product.getMinimumStockLevel(), product.getUnitOfMeasure()));

        product.setActive(true);
        Product savedProduct = productRepository.save(product);

        try {
            Long userId = authenticationFacade.getCurrentAuthenticatedSystemUserId().orElse(null);
            inventoryService.recordInitialStockMovement(savedProduct, userId);
        } catch (Exception e) {
            logger.error("Failed to record initial stock movement for new product ID {}. Error: {}", savedProduct.getId(), e.getMessage());
            throw e;
        }

        return productMapper.toDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (StringUtils.hasText(productDTO.getSku()) && !productDTO.getSku().trim().equalsIgnoreCase(product.getSku())) {
            if (productRepository.existsBySku(productDTO.getSku().trim())) {
                throw new DuplicateFieldException("Product SKU", productDTO.getSku().trim());
            }
        }

        productMapper.updateEntityFromDTO(productDTO, product);

        if (productDTO.getCurrentStock() != null) {
            product.setCurrentStock(unitConversionService.toBaseUnit(product.getCurrentStock(), product.getUnitOfMeasure()));
        }
        if (productDTO.getMinimumStockLevel() != null) {
            product.setMinimumStockLevel(unitConversionService.toBaseUnit(product.getMinimumStockLevel(), product.getUnitOfMeasure()));
        }

        if (product.getProductType() == ProductType.PHYSICAL_GOOD || product.getProductType() == ProductType.PACKAGING) {
            if (!StringUtils.hasText(product.getSku())) {
                product.setSku(productUtils.generateSku());
            }
            if (!StringUtils.hasText(product.getBarcode())) {
                product.setBarcode(productUtils.generateBarcode());
            }
        }

        if (product.getProductType() == ProductType.SERVICE || product.getProductType() == ProductType.SUBSCRIPTION || product.getProductType() == ProductType.DIGITAL) {
            product.setCurrentStock(BigDecimal.ZERO);
            product.setMinimumStockLevel(BigDecimal.ZERO);
        }

        if (productDTO.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", productDTO.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
        if (productDTO.getDefaultSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(productDTO.getDefaultSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", productDTO.getDefaultSupplierId()));
            product.setDefaultSupplier(supplier);
        } else {
            product.setDefaultSupplier(null);
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDTO(updatedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllPackaging(boolean activeOnly) {
        List<Product> products = activeOnly
                ? productRepository.findAllByProductTypeAndIsActiveTrueOrderByNameAsc(ProductType.PACKAGING)
                : productRepository.findAllByProductTypeOrderByNameAsc(ProductType.PACKAGING);

        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllPhysicalGoods(boolean activeOnly) {
        List<Product> products = activeOnly
                ? productRepository.findAllByProductTypeAndIsActiveTrueOrderByNameAsc(ProductType.PHYSICAL_GOOD)
                : productRepository.findAllByProductTypeOrderByNameAsc(ProductType.PHYSICAL_GOOD);

        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllCompound(boolean activeOnly) {
        List<Product> products = activeOnly
                ? productRepository.findAllByProductTypeAndIsActiveTrueOrderByNameAsc(ProductType.COMPOUND)
                : productRepository.findAllByProductTypeOrderByNameAsc(ProductType.COMPOUND);

        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(ProductFilterDTO filterDTO, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filterBy(filterDTO);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(productDTOs, pageable, productPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "SKU", sku));
        return productMapper.toDTO(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setActive(false);

        productRepository.save(product);
    }

    @Transactional
    public List<ProductRecipeDTO> setOrUpdateRecipe(Long mainProductId, List<ProductRecipeDTO> itemsDTO) {
        Product mainProduct = productRepository.findById(mainProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Main Product", "id", mainProductId));
        if (mainProduct.getProductType() != ProductType.COMPOUND) {
            throw new ValidationException("Recipes can only be set for products of type COMPOUND.");
        }

        // Validación de sumatoria de % (si aplica)
        BigDecimal sumPct = itemsDTO.stream()
                .filter(i -> i.getUnitOfMeasure() == UnitOfMeasure.PERCENTAGE)
                .map(ProductRecipeDTO::getQuantityRequired)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumPct.compareTo(BigDecimal.ONE) > 0) {
            throw new ValidationException("La sumatoria de líneas en PERCENTAGE supera 100% (use valores 0..1, p.ej. 0.90 = 90%).");
        }

        // Borrado y armado
        List<ProductRecipe> oldRecipe = productRecipeRepository.findByMainProductId(mainProductId);
        if (!oldRecipe.isEmpty()) productRecipeRepository.deleteAllInBatch(oldRecipe);

        List<ProductRecipe> newRecipeItems = new ArrayList<>();
        for (ProductRecipeDTO dto : itemsDTO) {
            Product ingredient = productRepository.findById(dto.getIngredientProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient Product", "id", dto.getIngredientProductId()));
            ProductType t = ingredient.getProductType();
            if (t != ProductType.PHYSICAL_GOOD && t != ProductType.COMPOUND) {
                throw new ValidationException("Ingredient '" + ingredient.getName() + "' must be stockable (PHYSICAL_GOOD or COMPOUND).");
            }

            // Reglas de compatibilidad de unidad
            UnitOfMeasure uom = dto.getUnitOfMeasure();
            if (uom == UnitOfMeasure.PERCENTAGE) {
                // 0..1
                if (dto.getQuantityRequired().compareTo(BigDecimal.ZERO) <= 0
                        || dto.getQuantityRequired().compareTo(BigDecimal.ONE) > 0) {
                    throw new ValidationException("Para PERCENTAGE use valores entre 0 y 1 (p.ej., 0.90 = 90%).");
                }
            } else {
                // Si no es porcentaje, la magnitud debe coincidir con la del ingrediente
                if (uom.getMagnitude() != ingredient.getUnitOfMeasure().getMagnitude()) {
                    throw new ValidationException(
                            "La unidad de la línea (" + uom + ") no es compatible con el ingrediente (" + ingredient.getUnitOfMeasure() + ")."
                    );
                }
            }

            ProductRecipe r = new ProductRecipe();
            r.setMainProduct(mainProduct);
            r.setIngredientProduct(ingredient);
            r.setQuantityRequired(dto.getQuantityRequired());
            r.setUnitOfMeasure(uom); // importante
            newRecipeItems.add(r);
        }

        productRecipeRepository.saveAllAndFlush(newRecipeItems);
        return getRecipeItems(mainProductId);
    }

    @Transactional(readOnly = true)
    public List<ProductRecipeDTO> getRecipeItems(Long mainProductId) {
        return productRecipeRepository.findByMainProductId(mainProductId).stream()
                .map(item -> {
                    ProductRecipeDTO dto = new ProductRecipeDTO();
                    dto.setId(item.getId());
                    dto.setIngredientProductId(item.getIngredientProduct().getId());
                    dto.setIngredientProductName(item.getIngredientProduct().getName());
                    dto.setIngredientProductSku(item.getIngredientProduct().getSku());
                    dto.setQuantityRequired(item.getQuantityRequired());
                    dto.setUnitOfMeasure(item.getUnitOfMeasure()); // NUEVO
                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional
    public void removeRecipeItem(Long recipeItemId) {
        if (!productRecipeRepository.existsById(recipeItemId)) {
            throw new ResourceNotFoundException("Recipe Item", "id", recipeItemId);
        }
        productRecipeRepository.deleteById(recipeItemId);
    }

}