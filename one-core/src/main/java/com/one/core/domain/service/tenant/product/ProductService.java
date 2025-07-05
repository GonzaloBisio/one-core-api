package com.one.core.domain.service.tenant.product;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.application.dto.tenant.product.ProductFilterDTO;
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
import com.one.core.domain.model.tenant.product.ProductRecipe;
import com.one.core.domain.model.tenant.supplier.Supplier;
import com.one.core.domain.repository.tenant.product.ProductCategoryRepository;
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

    @Autowired
    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          ProductRecipeRepository productRecipeRepository,
                          ProductMapper productMapper,
                          ProductUtils productUtils) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.productMapper = productMapper;
        this.productUtils = productUtils;
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        if (StringUtils.hasText(productDTO.getSku()) && productRepository.existsBySku(productDTO.getSku().trim())) {
            throw new DuplicateFieldException("Product SKU", productDTO.getSku().trim());
        }

        Product product = productMapper.toEntityForCreation(productDTO);

        // --- LÓGICA DE ADAPTACIÓN INTELIGENTE ---
        // 1. Asignar tipo de producto por defecto si no se especifica
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

        // 2. Lógica condicional basada en el tipo de producto final
        if (product.getProductType() != ProductType.PHYSICAL_GOOD) {
            // Los servicios o suscripciones no tienen stock
            product.setCurrentStock(BigDecimal.ZERO);
            product.setMinimumStockLevel(BigDecimal.ZERO);
        } else {
            // Solo los bienes físicos necesitan SKU y código de barras por defecto
            if (!StringUtils.hasText(product.getSku())) {
                product.setSku(productUtils.generateSku());
            }
            if (!StringUtils.hasText(product.getBarcode())) {
                product.setBarcode(productUtils.generateBarcode());
            }
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
        productRepository.deleteById(id);
    }


    @Transactional
    public ProductRecipeDTO addRecipeItem(Long mainProductId, ProductRecipeDTO recipeItemDTO) {
        Product mainProduct = productRepository.findById(mainProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Main Product", "id", mainProductId));
        if (mainProduct.getProductType() != ProductType.COMPOUND) {
            throw new ValidationException("Recipes can only be added to products of type COMPOUND.");
        }

        Product ingredientProduct = productRepository.findById(recipeItemDTO.getIngredientProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient Product", "id", recipeItemDTO.getIngredientProductId()));
        if (ingredientProduct.getProductType() != ProductType.PHYSICAL_GOOD) {
            throw new ValidationException("Ingredients must be products of type PHYSICAL_GOOD.");
        }

        ProductRecipe recipeItem = new ProductRecipe();
        recipeItem.setMainProduct(mainProduct);
        recipeItem.setIngredientProduct(ingredientProduct);
        recipeItem.setQuantityRequired(recipeItemDTO.getQuantityRequired());

        ProductRecipe savedItem = productRecipeRepository.save(recipeItem);

        recipeItemDTO.setId(savedItem.getId());
        recipeItemDTO.setIngredientProductName(ingredientProduct.getName());
        recipeItemDTO.setIngredientProductSku(ingredientProduct.getSku());
        return recipeItemDTO;
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

}