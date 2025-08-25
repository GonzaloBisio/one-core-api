package com.one.core.domain.service.tenant.purchases;

import com.one.core.application.dto.tenant.purchases.*;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.purchases.PurchaseOrderMapper;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.enums.purchases.PurchaseOrderStatus;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.purchases.PurchaseOrder;
import com.one.core.domain.model.tenant.purchases.PurchaseOrderItem;
import com.one.core.domain.model.tenant.supplier.Supplier;
import com.one.core.domain.repository.admin.SystemUserRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.purchases.PurchaseOrderItemRepository;
import com.one.core.domain.repository.tenant.purchases.PurchaseOrderRepository;
import com.one.core.domain.repository.tenant.supplier.SupplierRepository;
import com.one.core.domain.service.tenant.inventory.InventoryService;
import com.one.core.domain.service.tenant.purchases.criteria.PurchaseOrderSpecification;
import com.one.core.domain.service.common.UnitConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SystemUserRepository systemUserRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final InventoryService inventoryService;
    private final UnitConversionService unitConversionService;

    @Autowired
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderItemRepository purchaseOrderItemRepository,
                                ProductRepository productRepository,
                                SupplierRepository supplierRepository,
                                SystemUserRepository systemUserRepository,
                                PurchaseOrderMapper purchaseOrderMapper,
                                InventoryService inventoryService,
                                UnitConversionService unitConversionService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.systemUserRepository = systemUserRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.inventoryService = inventoryService;
        this.unitConversionService = unitConversionService;
    }

    @Transactional
    public PurchaseOrderDTO createPurchaseOrder(PurchaseOrderRequestDTO requestDTO, UserPrincipal currentUser) {
        SystemUser systemUser = systemUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", currentUser.getId()));

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderDate(LocalDate.now());
        // CAMBIO: El estado ahora es FULLY_RECEIVED por defecto, saltando DRAFT.
        order.setStatus(PurchaseOrderStatus.FULLY_RECEIVED);
        order.setExpectedDeliveryDate(requestDTO.getExpectedDeliveryDate());
        order.setNotes(requestDTO.getNotes());

        Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", requestDTO.getSupplierId()));
        order.setSupplier(supplier);
        order.setCreatedByUser(systemUser);

        List<PurchaseOrderItem> items = new ArrayList<>();
        for (PurchaseOrderItemRequestDTO itemDto : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDto.getProductId()));
            PurchaseOrderItem orderItem = purchaseOrderMapper.itemRequestDtoToEntity(itemDto, product);

            // LÓGICA AÑADIDA: Se asume que todo lo ordenado se recibe inmediatamente.
            orderItem.setQuantityReceived(orderItem.getQuantityOrdered());

            orderItem.setPurchaseOrder(order);
            items.add(orderItem);
        }
        order.setItems(items);
        order.recalculateTotals();

        // Guardamos la orden primero para obtener su ID.
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        logger.info("Purchase Order {} created with status FULLY_RECEIVED.", savedOrder.getId());

        // LÓGICA AÑADIDA: Procesar el ingreso de stock para cada ítem de la orden recién creada.
        String notesForMovement = "Stock received automatically upon creation of PO: " + savedOrder.getId();
        for (PurchaseOrderItem item : savedOrder.getItems()) {
            inventoryService.processIncomingStock(
                    item.getProduct().getId(),
                    unitConversionService.toBaseUnit(item.getQuantityReceived(), item.getProduct().getUnitOfMeasure()),
                    MovementType.PURCHASE_RECEIPT,
                    "PURCHASE_ORDER",
                    savedOrder.getId().toString(),
                    currentUser.getId(),
                    notesForMovement + " - Item: " + item.getProduct().getName()
            );
            logger.info("Stock updated for product ID {} with quantity {}.", item.getProduct().getId(), item.getQuantityReceived());
        }

        return purchaseOrderMapper.toDTO(savedOrder);
    }

    @Transactional
    public PurchaseOrderDTO receiveGoods(GoodsReceiptRequestDTO receiptDTO, UserPrincipal currentUser) {
        PurchaseOrder order = purchaseOrderRepository.findById(receiptDTO.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", receiptDTO.getPurchaseOrderId()));

        if (order.getStatus() == PurchaseOrderStatus.FULLY_RECEIVED || order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new ValidationException("Goods cannot be received for an order that is already " + order.getStatus());
        }

        String notesForMovement = "Goods received for PO: " + order.getId() +
                (receiptDTO.getNotes() != null ? " - " + receiptDTO.getNotes() : "");

        for (GoodsReceiptItemDTO itemReceivedDto : receiptDTO.getItemsReceived()) {
            PurchaseOrderItem orderItem = purchaseOrderItemRepository.findById(itemReceivedDto.getPurchaseOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderItem", "id", itemReceivedDto.getPurchaseOrderItemId()));

            if (!orderItem.getPurchaseOrder().getId().equals(order.getId())) {
                throw new ValidationException("Item " + orderItem.getId() + " does not belong to Purchase Order " + order.getId());
            }

            BigDecimal quantityToReceiveNow = itemReceivedDto.getQuantityReceivedNow();
            if (quantityToReceiveNow == null || quantityToReceiveNow.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal alreadyReceived = orderItem.getQuantityReceived();
            BigDecimal newTotalReceived = alreadyReceived.add(quantityToReceiveNow);

            if (newTotalReceived.compareTo(orderItem.getQuantityOrdered()) > 0) {
                throw new ValidationException(
                        String.format("Cannot receive more than ordered for product %s. PO Item ID: %d. Ordered: %s, Already Received: %s, Trying to Receive Now: %s",
                                orderItem.getProduct().getName(), orderItem.getId(), orderItem.getQuantityOrdered(), alreadyReceived, quantityToReceiveNow)
                );
            }

            orderItem.setQuantityReceived(newTotalReceived);

            inventoryService.processIncomingStock(
                    orderItem.getProduct().getId(),
                    unitConversionService.toBaseUnit(quantityToReceiveNow, orderItem.getProduct().getUnitOfMeasure()),
                    MovementType.PURCHASE_RECEIPT,
                    "PURCHASE_ORDER",
                    order.getId().toString(),
                    currentUser.getId(),
                    notesForMovement + " - Item: " + orderItem.getProduct().getName()
            );
        }

        boolean allOrderItemsNowFullyReceived = order.getItems().stream()
                .allMatch(item -> item.getQuantityReceived().compareTo(item.getQuantityOrdered()) >= 0);

        if (allOrderItemsNowFullyReceived) {
            order.setStatus(PurchaseOrderStatus.FULLY_RECEIVED);
        } else {
            order.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }

        PurchaseOrder updatedOrder = purchaseOrderRepository.save(order);
        return purchaseOrderMapper.toDTO(updatedOrder);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDTO getPurchaseOrderById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
        return purchaseOrderMapper.toDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderDTO> getAllPurchaseOrders(PurchaseOrderFilterDTO filterDTO, Pageable pageable) {
        Specification<PurchaseOrder> spec = PurchaseOrderSpecification.filterBy(filterDTO);
        Page<PurchaseOrder> orderPage = purchaseOrderRepository.findAll(spec, pageable);

        List<PurchaseOrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(purchaseOrderMapper::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(orderDTOs, pageable, orderPage.getTotalElements());
    }
}