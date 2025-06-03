// src/main/java/com/one/core/domain/service/tenant/sales/SalesOrderService.java
package com.one.core.domain.service.tenant.sales;

import com.one.core.application.dto.tenant.sales.SalesOrderDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderFilterDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderRequestDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderItemRequestDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.sales.SalesOrderMapper;
import com.one.core.application.security.AuthenticationFacade; // IMPORTA TU FACADE
import com.one.core.domain.model.admin.SystemUser; // IMPORTA SystemUser
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.sales.SalesOrderRepository;
import com.one.core.domain.service.tenant.inventory.InventoryService;
import com.one.core.domain.service.tenant.sales.criteria.SalesOrderSpecification; // Si usas Specifications

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Si usas Specifications
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SalesOrderService {
    private static final Logger logger = LoggerFactory.getLogger(SalesOrderService.class);

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final InventoryService inventoryService;
    private final AuthenticationFacade authenticationFacade; // INYECTA EL FACADE
    // TenantUserRepository ya no es necesario aquí si el createdByUser en SalesOrder es SystemUser

    @Autowired
    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             // TenantUserRepository tenantUserRepository, // Eliminar si ya no se usa
                             SalesOrderMapper salesOrderMapper,
                             InventoryService inventoryService,
                             AuthenticationFacade authenticationFacade) { // Añade al constructor
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        // this.tenantUserRepository = tenantUserRepository; // Eliminar
        this.salesOrderMapper = salesOrderMapper;
        this.inventoryService = inventoryService;
        this.authenticationFacade = authenticationFacade; // Asigna el Facade
    }

    @Transactional
    public SalesOrderDTO createSalesOrder(SalesOrderRequestDTO requestDTO) {
        SalesOrder order = new SalesOrder();
        order.setOrderDate(LocalDate.now());
        order.setStatus(SalesOrderStatus.PENDING_PAYMENT);
        order.setNotes(requestDTO.getNotes());
        order.setPaymentMethod(requestDTO.getPaymentMethod());
        order.setShippingAddress(requestDTO.getShippingAddress());

        if (requestDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", requestDTO.getCustomerId()));
            order.setCustomer(customer);
            if (order.getShippingAddress() == null && customer.getAddress() != null) {
                order.setShippingAddress(customer.getAddress());
            }
        }

        // Obtener el SystemUser actual para createdByUser
        // Asumiendo que SalesOrder.createdByUser ahora es de tipo SystemUser
        Optional<SystemUser> currentUserOpt = authenticationFacade.getCurrentAuthenticatedSystemUser();
        currentUserOpt.ifPresent(order::setCreatedByUser);
        // Si no hay usuario (ej. proceso de sistema), createdByUser será null, lo cual es aceptable si el campo es nullable

        List<SalesOrderItem> items = new ArrayList<>();
        for (SalesOrderItemRequestDTO itemDto : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDto.getProductId()));

            SalesOrderItem orderItem = salesOrderMapper.itemRequestDtoToEntity(itemDto, product);
            orderItem.setSalesOrder(order);
            items.add(orderItem);
        }
        order.setItems(items);
        order.recalculateTotals();

        SalesOrder savedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(savedOrder);
    }

    @Transactional
    public SalesOrderDTO confirmAndProcessSalesOrder(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        // Validar estados desde los que se puede confirmar
        if (order.getStatus() != SalesOrderStatus.PENDING_PAYMENT && order.getStatus() != SalesOrderStatus.PENDING_PAYMENT) {
            throw new ValidationException("Sales order cannot be confirmed from its current status: " + order.getStatus());
        }

        for (SalesOrderItem item : order.getItems()) {
            if (!inventoryService.isStockAvailable(item.getProduct().getId(), item.getQuantity())) {
                throw new ValidationException(
                        String.format("Insufficient stock for product: %s (ID: %d). Requested: %s, Available: %s",
                                item.getProduct().getName(), item.getProduct().getId(), item.getQuantity(), inventoryService.getCurrentStock(item.getProduct().getId()))
                );
            }
        }

        // Obtener el ID del SystemUser que está realizando la confirmación.
        // Si la orden fue creada por un usuario y confirmada por otro, este ID podría ser diferente.
        // Por ahora, asumimos que el usuario actual en el contexto es quien procesa.
        Long processingSystemUserId = authenticationFacade.getCurrentAuthenticatedSystemUserId()
                .orElse(order.getCreatedByUser() != null ? order.getCreatedByUser().getId() : null); // Fallback al creador si no hay contexto, o manejar como error

        if (processingSystemUserId == null && order.getCreatedByUser() == null) {
            logger.warn("Cannot determine processing user for SalesOrder ID: {}. Stock movement will be recorded without user.", salesOrderId);
        }


        for (SalesOrderItem item : order.getItems()) {
            inventoryService.processOutgoingStock(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    MovementType.SALE_CONFIRMED, // Enum de MovementType
                    "SALES_ORDER",
                    order.getId().toString(),
                    // Pasa el ID del SystemUser que procesa la orden.
                    // Si createdByUser es SystemUser y quieres usar el creador original:
                    // order.getCreatedByUser() != null ? order.getCreatedByUser().getId() : null
                    processingSystemUserId,
                    "Sale for order ID: " + order.getId() + " - Product: " + item.getProduct().getName()
            );
        }

        order.setStatus(SalesOrderStatus.PREPARING_ORDER); // O SHIPPED, dependiendo del flujo de tu negocio
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

    // Aquí puedes añadir más métodos como:
    // - updateOrderStatus(Long orderId, SalesOrderStatus newStatus)
    // - cancelSalesOrder(Long orderId)
    // etc.
}