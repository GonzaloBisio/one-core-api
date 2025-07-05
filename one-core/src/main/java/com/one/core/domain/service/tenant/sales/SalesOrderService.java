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
import com.one.core.domain.model.tenant.product.ProductRecipe;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import com.one.core.domain.repository.admin.SystemUserRepository;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.repository.tenant.product.ProductRecipeRepository;
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

    @Autowired
    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             SalesOrderMapper salesOrderMapper,
                             InventoryService inventoryService,
                             SystemUserRepository systemUserRepository,
                             ProductRecipeRepository productRecipeRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.inventoryService = inventoryService;
        this.productRecipeRepository = productRecipeRepository;
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

            // --- INICIO DE LA LÓGICA DE PRECIO INTELIGENTE ---
            if (itemDto.getUnitPrice() == null) {
                // Si no se especifica un precio, usamos el precio de lista del producto.
                itemDto.setUnitPrice(product.getSalePrice());

                // Validamos que el producto tenga un precio de lista válido.
                if (itemDto.getUnitPrice() == null || itemDto.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ValidationException(
                            "Product '" + product.getName() + "' does not have a default sale price. A price must be specified in the order."
                    );
                }
            }
            // --- FIN DE LA LÓGICA DE PRECIO INTELIGENTE ---

            if (product.getProductType() == ProductType.PHYSICAL_GOOD) {
                hasPhysicalGoods = true;
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

        // Primero, validamos la disponibilidad de stock para todos los componentes necesarios
        for (SalesOrderItem item : order.getItems()) {
            Product productSold = item.getProduct();
            if (productSold.getProductType() == ProductType.PHYSICAL_GOOD) {
                if (!inventoryService.isStockAvailable(productSold.getId(), item.getQuantity())) {
                    throw new ValidationException("Insufficient stock for product: " + productSold.getName());
                }
            } else if (productSold.getProductType() == ProductType.COMPOUND) {
                List<ProductRecipe> recipeItems = productRecipeRepository.findByMainProductId(productSold.getId());
                if (recipeItems.isEmpty()) {
                    throw new ValidationException("Product " + productSold.getName() + " is compound but has no recipe defined.");
                }
                for (ProductRecipe recipeItem : recipeItems) {
                    BigDecimal requiredQuantity = recipeItem.getQuantityRequired().multiply(item.getQuantity());
                    if (!inventoryService.isStockAvailable(recipeItem.getIngredientProduct().getId(), requiredQuantity)) {
                        throw new ValidationException("Insufficient stock for ingredient: " + recipeItem.getIngredientProduct().getName());
                    }
                }
            }
        }

        Long processingSystemUserId = currentUser.getId();

        for (SalesOrderItem item : order.getItems()) {
            Product productSold = item.getProduct();
            if (productSold.getProductType() == ProductType.PHYSICAL_GOOD) {
                inventoryService.processOutgoingStock(
                        productSold.getId(), item.getQuantity(), MovementType.SALE_CONFIRMED,
                        "SALES_ORDER", order.getId().toString(), processingSystemUserId, "Sale for order ID: " + order.getId()
                );
            } else if (productSold.getProductType() == ProductType.COMPOUND) {
                List<ProductRecipe> recipeItems = productRecipeRepository.findByMainProductId(productSold.getId());
                for (ProductRecipe recipeItem : recipeItems) {
                    BigDecimal requiredQuantity = recipeItem.getQuantityRequired().multiply(item.getQuantity());
                    inventoryService.processOutgoingStock(
                            recipeItem.getIngredientProduct().getId(), requiredQuantity, MovementType.COMPONENT_CONSUMPTION,
                            "SALES_ORDER", order.getId().toString(), processingSystemUserId, "Consumption for " + productSold.getName()
                    );
                }
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

    @Transactional
    public SalesOrderDTO shipOrder(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        // Solo se puede despachar una orden que se está preparando.
        if (order.getStatus() != SalesOrderStatus.PREPARING_ORDER) {
            throw new ValidationException("Order cannot be shipped from its current status: " + order.getStatus());
        }

        // Aquí podrías añadir lógica para generar una guía de envío, notificar al cliente, etc.
        logger.info("Shipping order ID: {}", salesOrderId);

        order.setStatus(SalesOrderStatus.SHIPPED);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(updatedOrder);
    }

    @Transactional
    public SalesOrderDTO deliverOrder(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        // Solo se puede entregar una orden que ha sido despachada.
        if (order.getStatus() != SalesOrderStatus.SHIPPED) {
            throw new ValidationException("Order cannot be delivered from its current status: " + order.getStatus());
        }

        logger.info("Delivering order ID: {}", salesOrderId);

        order.setStatus(SalesOrderStatus.DELIVERED);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(updatedOrder);
    }

    @Transactional
    public SalesOrderDTO cancelOrder(Long salesOrderId, UserPrincipal currentUser) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        // No se puede cancelar una orden ya despachada o entregada.
        if (order.getStatus() == SalesOrderStatus.SHIPPED || order.getStatus() == SalesOrderStatus.DELIVERED) {
            throw new ValidationException("Cannot cancel an order that has already been shipped or delivered.");
        }

        // Si la orden ya estaba cancelada, no hacemos nada.
        if (order.getStatus() == SalesOrderStatus.CANCELLED) {
            return salesOrderMapper.toDTO(order);
        }

        // --- LÓGICA CRÍTICA: DEVOLUCIÓN DE STOCK ---
        // Si la orden estaba en "PREPARING_ORDER", significa que el stock ya fue descontado.
        // Debemos devolverlo.
        if (order.getStatus() == SalesOrderStatus.PREPARING_ORDER) {
            logger.info("Order {} is being cancelled. Returning stock to inventory.", salesOrderId);

            for (SalesOrderItem item : order.getItems()) {
                Product productSold = item.getProduct();
                if (productSold.getProductType() == ProductType.PHYSICAL_GOOD) {
                    inventoryService.processIncomingStock( // Usamos el método de ENTRADA
                            productSold.getId(), item.getQuantity(), MovementType.SALE_CANCELLED,
                            "SALES_ORDER_CANCEL", order.getId().toString(), currentUser.getId(),
                            "Stock returned for cancelled order ID: " + order.getId()
                    );
                } else if (productSold.getProductType() == ProductType.COMPOUND) {
                    List<ProductRecipe> recipeItems = productRecipeRepository.findByMainProductId(productSold.getId());
                    for (ProductRecipe recipeItem : recipeItems) {
                        BigDecimal quantityToReturn = recipeItem.getQuantityRequired().multiply(item.getQuantity());
                        inventoryService.processIncomingStock( // Devolvemos el stock de cada INSUMO
                                recipeItem.getIngredientProduct().getId(), quantityToReturn, MovementType.SALE_CANCELLED,
                                "SALES_ORDER_CANCEL", order.getId().toString(), currentUser.getId(),
                                "Component stock returned for cancelled order ID: " + order.getId()
                        );
                    }
                }
            }
        }

        order.setStatus(SalesOrderStatus.CANCELLED);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(updatedOrder);
    }

}