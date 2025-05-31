// src/main/java/com/one/core/domain/service/tenant/product/ProductService.java
package com.one.core.domain.service.tenant.product;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.application.dto.tenant.product.ProductFilterDTO;
import com.one.core.application.exception.DuplicateFieldException;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.product.ProductMapper;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductCategory;
import com.one.core.domain.model.tenant.supplier.Supplier;
import com.one.core.domain.repository.tenant.product.ProductCategoryRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.supplier.SupplierRepository;
import com.one.core.domain.service.tenant.product.criteria.ProductSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productMapper = productMapper;
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
        return productMapper.toDTO(product); // USA EL MAPPER
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "SKU", sku));
        return productMapper.toDTO(product); // USA EL MAPPER
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        if (StringUtils.hasText(productDTO.getSku())) {
            productRepository.findBySku(productDTO.getSku().trim()).ifPresent(p -> {
                throw new DuplicateFieldException("Product SKU", productDTO.getSku().trim());
            });
        }

        Product product = productMapper.toEntityForCreation(productDTO); // USA EL MAPPER

        // La carga y asignación de relaciones se mantiene en el servicio
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
        return productMapper.toDTO(savedProduct); // USA EL MAPPER
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (StringUtils.hasText(productDTO.getSku()) && !productDTO.getSku().trim().equalsIgnoreCase(product.getSku())) {
            productRepository.findBySku(productDTO.getSku().trim()).ifPresent(existingProduct -> {
                if (!existingProduct.getId().equals(product.getId())) {
                    throw new DuplicateFieldException("Product SKU", productDTO.getSku().trim());
                }
            });
        }

        productMapper.updateEntityFromDTO(productDTO, product); // USA EL MAPPER

        // La carga y asignación de relaciones se mantiene en el servicio
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
        return productMapper.toDTO(updatedProduct); // USA EL MAPPER
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }
}