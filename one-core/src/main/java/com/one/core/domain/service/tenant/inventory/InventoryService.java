package com.one.core.domain.service.tenant.inventory;

import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.application.exception.InsufficientStockException;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.StockMovement;
import com.one.core.domain.model.tenant.TenantUser; // Asumiendo que tienes esta entidad
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.product.StockMovementRepository;
import com.one.core.domain.repository.tenant.TenantUserRepository; // Para buscar el TenantUser

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional; // IMPORTANTE: Spring Transactional

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final TenantUserRepository tenantUserRepository; // Para cargar el TenantUser

    @Autowired
    public InventoryService(ProductRepository productRepository,
                            StockMovementRepository stockMovementRepository,
                            TenantUserRepository tenantUserRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.tenantUserRepository = tenantUserRepository;
    }

    @Transactional
    public StockMovementDTO processPurchaseReceipt(Long productId, BigDecimal quantityReceived, String purchaseOrderId, Long performingTenantUserId) {
        if (quantityReceived == null || quantityReceived.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity received must be positive.");
        }

        StockMovement movement = recordMovementAndUpdateProductStock(
                productId,
                MovementType.PURCHASE_RECEIPT,
                quantityReceived,
                "PURCHASE_ORDER", // referenceDocumentType
                purchaseOrderId,    // referenceDocumentId
                performingTenantUserId,
                "Receipt from PO: " + purchaseOrderId
        );

        // Devolver un DTO (necesitarías un StockMovementMapper)
        // return stockMovementMapper.toDTO(movement);
        return new StockMovementDTO(); // Placeholder DTO
    }

    @Transactional
    public StockMovementDTO processSaleConfirmed(Long productId, BigDecimal quantitySold, String salesOrderId, Long performingTenantUserId) {
        if (quantitySold == null || quantitySold.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity sold must be positive.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getCurrentStock().compareTo(quantitySold) < 0) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %d. Requested: %s, Available: %s",
                            productId, quantitySold, product.getCurrentStock())
            );
        }

        // quantityChanged es negativo para salidas
        StockMovement movement = recordMovementAndUpdateProductStock(
                productId,
                MovementType.SALE_CONFIRMED,
                quantitySold.negate(), // Hacer la cantidad negativa
                "SALES_ORDER",     // referenceDocumentType
                salesOrderId,        // referenceDocumentId
                performingTenantUserId,
                "Sale from SO: " + salesOrderId
        );

        // Devolver un DTO
        // return stockMovementMapper.toDTO(movement);
        return new StockMovementDTO(); // Placeholder DTO
    }

    @Transactional(propagation = Propagation.MANDATORY) // Asegura que se llama desde un método transaccional
    protected StockMovement recordMovementAndUpdateProductStock(
            Long productId,
            MovementType movementType,
            BigDecimal quantityChanged,
            String referenceDocumentType,
            String referenceDocumentId,
            Long tenantUserId, // ID del usuario del tenant
            String notes
    ) {
        logger.debug("Attempting to record stock movement. ProductId: {}, Type: {}, QuantityChange: {}",
                productId, movementType, quantityChanged);

        // 1. Obtener el Producto (y bloquearlo si es necesario, aunque @Version ayuda)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // 2. Calcular el nuevo stock
        BigDecimal currentStock = product.getCurrentStock();
        BigDecimal newStock = currentStock.add(quantityChanged);

        // Validación opcional (ej. stock no puede ser negativo, a menos que lo permitas)
        // if (newStock.compareTo(BigDecimal.ZERO) < 0) {
        //     throw new InsufficientStockException("Stock cannot go negative for product: " + productId);
        // }

        // 3. Actualizar el stock del producto
        product.setCurrentStock(newStock);
        // La entidad Product tiene @PreUpdate que actualiza 'updatedAt'.
        // Si tienes @Version, Hibernate lo manejará.
        productRepository.save(product); // Guardar el producto actualizado
        logger.info("Updated stock for ProductId: {}. Old: {}, Change: {}, New: {}",
                productId, currentStock, quantityChanged, newStock);


        // 4. Cargar el TenantUser si se proveyó el ID
        TenantUser performingUser = null;
        if (tenantUserId != null) {
            performingUser = tenantUserRepository.findById(tenantUserId)
                    .orElse(null); // O lanzar excepción si el usuario DEBE existir
            if (performingUser == null) {
                logger.warn("TenantUser with id {} not found for stock movement. Storing movement without user.", tenantUserId);
            }
        }

        // 5. Crear y guardar la entidad StockMovement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setQuantityChanged(quantityChanged);
        movement.setStockAfterMovement(newStock); // El stock DESPUÉS de este movimiento
        movement.setMovementDate(LocalDateTime.now()); // O usa @PrePersist en StockMovement
        movement.setReferenceDocumentType(referenceDocumentType);
        movement.setReferenceDocumentId(referenceDocumentId);
        movement.setUser(performingUser); // Asocia el TenantUser cargado
        movement.setNotes(notes);

        StockMovement savedMovement = stockMovementRepository.save(movement);
        logger.info("Recorded StockMovement Id: {} for ProductId: {}", savedMovement.getId(), productId);

        return savedMovement;
    }




}
