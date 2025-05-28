package com.one.core.domain.service.tenant.product;

import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductCategory;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.product.ProductCategoryRepository;
import com.one.core.application.dto.tenant.product.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
// Importa excepciones personalizadas si las tienes
// import com.one.core.application.exception.ResourceNotFoundException;


@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository; // Para buscar la categoría

    // Mapeador (ej. MapStruct o manual)
    // private ProductMapper productMapper;


    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO) // Implementa este método de mapeo
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id)); // Usa ResourceNotFoundException
        return convertToDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        return convertToDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        // Mapear desde DTO a entidad
        product.setName(productDTO.getName());
        product.setSku(productDTO.getSku());
        product.setDescription(productDTO.getDescription());
        product.setSalePrice(productDTO.getSalePrice());
        product.setPurchasePrice(productDTO.getPurchasePrice());
        product.setCurrentStock(productDTO.getCurrentStock() != null ? productDTO.getCurrentStock() : BigDecimal.ZERO);
        product.setMinimumStockLevel(productDTO.getMinimumStockLevel() != null ? productDTO.getMinimumStockLevel() : BigDecimal.ZERO);
        product.setUnitOfMeasure(productDTO.getUnitOfMeasure());
        product.setActive(productDTO.isActive());
        product.setBarcode(productDTO.getBarcode());
        product.setImageUrl(productDTO.getImageUrl());


        if (productDTO.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) { // O un UpdateProductRequestDTO
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Actualizar campos de la entidad desde el DTO
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        // ... otros campos ...
        if (productDTO.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        // Considera la lógica de negocio: ¿se puede borrar si tiene stock o movimientos?
        // Por ahora, borrado simple.
        productRepository.deleteById(id);
    }

    // Método de utilidad para mapear Entidad a DTO (implementa según necesites)
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getDefaultSupplier() != null) {
            dto.setDefaultSupplierId(product.getDefaultSupplier().getId());
            dto.setDefaultSupplierName(product.getDefaultSupplier().getName());
        }
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setCurrentStock(product.getCurrentStock());
        dto.setMinimumStockLevel(product.getMinimumStockLevel());
        dto.setActive(product.isActive());
        dto.setBarcode(product.getBarcode());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }
}
