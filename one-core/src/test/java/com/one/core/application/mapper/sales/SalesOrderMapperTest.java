package com.one.core.application.mapper.sales;

import com.one.core.application.dto.tenant.sales.SalesOrderPackagingRequestDTO;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.sales.SalesOrderPackaging;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SalesOrderMapperTest {

    private final SalesOrderMapper mapper = new SalesOrderMapper();

    @Test
    void mapsPackagingToDTO() {
        Product packagingProduct = new Product();
        packagingProduct.setId(1L);
        packagingProduct.setName("Box");
        SalesOrderPackaging packaging = new SalesOrderPackaging();
        packaging.setId(10L);
        packaging.setProduct(packagingProduct);
        packaging.setQuantity(new BigDecimal("2"));

        var dto = mapper.toDTO(packaging);
        assertEquals(10L, dto.getId());
        assertEquals(1L, dto.getProductId());
        assertEquals("Box", dto.getProductName());
        assertEquals(new BigDecimal("2"), dto.getQuantity());
    }

    @Test
    void mapsRequestDtoToEntity() {
        SalesOrderPackagingRequestDTO dto = new SalesOrderPackagingRequestDTO();
        dto.setProductId(1L);
        dto.setQuantity(new BigDecimal("3"));

        Product product = new Product();
        product.setId(1L);
        product.setProductType(ProductType.PACKAGING);

        SalesOrderPackaging entity = mapper.toEntity(dto, product);
        assertEquals(product, entity.getProduct());
        assertEquals(new BigDecimal("3"), entity.getQuantity());
    }
}
