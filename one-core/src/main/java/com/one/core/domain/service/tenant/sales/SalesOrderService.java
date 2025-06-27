package com.one.core.domain.service.tenant.sales;

import com.one.core.application.dto.tenant.sales.SalesOrderDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderFilterDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderRequestDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderItemRequestDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.sales.SalesOrderMapper;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import com.one.core.domain.repository.admin.SystemUserRepository;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.sales.SalesOrderRepository;
import com.one.core.domain.service.tenant.inventory.InventoryService;
import com.one.core.domain.service.tenant.sales.criteria.SalesOrderSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesOrderService {
    private static final Logger logger = LoggerFactory.getLogger(SalesOrderService.class);

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final InventoryService inventoryService;
    private final SystemUserRepository systemUserRepository;

    @Autowired
    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             SalesOrderMapper salesOrderMapper,
                             InventoryService inventoryService,
                             SystemUserRepository systemUserRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.inventoryService = inventoryService;
        this.systemUserRepository = systemUserRepository;
    }

    @Transactional
    public SalesOrderDTO createSalesOrder(SalesOrderRequestDTO requestDTO, UserPrincipal currentUser) {
        SystemUser systemUser = systemUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", currentUser.getId()));

        SalesOrder order = new SalesOrder();
        order.setOrderDate(LocalDate.now());
        order.setStatus(SalesOrderStatus.PENDING_PAYMENT);
        order.setNotes(requestDTO.getNotes());
        order.setPaymentMethod(requestDTO.getPaymentMethod());
        order.setShippingAddress(requestDTO.getShippingAddress());
        order.setCreatedByUser(systemUser);

        if (requestDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", requestDTO.getCustomerId()));
            order.setCustomer(customer);
            if (order.getShippingAddress() == null && customer.getAddress() != null) {
                order.setShippingAddress(customer.getAddress());
            }
        }

        boolean hasPhysicalGoods = false;
        List<SalesOrderItem> items = new ArrayList<>();
        for (SalesOrderItemRequestDTO itemDto : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDto.getProductId()));

            if (product.getProductType() == ProductType.PHYSICAL_GOOD) {
                hasPhysicalGoods = true;
            }

            SalesOrderItem orderItem = salesOrderMapper.itemRequestDtoToEntity(itemDto, product);
            orderItem.setSalesOrder(order);
            items.add(orderItem);
        }
        order.setItems(items);
        order.recalculateTotals();

        // Si la orden no contiene bienes físicos, no necesita una dirección de envío.
        if (!hasPhysicalGoods) {
            order.setShippingAddress(null);
        }

        SalesOrder savedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(savedOrder);
    }

    @Transactional
    public SalesOrderDTO confirmAndProcessSalesOrder(Long salesOrderId, UserPrincipal currentUser) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        if (order.getStatus() != SalesOrderStatus.PENDING_PAYMENT) {
            throw new ValidationException("Sales order cannot be confirmed from its current status: " + order.getStatus());
        }

        // Primero, verificamos el stock de todos los productos físicos necesarios
        for (SalesOrderItem item : order.getItems()) {
            if (item.getProduct().getProductType() == ProductType.PHYSICAL_GOOD) {
                if (!inventoryService.isStockAvailable(item.getProduct().getId(), item.getQuantity())) {
                    throw new ValidationException(
                            String.format("Insufficient stock for product: %s (ID: %d). Requested: %s, Available: %s",
                                    item.getProduct().getName(), item.getProduct().getId(), item.getQuantity(), inventoryService.getCurrentStock(item.getProduct().getId()))
                    );
                }
            }
        }

        Long processingSystemUserId = currentUser.getId();

        // Si todas las validaciones pasaron, ahora sí procesamos el descuento de stock
        for (SalesOrderItem item : order.getItems()) {
            if (item.getProduct().getProductType() == ProductType.PHYSICAL_GOOD) {
                inventoryService.processOutgoingStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        MovementType.SALE_CONFIRMED,
                        "SALES_ORDER",
                        order.getId().toString(),
                        processingSystemUserId,
                        "Sale for order ID: " + order.getId() + " - Product: " + item.getProduct().getName()
                );
            }
        }

        order.setStatus(SalesOrderStatus.PREPARING_ORDER);
        SalesOrder updatedOrder = salesOrderRepository.save(order);

        return salesOrderMapper.toDTO(updatedOrder);
    }

    @Transactional(readOnly = true)
    public SalesOrderDTO getSalesOrderById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", id));
        return salesOrderMapper.toDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<SalesOrderDTO> getAllSalesOrders(SalesOrderFilterDTO filterDTO, Pageable pageable) {
        Specification<SalesOrder> spec = SalesOrderSpecification.filterBy(filterDTO);
        Page<SalesOrder> orderPage = salesOrderRepository.findAll(spec, pageable);

        List<SalesOrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(salesOrderMapper::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(orderDTOs, pageable, orderPage.getTotalElements());
    }
}