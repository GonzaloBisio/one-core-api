package com.one.core.domain.service.tenant.product;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.application.dto.tenant.product.ProductFilterDTO;
import com.one.core.application.dto.tenant.product.ProductPackagingDTO;
import com.one.core.application.dto.tenant.product.ProductRecipeDTO;
import com.one.core.application.exception.DuplicateFieldException;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.product.ProductMapper;
import com.one.core.config.multitenancy.TenantContext;
import com.one.core.domain.model.enums.IndustryType;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductCategory;
import com.one.core.domain.model.tenant.product.ProductPackaging;
import com.one.core.domain.model.tenant.product.ProductRecipe;
import com.one.core.domain.model.tenant.supplier.Supplier;
import com.one.core.domain.repository.tenant.product.ProductCategoryRepository;
import com.one.core.domain.repository.tenant.product.ProductPackagingRepository;
import com.one.core.domain.repository.tenant.product.ProductRecipeRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.supplier.SupplierRepository;
import com.one.core.domain.service.tenant.product.criteria.ProductSpecification;
import com.one.core.domain.service.tenant.util.ProductUtils;
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

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final ProductMapper productMapper;
    private final ProductUtils productUtils;
    private final ProductPackagingRepository productPackagingRepository;


    @Autowired
    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          ProductRecipeRepository productRecipeRepository,
                          ProductMapper productMapper,
                          ProductUtils productUtils,
                          ProductPackagingRepository productPackagingRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.productMapper = productMapper;
        this.productUtils = productUtils;
        this.productPackagingRepository = productPackagingRepository;
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
        } else if (product.getProductType() == ProductType.PHYSICAL_GOOD) {
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

        if (product.getProductType() != ProductType.PHYSICAL_GOOD) {
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

        Product savedProduct = productRepository.save(product);
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

        if (product.getProductType() != ProductType.PHYSICAL_GOOD) {
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
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) throw new ResourceNotFoundException("Product", "id", id);
        List<ProductRecipe> recipe = productRecipeRepository.findByMainProductId(id);
        productRecipeRepository.deleteAll(recipe);
        productRepository.deleteById(id);
    }

    @Transactional
    public List<ProductRecipeDTO> setOrUpdateRecipe(Long mainProductId, List<ProductRecipeDTO> recipeItemsDTO) {
        Product mainProduct = productRepository.findById(mainProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Main Product", "id", mainProductId));
        if (mainProduct.getProductType() != ProductType.COMPOUND) {
            throw new ValidationException("Recipes can only be set for products of type COMPOUND.");
        }

        List<ProductRecipe> oldRecipe = productRecipeRepository.findByMainProductId(mainProductId);
        if (!oldRecipe.isEmpty()) {
            productRecipeRepository.deleteAllInBatch(oldRecipe);
        }

        List<ProductRecipe> newRecipeItems = new ArrayList<>();
        for (ProductRecipeDTO itemDTO : recipeItemsDTO) {
            Product ingredientProduct = productRepository.findById(itemDTO.getIngredientProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient Product", "id", itemDTO.getIngredientProductId()));
            if (ingredientProduct.getProductType() != ProductType.PHYSICAL_GOOD) {
                throw new ValidationException("Ingredient '" + ingredientProduct.getName() + "' must be a PHYSICAL_GOOD.");
            }

            ProductRecipe recipeItem = new ProductRecipe();
            recipeItem.setMainProduct(mainProduct);
            recipeItem.setIngredientProduct(ingredientProduct);
            recipeItem.setQuantityRequired(itemDTO.getQuantityRequired());
            newRecipeItems.add(recipeItem);
        }

        productRecipeRepository.saveAllAndFlush(newRecipeItems);

        return getRecipeItems(mainProductId);
    }

    @Transactional(readOnly = true)
    public List<ProductRecipeDTO> getRecipeItems(Long mainProductId) {
        List<ProductRecipe> recipeItems = productRecipeRepository.findByMainProductId(mainProductId);
        return recipeItems.stream().map(item -> {
            ProductRecipeDTO dto = new ProductRecipeDTO();
            dto.setId(item.getId());
            dto.setIngredientProductId(item.getIngredientProduct().getId());
            dto.setIngredientProductName(item.getIngredientProduct().getName());
            dto.setIngredientProductSku(item.getIngredientProduct().getSku());
            dto.setQuantityRequired(item.getQuantityRequired());
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

    @Transactional
    public List<ProductPackagingDTO> setOrUpdatePackaging(Long mainProductId, List<ProductPackagingDTO> packagingItemsDTO) {
        Product mainProduct = productRepository.findById(mainProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Main Product", "id", mainProductId));

        List<ProductPackaging> oldPackaging = productPackagingRepository.findByMainProductId(mainProductId);
        if (!oldPackaging.isEmpty()) {
            productPackagingRepository.deleteAllInBatch(oldPackaging);
        }

        List<ProductPackaging> newPackagingItems = new ArrayList<>();
        for (ProductPackagingDTO itemDTO : packagingItemsDTO) {
            Product packagingProduct = productRepository.findById(itemDTO.getPackagingProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Packaging Product", "id", itemDTO.getPackagingProductId()));

            if (packagingProduct.getProductType() != ProductType.PHYSICAL_GOOD) {
                throw new ValidationException("Packaging items must be products of type PHYSICAL_GOOD.");
            }
            if (packagingProduct.getCategory() == null || !"Empaques".equalsIgnoreCase(packagingProduct.getCategory().getName())) {
                throw new ValidationException(
                        "El producto '" + packagingProduct.getName() + "' no puede ser usado como empaque porque no pertenece a la categoría 'Empaques'."
                );
            }

            ProductPackaging packagingItem = new ProductPackaging();
            packagingItem.setMainProduct(mainProduct);
            packagingItem.setPackagingProduct(packagingProduct);
            packagingItem.setQuantity(itemDTO.getQuantity());
            newPackagingItems.add(packagingItem);
        }

        productPackagingRepository.saveAll(newPackagingItems);
        return getPackagingForProduct(mainProductId);
    }

    @Transactional(readOnly = true)
    public List<ProductPackagingDTO> getPackagingForProduct(Long mainProductId) {
        return productPackagingRepository.findByMainProductId(mainProductId).stream()
                .map(item -> {
                    ProductPackagingDTO dto = new ProductPackagingDTO();
                    dto.setId(item.getId());
                    dto.setPackagingProductId(item.getPackagingProduct().getId());
                    dto.setPackagingProductName(item.getPackagingProduct().getName());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                }).collect(Collectors.toList());
    }

}