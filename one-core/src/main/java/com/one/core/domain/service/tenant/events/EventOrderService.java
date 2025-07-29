package com.one.core.domain.service.tenant.events;

import com.one.core.application.dto.tenant.events.*;
import com.one.core.application.dto.tenant.production.ProductionOrderRequestDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.events.EventOrderMapper;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.events.EventOrderStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.events.EventOrder;
import com.one.core.domain.model.tenant.events.EventOrderItem;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.repository.admin.SystemUserRepository;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.repository.tenant.events.EventOrderRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.service.tenant.production.ProductionOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.one.core.application.dto.tenant.events.EventOrderFilterDTO;
import com.one.core.domain.service.tenant.events.criteria.EventOrderSpecification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;


@Service
public class EventOrderService {

    private final EventOrderRepository eventOrderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SystemUserRepository systemUserRepository;
    private final ProductionOrderService productionOrderService;
    private final EventOrderMapper eventOrderMapper;

    @Autowired
    public EventOrderService(EventOrderRepository eventOrderRepository, ProductRepository productRepository, CustomerRepository customerRepository, SystemUserRepository systemUserRepository, ProductionOrderService productionOrderService, EventOrderMapper eventOrderMapper) {
        this.eventOrderRepository = eventOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.systemUserRepository = systemUserRepository;
        this.productionOrderService = productionOrderService;
        this.eventOrderMapper = eventOrderMapper;
    }

    @Transactional
    public EventOrderDTO createEventOrder(EventOrderRequestDTO requestDTO, UserPrincipal currentUser) {
        EventOrder order = new EventOrder();
        order.setEventDate(requestDTO.getEventDate());
        order.setNotes(requestDTO.getNotes());
        order.setDeliveryAddress(requestDTO.getDeliveryAddress());
        order.setCreatedByUser(systemUserRepository.getReferenceById(currentUser.getId()));

        if (requestDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", requestDTO.getCustomerId()));
            order.setCustomer(customer);
        }

        for (EventOrderItemRequestDTO itemDto : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDto.getProductId()));

            EventOrderItem item = new EventOrderItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());

            BigDecimal unitPrice = itemDto.getUnitPrice();
            if (unitPrice == null) {
                unitPrice = product.getSalePrice();
            }
            if (unitPrice == null) {
                throw new ValidationException("Price for product '" + product.getName() + "' is not defined.");
            }
            item.setUnitPrice(unitPrice);

            item.calculateSubtotal();

            order.addItem(item);
        }

        order.recalculateTotals();

        EventOrder savedOrder = eventOrderRepository.save(order);
        return eventOrderMapper.toDTO(savedOrder);
    }
    @Transactional
    public EventOrderDTO confirmEventOrder(Long eventOrderId, UserPrincipal currentUser) {
        EventOrder eventOrder = eventOrderRepository.findById(eventOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("EventOrder", "id", eventOrderId));

        if (eventOrder.getStatus() != EventOrderStatus.PENDING) {
            throw new ValidationException("Only PENDING orders can be confirmed.");
        }

        eventOrder.setStatus(EventOrderStatus.IN_PRODUCTION);

        for (EventOrderItem item : eventOrder.getItems()) {
            if (item.getProduct().getProductType() == ProductType.COMPOUND) {
                ProductionOrderRequestDTO productionRequest = new ProductionOrderRequestDTO();
                productionRequest.setProductId(item.getProduct().getId());
                productionRequest.setQuantityProduced(item.getQuantity());
                productionRequest.setNotes("Production for event #" + eventOrder.getId() + " on " + eventOrder.getEventDate());

                productionOrderService.createProductionOrder(productionRequest, currentUser);
            }
        }

        EventOrder updatedOrder = eventOrderRepository.save(eventOrder);
        return eventOrderMapper.toDTO(updatedOrder);
    }

    @Transactional(readOnly = true)
    public Page<EventOrderDTO> getAllEventOrders(EventOrderFilterDTO filter, Pageable pageable) {
        Specification<EventOrder> spec = EventOrderSpecification.filterBy(filter);
        Page<EventOrder> eventPage = eventOrderRepository.findAll(spec, pageable);
        return eventPage.map(eventOrderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public EventOrderDTO getEventOrderById(Long eventOrderId) {
        EventOrder eventOrder = eventOrderRepository.findById(eventOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("EventOrder", "id", eventOrderId));
        return eventOrderMapper.toDTO(eventOrder);
    }

    @Transactional
    public void deleteEventOrder(Long eventOrderId) {
        if (!eventOrderRepository.existsById(eventOrderId)) {
            throw new ResourceNotFoundException("EventOrder", "id", eventOrderId);
        }
        eventOrderRepository.deleteById(eventOrderId);
    }

    @Transactional
    public EventOrderDTO markOrderAsReady(Long eventOrderId) {
        EventOrder eventOrder = eventOrderRepository.findById(eventOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("EventOrder", "id", eventOrderId));

        if (eventOrder.getStatus() != EventOrderStatus.IN_PRODUCTION) {
            throw new ValidationException("Only orders with status IN_PRODUCTION can be marked as ready. Current status: " + eventOrder.getStatus());
        }

        eventOrder.setStatus(EventOrderStatus.READY);
        EventOrder updatedOrder = eventOrderRepository.save(eventOrder);

        return eventOrderMapper.toDTO(updatedOrder);
    }
}