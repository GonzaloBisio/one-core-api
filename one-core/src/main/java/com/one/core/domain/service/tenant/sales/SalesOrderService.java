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
import com.one.core.domain.model.tenant.product.ProductPackaging;
import com.one.core.domain.model.tenant.product.ProductRecipe;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import com.one.core.domain.repository.admin.SystemUserRepository;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.repository.tenant.product.ProductPackagingRepository;
import com.one.core.domain.repository.tenant.product.ProductRecipeRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.sales.SalesOrderRepository;
import com.one.core.domain.service.tenant.inventory.InventoryService;
import com.one.core.domain.service.tenant.sales.criteria.SalesOrderSpecification;
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
public class SalesOrderService {
    private static final Logger logger = LoggerFactory.getLogger(SalesOrderService.class);

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final InventoryService inventoryService;
    private final SystemUserRepository systemUserRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final ProductPackagingRepository productPackagingRepository;
    private final UnitConversionService unitConversionService;


    @Autowired
    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             SalesOrderMapper salesOrderMapper,
                             InventoryService inventoryService,
                             SystemUserRepository systemUserRepository,
                             ProductRecipeRepository productRecipeRepository,
                             ProductPackagingRepository productPackagingRepository,
                             UnitConversionService unitConversionService) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.inventoryService = inventoryService;
        this.productRecipeRepository = productRecipeRepository;
        this.systemUserRepository = systemUserRepository;
        this.productPackagingRepository = productPackagingRepository;
        this.unitConversionService = unitConversionService;
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

            if (itemDto.getUnitPrice() == null) {
                itemDto.setUnitPrice(product.getSalePrice());
                if (itemDto.getUnitPrice() == null || itemDto.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ValidationException(
                            "Product '" + product.getName() + "' does not have a default sale price. A price must be specified in the order."
                    );
                }
            }

            if (product.getProductType() == ProductType.PHYSICAL_GOOD || product.getProductType() == ProductType.COMPOUND) {
                hasPhysicalGoods = true; // Consideramos que los compuestos también pueden ser físicos en su entrega
            }

            SalesOrderItem orderItem = salesOrderMapper.itemRequestDtoToEntity(itemDto, product);
            orderItem.setSalesOrder(order);
            items.add(orderItem);
        }
        order.setItems(items);
        order.recalculateTotals();

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

        // 1. Validar disponibilidad de stock (productos y packaging)
        validateStockAvailability(order);

        // 2. Si hay stock, procesar todos los descuentos
        processStockDeductions(order, currentUser.getId());

        order.setStatus(SalesOrderStatus.PREPARING_ORDER);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(updatedOrder);
    }

    @Transactional
    public SalesOrderDTO cancelOrder(Long salesOrderId, UserPrincipal currentUser) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        if (order.getStatus() == SalesOrderStatus.SHIPPED || order.getStatus() == SalesOrderStatus.DELIVERED) {
            throw new ValidationException("Cannot cancel an order that has already been shipped or delivered.");
        }
        if (order.getStatus() == SalesOrderStatus.CANCELLED) {
            return salesOrderMapper.toDTO(order);
        }

        if (order.getStatus() == SalesOrderStatus.PREPARING_ORDER) {
            logger.info("Order {} is being cancelled. Returning stock to inventory.", salesOrderId);
            returnStockForOrder(order, currentUser.getId());
        }

        order.setStatus(SalesOrderStatus.CANCELLED);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(updatedOrder);
    }


    // --- MÉTODOS DE AYUDA (HELPERS) PARA MAYOR CLARIDAD ---

    private void validateStockAvailability(SalesOrder order) {
        for (SalesOrderItem item : order.getItems()) {
            Product productSold = item.getProduct();

            // --- LÓGICA SIMPLIFICADA ---
            // Ahora, tanto PHYSICAL_GOOD como COMPOUND son inventariables y se tratan igual.
            if (productSold.getProductType() == ProductType.PHYSICAL_GOOD || productSold.getProductType() == ProductType.COMPOUND) {
                BigDecimal qtyBase = unitConversionService.toBaseUnit(item.getQuantity(), productSold.getUnitOfMeasure());
                if (!inventoryService.isStockAvailable(productSold.getId(), qtyBase)) {
                    throw new ValidationException("Insufficient stock for product: " + productSold.getName());
                }
            }

            // La validación de packaging no cambia.
            List<ProductPackaging> packagingItems = productPackagingRepository.findByMainProductId(productSold.getId());
            for (ProductPackaging packaging : packagingItems) {
                BigDecimal requiredQuantity = packaging.getQuantity().multiply(item.getQuantity());
                BigDecimal requiredBase = unitConversionService.toBaseUnit(requiredQuantity, packaging.getPackagingProduct().getUnitOfMeasure());
                if (!inventoryService.isStockAvailable(packaging.getPackagingProduct().getId(), requiredBase)) {
                    throw new ValidationException("Insufficient stock for packaging: " + packaging.getPackagingProduct().getName());
                }
            }
        }
    }

    private void processStockDeductions(SalesOrder order, Long userId) {
        for (SalesOrderItem item : order.getItems()) {
            Product productSold = item.getProduct();

            // --- LÓGICA SIMPLIFICADA ---
            // Se descuenta el stock del producto vendido, sin importar si es simple o compuesto.
            if (productSold.getProductType() == ProductType.PHYSICAL_GOOD || productSold.getProductType() == ProductType.COMPOUND) {
                inventoryService.processOutgoingStock(
                        productSold.getId(), unitConversionService.toBaseUnit(item.getQuantity(), productSold.getUnitOfMeasure()), MovementType.SALE_CONFIRMED,
                        "SALES_ORDER", order.getId().toString(), userId, "Sale for order ID: " + order.getId()
                );
            }

            // La lógica de descuento de packaging no cambia.
            List<ProductPackaging> packagingItems = productPackagingRepository.findByMainProductId(productSold.getId());
            for (ProductPackaging packagingItem : packagingItems) {
                BigDecimal quantityToConsume = packagingItem.getQuantity().multiply(item.getQuantity());
                BigDecimal consumeBase = unitConversionService.toBaseUnit(quantityToConsume, packagingItem.getPackagingProduct().getUnitOfMeasure());
                inventoryService.processOutgoingStock(packagingItem.getPackagingProduct().getId(), consumeBase, MovementType.PACKAGING_CONSUMPTION, "SALES_ORDER", order.getId().toString(), userId, "Packaging for product: " + productSold.getName());
            }
        }
    }

    private void returnStockForOrder(SalesOrder order, Long userId) {
        for (SalesOrderItem item : order.getItems()) {
            Product productSold = item.getProduct();

            // --- LÓGICA SIMPLIFICADA ---
            // Se devuelve el stock del producto vendido, sea simple o compuesto.
            if (productSold.getProductType() == ProductType.PHYSICAL_GOOD || productSold.getProductType() == ProductType.COMPOUND) {
                inventoryService.processIncomingStock(
                        productSold.getId(), unitConversionService.toBaseUnit(item.getQuantity(), productSold.getUnitOfMeasure()), MovementType.SALE_CANCELLED,
                        "SALES_ORDER_CANCEL", order.getId().toString(), userId, "Stock returned for cancelled order ID: " + order.getId()
                );
            }

            // La lógica de devolución de packaging no cambia.
            List<ProductPackaging> packagingItems = productPackagingRepository.findByMainProductId(productSold.getId());
            for (ProductPackaging packagingItem : packagingItems) {
                BigDecimal quantityToReturn = packagingItem.getQuantity().multiply(item.getQuantity());
                BigDecimal returnBase = unitConversionService.toBaseUnit(quantityToReturn, packagingItem.getPackagingProduct().getUnitOfMeasure());
                inventoryService.processIncomingStock(packagingItem.getPackagingProduct().getId(), returnBase, MovementType.SALE_CANCELLED, "SALES_ORDER_CANCEL", order.getId().toString(), userId, "Packaging stock returned for product: " + productSold.getName());
            }
        }
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

    @Transactional
    public SalesOrderDTO shipOrder(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        if (order.getStatus() != SalesOrderStatus.PREPARING_ORDER) {
            throw new ValidationException("Order cannot be shipped from its current status: " + order.getStatus());
        }

        logger.info("Shipping order ID: {}", salesOrderId);

        order.setStatus(SalesOrderStatus.SHIPPED);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(updatedOrder);
    }

    @Transactional
    public SalesOrderDTO deliverOrder(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        if (order.getStatus() != SalesOrderStatus.SHIPPED) {
            throw new ValidationException("Order cannot be delivered from its current status: " + order.getStatus());
        }

        logger.info("Delivering order ID: {}", salesOrderId);

        order.setStatus(SalesOrderStatus.DELIVERED);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(updatedOrder);
    }
}