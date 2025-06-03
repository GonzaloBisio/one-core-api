// src/main/java/com/one/core/domain/service/tenant/inventory/InventoryService.java
package com.one.core.domain.service.tenant.inventory;

import com.one.core.application.dto.tenant.inventory.StockAdjustmentRequestDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementFilterDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.inventory.StockMovementMapper;
import com.one.core.application.security.AuthenticationFacade; // IMPORTA TU FACADE
import com.one.core.domain.model.admin.SystemUser; // IMPORTA SystemUser
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.StockMovement;
import com.one.core.domain.repository.admin.SystemUserRepository; // Podría ser necesario si AuthenticationFacade solo da ID
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.product.StockMovementRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper stockMovementMapper;
    private final AuthenticationFacade authenticationFacade; // Inyecta el Facade
    private final SystemUserRepository systemUserRepository; // Inyecta para cargar SystemUser si es necesario

    @Autowired
    public InventoryService(ProductRepository productRepository,
                            StockMovementRepository stockMovementRepository,
                            StockMovementMapper stockMovementMapper,
                            AuthenticationFacade authenticationFacade,
                            SystemUserRepository systemUserRepository) { // Añade SystemUserRepository
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementMapper = stockMovementMapper;
        this.authenticationFacade = authenticationFacade;
        this.systemUserRepository = systemUserRepository; // Asigna
    }

    // --- MÉTODO INTERNO PRINCIPAL ---
    @Transactional(propagation = Propagation.MANDATORY)
    protected StockMovement recordMovementAndUpdateProductStock(
            Long productId,
            MovementType movementType,
            BigDecimal quantityChanged, // Positivo para entrada, negativo para salida
            String referenceDocumentType,
            String referenceDocumentId,
            Long performingSystemUserId, // Ahora es ID del SystemUser
            String notes
    ) {
        logger.debug("Recording stock movement. ProductId: {}, Type: {}, QuantityChange: {}, SystemUser ID: {}",
                productId, movementType, quantityChanged, performingSystemUserId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        BigDecimal currentStock = product.getCurrentStock();
        BigDecimal newStock = currentStock.add(quantityChanged);

        if (newStock.compareTo(BigDecimal.ZERO) < 0 &&
                movementType != MovementType.ADJUSTMENT_OUT &&
                movementType != MovementType.SALE_CONFIRMED &&
                movementType != MovementType.SUPPLIER_RETURN &&
                movementType != MovementType.WASTAGE) { // Permitir stock negativo solo para ciertos tipos de salida si es necesario, o nunca.
            // Esta validación es más robusta si se hace ANTES de llamar a este método para salidas.
        } else if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Stock for product '{}' (ID: {}) is going negative. Current: {}, Change: {}, New: {}",
                    product.getName(), productId, currentStock, quantityChanged, newStock);
            // No lanzar excepción aquí si se permiten negativos, la validación de "stock suficiente" ya debió ocurrir
        }


        product.setCurrentStock(newStock);
        productRepository.save(product);
        logger.info("Updated stock for ProductId: {}. Old: {}, Change: {}, New: {}",
                productId, currentStock, quantityChanged, newStock);

        SystemUser performingUser = null;
        if (performingSystemUserId != null) {
            // Usamos systemUserRepository para cargar el SystemUser
            performingUser = systemUserRepository.findById(performingSystemUserId).orElse(null);
            if (performingUser == null) {
                logger.warn("SystemUser with id {} not found for stock movement. Storing movement without associating user entity.", performingSystemUserId);
            }
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setQuantityChanged(quantityChanged);
        movement.setStockAfterMovement(newStock);
        movement.setMovementDate(LocalDateTime.now());
        movement.setReferenceDocumentType(referenceDocumentType);
        movement.setReferenceDocumentId(referenceDocumentId);
        movement.setUser(performingUser); // Asume que StockMovement.user es de tipo SystemUser
        movement.setNotes(notes);

        StockMovement savedMovement = stockMovementRepository.save(movement);
        logger.info("Recorded StockMovement Id: {} for ProductId: {}", savedMovement.getId(), productId);

        return savedMovement;
    }

    // --- MÉTODOS PÚBLICOS DEL SERVICIO ---

    /**
     * Procesa una entrada de stock genérica.
     * @param performingSystemUserId ID del SystemUser que realiza la operación. Si es null, se intenta obtener del contexto.
     */
    @Transactional
    public StockMovementDTO processIncomingStock(Long productId, BigDecimal quantity, MovementType movementType,
                                                 String referenceType, String referenceId, Long performingSystemUserId, String notes) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Incoming stock quantity must be positive.");
        }

        Long actualPerformingSystemUserId = (performingSystemUserId != null) ? performingSystemUserId
                : authenticationFacade.getCurrentAuthenticatedSystemUserId().orElse(null);

        if (actualPerformingSystemUserId == null) {
            logger.warn("No SystemUser ID provided or found in context for incoming stock. ProductId: {}. Movement will be recorded without user.", productId);
            // Considera si esto es un error o un comportamiento aceptable (ej. para procesos de sistema)
            // throw new ValidationException("User performing the stock movement could not be identified.");
        }

        StockMovement movement = recordMovementAndUpdateProductStock(
                productId, movementType, quantity, // quantity es positiva
                referenceType, referenceId, actualPerformingSystemUserId, notes);
        return stockMovementMapper.toDTO(movement);
    }

    /**
     * Procesa una salida de stock. Valida disponibilidad.
     * @param performingSystemUserId ID del SystemUser que realiza la operación. Si es null, se intenta obtener del contexto.
     */
    @Transactional
    public StockMovementDTO processOutgoingStock(Long productId, BigDecimal quantity, MovementType movementType,
                                                 String referenceType, String referenceId, Long performingSystemUserId, String notes) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Outgoing stock quantity must be positive.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getCurrentStock().compareTo(quantity) < 0) {
            throw new ValidationException(
                    String.format("Insufficient stock for product '%s' (ID: %d). Requested to remove: %s, Available: %s",
                            product.getName(), productId, quantity, product.getCurrentStock())
            );
        }

        Long actualPerformingSystemUserId = (performingSystemUserId != null) ? performingSystemUserId
                : authenticationFacade.getCurrentAuthenticatedSystemUserId().orElse(null);

        if (actualPerformingSystemUserId == null) {
            logger.warn("No SystemUser ID provided or found in context for outgoing stock. ProductId: {}. Movement will be recorded without user.", productId);
        }

        StockMovement movement = recordMovementAndUpdateProductStock(
                productId, movementType, quantity.negate(), // La cantidad se convierte a negativa
                referenceType, referenceId, actualPerformingSystemUserId, notes);
        return stockMovementMapper.toDTO(movement);
    }

    @Transactional
    public StockMovementDTO performManualStockAdjustment(StockAdjustmentRequestDTO adjustmentDTO) {
        logger.info("Performing manual stock adjustment for product ID: {}", adjustmentDTO.getProductId());

        if (adjustmentDTO.getQuantityAdjusted() == null ||
                adjustmentDTO.getQuantityAdjusted().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Adjusted quantity (magnitude) must be a positive value.");
        }

        MovementType type = adjustmentDTO.getAdjustmentType();
        if (type != MovementType.ADJUSTMENT_IN && type != MovementType.ADJUSTMENT_OUT) {
            throw new ValidationException("Invalid adjustment type for manual adjustment. Must be ADJUSTMENT_IN or ADJUSTMENT_OUT.");
        }

        BigDecimal quantityChangedForStock;
        if (type == MovementType.ADJUSTMENT_OUT) {
            quantityChangedForStock = adjustmentDTO.getQuantityAdjusted().negate();
        } else { // ADJUSTMENT_IN
            quantityChangedForStock = adjustmentDTO.getQuantityAdjusted();
        }

        Long performingSystemUserId = authenticationFacade.getCurrentAuthenticatedSystemUserId()
                .orElseThrow(() -> new ValidationException("Authenticated user ID is required for manual stock adjustment."));


        StockMovement movement = recordMovementAndUpdateProductStock(
                adjustmentDTO.getProductId(),
                type,
                quantityChangedForStock,
                "MANUAL_ADJUSTMENT",
                null,
                performingSystemUserId, // Pasar el ID del SystemUser
                adjustmentDTO.getReason()
        );
        return stockMovementMapper.toDTO(movement);
    }

    // --- Métodos de Consulta (sin cambios en su lógica principal) ---
    @Transactional(readOnly = true)
    public BigDecimal getCurrentStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return product.getCurrentStock();
    }

    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long productId, BigDecimal quantityNeeded) {
        if (quantityNeeded == null || quantityNeeded.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        BigDecimal currentStock = getCurrentStock(productId);
        return currentStock.compareTo(quantityNeeded) >= 0;
    }

    @Transactional(readOnly = true)
    public Page<StockMovementDTO> getStockMovements(StockMovementFilterDTO filterDTO, Pageable pageable) {
        // Specification<StockMovement> spec = StockMovementSpecification.filterBy(filterDTO);
        // Page<StockMovement> movementPage = stockMovementRepository.findAll(spec, pageable);

        Page<StockMovement> movementPage;
        // Aquí, si StockMovementFilterDTO tiene tenantUserId, y ahora es systemUserId:
        if (filterDTO != null && filterDTO.getProductId() != null) {
            // Si findByProductIdOrderByMovementDateDesc es tu método:
            movementPage = stockMovementRepository.findByProductIdOrderByMovementDateDesc(filterDTO.getProductId(), pageable);
        }
        // else if (filterDTO != null && filterDTO.getTenantUserId() != null) { // Ahora sería systemUserId
        // movementPage = stockMovementRepository.findByUserIdOrderByMovementDateDesc(filterDTO.getTenantUserId(), pageable);
        // }
        else {
            movementPage = stockMovementRepository.findAll(pageable);
        }

        List<StockMovementDTO> movementDTOs = movementPage.getContent().stream()
                .map(stockMovementMapper::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(movementDTOs, pageable, movementPage.getTotalElements());
    }
}