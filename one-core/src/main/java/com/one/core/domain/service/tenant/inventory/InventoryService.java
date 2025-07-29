package com.one.core.domain.service.tenant.inventory;

import com.one.core.application.dto.tenant.inventory.StockAdjustmentRequestDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementFilterDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.inventory.StockMovementMapper;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.admin.SystemUser; // Sigue siendo necesario para el tipo de campo
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.StockMovement;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.product.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper stockMovementMapper;

    @Autowired
    public InventoryService(ProductRepository productRepository,
                            StockMovementRepository stockMovementRepository,
                            StockMovementMapper stockMovementMapper) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementMapper = stockMovementMapper;
    }

    private void validateProductIsStockable(Product product) {
        ProductType type = product.getProductType();
        if (type == ProductType.SERVICE || type == ProductType.SUBSCRIPTION || type == ProductType.DIGITAL) {
            throw new ValidationException(
                    String.format("Stock operations cannot be performed on product '%s' because it is not a stockable type (type: %s).",
                            product.getName(), type)
            );
        }
    }
    @Transactional(propagation = Propagation.MANDATORY)
    protected StockMovement recordMovementAndUpdateProductStock(
            Long productId,
            MovementType movementType,
            BigDecimal quantityChanged,
            String referenceDocumentType,
            String referenceDocumentId,
            Long performingSystemUserId,
            String notes
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        validateProductIsStockable(product);

        BigDecimal currentStock = product.getCurrentStock();
        BigDecimal newStock = currentStock.add(quantityChanged);

        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Stock for product '{}' (ID: {}) is going negative. Current: {}, Change: {}, New: {}",
                    product.getName(), productId, currentStock, quantityChanged, newStock);
        }

        product.setCurrentStock(newStock);
        productRepository.save(product);

        SystemUser performingUserProxy = null;
        if (performingSystemUserId != null) {
            performingUserProxy = new SystemUser();
            performingUserProxy.setId(performingSystemUserId);
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setQuantityChanged(quantityChanged);
        movement.setStockAfterMovement(newStock);
        movement.setMovementDate(OffsetDateTime.now());
        movement.setReferenceDocumentType(referenceDocumentType);
        movement.setReferenceDocumentId(referenceDocumentId);
        movement.setUser(performingUserProxy); // Usamos el objeto proxy
        movement.setNotes(notes);

        return stockMovementRepository.save(movement);
    }



    @Transactional
    public StockMovementDTO processIncomingStock(Long productId, BigDecimal quantity, MovementType movementType,
                                                 String referenceType, String referenceId, Long performingSystemUserId, String notes) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Incoming stock quantity must be positive.");
        }
        StockMovement movement = recordMovementAndUpdateProductStock(
                productId, movementType, quantity, referenceType, referenceId, performingSystemUserId, notes);
        return stockMovementMapper.toDTO(movement);
    }

    @Transactional
    public StockMovementDTO processOutgoingStock(Long productId, BigDecimal quantity, MovementType movementType,
                                                 String referenceType, String referenceId, Long performingSystemUserId, String notes) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Outgoing stock quantity must be positive.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        validateProductIsStockable(product);

        if (product.getCurrentStock().compareTo(quantity) < 0) {
            throw new ValidationException(
                    String.format("Insufficient stock for product '%s' (ID: %d). Requested: %s, Available: %s",
                            product.getName(), productId, quantity, product.getCurrentStock())
            );
        }
        StockMovement movement = recordMovementAndUpdateProductStock(
                productId, movementType, quantity.negate(), referenceType, referenceId, performingSystemUserId, notes);
        return stockMovementMapper.toDTO(movement);
    }

    @Transactional
    public StockMovementDTO performManualStockAdjustment(StockAdjustmentRequestDTO adjustmentDTO, UserPrincipal currentUser) {
        MovementType type = adjustmentDTO.getAdjustmentType();
        if (type != MovementType.ADJUSTMENT_IN && type != MovementType.ADJUSTMENT_OUT) {
            throw new ValidationException("Invalid adjustment type. Must be ADJUSTMENT_IN or ADJUSTMENT_OUT.");
        }

        BigDecimal quantityChangedForStock = (type == MovementType.ADJUSTMENT_OUT)
                ? adjustmentDTO.getQuantityAdjusted().negate()
                : adjustmentDTO.getQuantityAdjusted();

        StockMovement movement = recordMovementAndUpdateProductStock(
                adjustmentDTO.getProductId(),
                type,
                quantityChangedForStock,
                "MANUAL_ADJUSTMENT",
                null,
                currentUser.getId(),
                adjustmentDTO.getReason()
        );
        return stockMovementMapper.toDTO(movement);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentStock(Long productId) {
        return productRepository.findById(productId)
                .map(Product::getCurrentStock)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long productId, BigDecimal quantityNeeded) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductType type = product.getProductType();
        if (type == ProductType.SERVICE || type == ProductType.SUBSCRIPTION || type == ProductType.DIGITAL) {
            return true;
        }

        if (quantityNeeded == null || quantityNeeded.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return product.getCurrentStock().compareTo(quantityNeeded) >= 0;
    }

    @Transactional(readOnly = true)
    public Page<StockMovementDTO> getStockMovements(StockMovementFilterDTO filterDTO, Pageable pageable) {
        Page<StockMovement> movementPage;
        if (filterDTO != null && filterDTO.getProductId() != null) {
            movementPage = stockMovementRepository.findByProductIdOrderByMovementDateDesc(filterDTO.getProductId(), pageable);
        } else {
            movementPage = stockMovementRepository.findAll(pageable);
        }

        List<StockMovementDTO> movementDTOs = movementPage.getContent().stream()
                .map(stockMovementMapper::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(movementDTOs, pageable, movementPage.getTotalElements());
    }

    /**
     * Registra únicamente el movimiento de stock inicial para un producto recién creado.
     * NO modifica el stock del producto, asume que ya fue establecido durante la creación.
     * @param product El producto ya guardado con su stock inicial.
     * @param performingSystemUserId El ID del usuario que realiza la acción.
     */
    @Transactional(propagation = Propagation.MANDATORY) // Se une a la transacción de createProduct
    public void recordInitialStockMovement(Product product, Long performingSystemUserId) {
        validateProductIsStockable(product);

        BigDecimal initialStock = product.getCurrentStock();

        // Si no hay stock inicial, no hay nada que registrar.
        if (initialStock == null || initialStock.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        SystemUser performingUserProxy = null;
        if (performingSystemUserId != null) {
            performingUserProxy = new SystemUser();
            performingUserProxy.setId(performingSystemUserId);
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(MovementType.INITIAL_STOCK); // Usamos el tipo que definimos antes
        movement.setQuantityChanged(initialStock); // La cantidad que cambió es el stock total
        movement.setStockAfterMovement(initialStock); // El stock después del movimiento es el mismo
        movement.setMovementDate(OffsetDateTime.now());
        movement.setReferenceDocumentType("PRODUCT_CREATION");
        movement.setReferenceDocumentId("ID:" + product.getId());
        movement.setUser(performingUserProxy);
        movement.setNotes("Carga de stock inicial por creación de producto.");

        stockMovementRepository.save(movement);
        logger.info("Initial stock movement recorded for product ID {} with quantity {}.", product.getId(), initialStock);
    }

    @Transactional
    public void transferToFrozenStock(Long productId, BigDecimal quantityToFreeze, UserPrincipal currentUser) {
        if (quantityToFreeze == null || quantityToFreeze.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Quantity to freeze must be positive.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getCurrentStock().compareTo(quantityToFreeze) < 0) {
            throw new ValidationException("Insufficient available stock to freeze. Available: " + product.getCurrentStock() + ", Requested: " + quantityToFreeze);
        }

        // 2. Realizar la transferencia
        product.setCurrentStock(product.getCurrentStock().subtract(quantityToFreeze));
        product.setFrozenStock(product.getFrozenStock().add(quantityToFreeze));

        productRepository.save(product);
    }

    @Transactional
    public void thawStock(Long productId, BigDecimal quantityToThaw, UserPrincipal currentUser) {
        if (quantityToThaw == null || quantityToThaw.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Cantidad a descongelar debe ser positiva.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getFrozenStock().compareTo(quantityToThaw) < 0) {
            throw new ValidationException("Stock insuficiente para descongelar. Freezado: " + product.getFrozenStock() + ", Solicitado: " + quantityToThaw);
        }

        product.setFrozenStock(product.getFrozenStock().subtract(quantityToThaw));
        product.setCurrentStock(product.getCurrentStock().add(quantityToThaw));
        productRepository.save(product);
    }
}