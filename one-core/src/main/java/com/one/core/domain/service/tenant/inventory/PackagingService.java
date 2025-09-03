package com.one.core.domain.service.tenant.inventory;

import com.one.core.application.dto.tenant.product.PackagingConsumptionRequestDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductPackaging;
import com.one.core.domain.repository.tenant.product.ProductPackagingRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.service.common.UnitConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service that handles manual packaging consumption independent of sales orders.
 */
@Service
public class PackagingService {

    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final ProductPackagingRepository productPackagingRepository;
    private final UnitConversionService unitConversionService;

    @Autowired
    public PackagingService(InventoryService inventoryService,
                            ProductRepository productRepository,
                            ProductPackagingRepository productPackagingRepository,
                            UnitConversionService unitConversionService) {
        this.inventoryService = inventoryService;
        this.productRepository = productRepository;
        this.productPackagingRepository = productPackagingRepository;
        this.unitConversionService = unitConversionService;
    }

    /**
     * Consumes packaging stock, optionally validating that the packaging is
     * associated with a given product.
     */
    @Transactional
    public StockMovementDTO consumePackaging(PackagingConsumptionRequestDTO request,
                                            UserPrincipal currentUser) {
        Long packagingId = request.getPackagingProductId();
        BigDecimal quantity = request.getQuantity();
        Long productId = request.getProductId();

        Product packagingProduct = productRepository.findById(packagingId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", packagingId));

        if (packagingProduct.getProductType() != ProductType.PACKAGING) {
            throw new ValidationException("Provided product is not of type PACKAGING.");
        }

        if (productId != null) {
            List<ProductPackaging> packagingOptions = productPackagingRepository.findByMainProductId(productId);
            boolean valid = packagingOptions.stream()
                    .anyMatch(pp -> pp.getPackagingProduct().getId().equals(packagingId));
            if (!valid) {
                throw new ValidationException("Packaging not valid for the specified product.");
            }
        }

        BigDecimal quantityInBase = unitConversionService.toBaseUnit(quantity, packagingProduct.getUnitOfMeasure());

        return inventoryService.processOutgoingStock(
                packagingId,
                quantityInBase,
                MovementType.PACKAGING_CONSUMPTION,
                productId != null ? "PRODUCT" : "PACKAGING_MANUAL",
                productId != null ? productId.toString() : null,
                currentUser.getId(),
                productId != null ? "Packaging consumed for product ID: " + productId : "Packaging consumption");
    }
}
