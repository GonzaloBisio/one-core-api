package com.one.core.domain.service.tenant.inventory;

import com.one.core.application.dto.tenant.inventory.StockAdjustmentRequestDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementFilterDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.inventory.StockMovementMapper;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.StockMovement;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.product.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper stockMovementMapper;

    @Value("${inventory.allow-negative:false}")
    private boolean allowNegativeInventory;

    @Autowired
    public InventoryService(ProductRepository productRepository,
                            StockMovementRepository stockMovementRepository,
                            StockMovementMapper stockMovementMapper) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementMapper = stockMovementMapper;
    }

    /** Helper: indica si el producto es stockeable (toca inventario). */
    private boolean isStockable(ProductType type) {
        return type != ProductType.SERVICE
                && type != ProductType.SUBSCRIPTION
                && type != ProductType.DIGITAL;
    }

    private void validateProductIsStockable(Product product) {
        if (!isStockable(product.getProductType())) {
            throw new ValidationException(
                    String.format("Stock operations cannot be performed on product '%s' because it is not a stockable type (type: %s).",
                            product.getName(), product.getProductType())
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
        movement.setUser(performingUserProxy);
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

        if (!allowNegativeInventory && product.getCurrentStock().compareTo(quantity) < 0) {
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

        // No stock check for non-stockables
        if (!isStockable(product.getProductType())) {
            return true;
        }

        if (quantityNeeded == null || quantityNeeded.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return product.getCurrentStock().compareTo(quantityNeeded) >= 0;
    }

    @Transactional(readOnly = true)
    public Page<StockMovementDTO> getStockMovements(StockMovementFilterDTO filterDTO, Pageable pageable) {

        var spec = com.one.core.domain.service.tenant.inventory.criteria
                .StockMovementSpecification.filterBy(filterDTO);

        Page<StockMovement> movementPage = stockMovementRepository.findAll(spec, pageable);

        var dtos = movementPage.getContent().stream()
                .map(stockMovementMapper::toDTO)
                .toList();

        return new PageImpl<>(dtos, pageable, movementPage.getTotalElements());
    }

    /**
     * Registra únicamente el movimiento de stock inicial para un producto recién creado.
     * NO modifica el stock del producto, asume que ya fue establecido durante la creación.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordInitialStockMovement(Product product, Long performingSystemUserId) {
        // Skip: productos no stockeables (SERVICE/SUBSCRIPTION/DIGITAL)
        if (!isStockable(product.getProductType())) {
            logger.debug("Skipping initial stock movement for non-stockable product id {}", product.getId());
            return;
        }

        BigDecimal initialStock = product.getCurrentStock();

        // Si no hay stock inicial, no se registra nada
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
        movement.setMovementType(MovementType.INITIAL_STOCK);
        movement.setQuantityChanged(initialStock);
        movement.setStockAfterMovement(initialStock);
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

        validateProductIsStockable(product); // añadido

        if (product.getCurrentStock().compareTo(quantityToFreeze) < 0) {
            throw new ValidationException("Insufficient available stock to freeze. Available: " + product.getCurrentStock() + ", Requested: " + quantityToFreeze);
        }

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

        validateProductIsStockable(product); // añadido

        if (product.getFrozenStock().compareTo(quantityToThaw) < 0) {
            throw new ValidationException("Stock insuficiente para descongelar. Freezado: " + product.getFrozenStock() + ", Solicitado: " + quantityToThaw);
        }

        product.setFrozenStock(product.getFrozenStock().subtract(quantityToThaw));
        product.setCurrentStock(product.getCurrentStock().add(quantityToThaw));
        productRepository.save(product);
    }
}
