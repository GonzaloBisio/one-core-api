package com.one.core.domain.service.tenant.sales;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.application.dto.tenant.sales.*;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.sales.SalesOrderMapper;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.UnitOfMeasure;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductPackaging;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import com.one.core.domain.repository.admin.SystemUserRepository;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.repository.tenant.product.ProductPackagingRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.sales.SalesOrderRepository;
import com.one.core.domain.service.common.UnitConversionService;
import com.one.core.domain.service.tenant.inventory.InventoryService;
import com.one.core.domain.service.tenant.product.ProductService;
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
    private final ProductPackagingRepository productPackagingRepository;
    private final UnitConversionService unitConversionService;
    private final ProductService productService;

    @Autowired
    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             SalesOrderMapper salesOrderMapper,
                             InventoryService inventoryService,
                             SystemUserRepository systemUserRepository,
                             ProductPackagingRepository productPackagingRepository,
                             UnitConversionService unitConversionService,
                             ProductService productService) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.inventoryService = inventoryService;
        this.systemUserRepository = systemUserRepository;
        this.productPackagingRepository = productPackagingRepository;
        this.unitConversionService = unitConversionService;
        this.productService = productService;
    }

    // =========================
    // Creación estándar
    // =========================
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
                    throw new ValidationException("Product '" + product.getName()
                            + "' does not have a default sale price. A price must be specified in the order.");
                }
            }

            if (product.getProductType() == ProductType.PHYSICAL_GOOD
                    || product.getProductType() == ProductType.COMPOUND) {
                hasPhysicalGoods = true;
            }

            SalesOrderItem orderItem = salesOrderMapper.itemRequestDtoToEntity(itemDto, product);
            // asegurar mapeo del flag por si el mapper no lo setea:
            orderItem.setSkipAutoPackaging(Boolean.TRUE.equals(itemDto.getSkipAutoPackaging()));
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

    // =========================
    // Confirmación / Procesamiento
    // =========================
    @Transactional
    public SalesOrderDTO confirmAndProcessSalesOrder(Long salesOrderId, UserPrincipal currentUser) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        if (order.getStatus() != SalesOrderStatus.PENDING_PAYMENT) {
            throw new ValidationException("Sales order cannot be confirmed from its current status: " + order.getStatus());
        }

        validateStockAvailability(order);
        processStockDeductions(order, currentUser.getId());

        order.setStatus(SalesOrderStatus.PREPARING_ORDER);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(updatedOrder);
    }

    // =========================
    // Cancelación
    // =========================
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

    // =========================
    // Helpers de stock (FINAL)
    // =========================
    private void validateStockAvailability(SalesOrder order) {
        for (SalesOrderItem item : order.getItems()) {
            Product productSold = item.getProduct();

            // 1) Validar stock del producto de la línea (incluye PACKAGING si se vende como ítem)
            if (productSold.getProductType() == ProductType.PHYSICAL_GOOD
                    || productSold.getProductType() == ProductType.COMPOUND
                    || productSold.getProductType() == ProductType.PACKAGING) {

                BigDecimal qtyBase = unitConversionService.toBaseUnit(
                        item.getQuantity(), productSold.getUnitOfMeasure());

                if (!inventoryService.isStockAvailable(productSold.getId(), qtyBase)) {
                    throw new ValidationException("Insufficient stock for product: " + productSold.getName());
                }
            }

            // 2) Validar packaging automático SOLO si no se saltea
            if (!item.isSkipAutoPackaging()) {
                List<ProductPackaging> packagingItems =
                        productPackagingRepository.findByMainProductId(productSold.getId());

                for (ProductPackaging packaging : packagingItems) {
                    BigDecimal requiredQuantity = packaging.getQuantity().multiply(item.getQuantity());
                    BigDecimal requiredBase = unitConversionService.toBaseUnit(
                            requiredQuantity, packaging.getPackagingProduct().getUnitOfMeasure());

                    if (!inventoryService.isStockAvailable(packaging.getPackagingProduct().getId(), requiredBase)) {
                        throw new ValidationException("Insufficient stock for packaging: "
                                + packaging.getPackagingProduct().getName());
                    }
                }
            }
        }
    }

    private void processStockDeductions(SalesOrder order, Long userId) {
        for (SalesOrderItem item : order.getItems()) {
            Product productSold = item.getProduct();

            // 1) Descontar producto de la línea (incluye PACKAGING si se vende como ítem)
            if (productSold.getProductType() == ProductType.PHYSICAL_GOOD
                    || productSold.getProductType() == ProductType.COMPOUND
                    || productSold.getProductType() == ProductType.PACKAGING) {

                inventoryService.processOutgoingStock(
                        productSold.getId(),
                        unitConversionService.toBaseUnit(item.getQuantity(), productSold.getUnitOfMeasure()),
                        MovementType.SALE_CONFIRMED,
                        "SALES_ORDER",
                        order.getId().toString(),
                        userId,
                        "Sale for order ID: " + order.getId()
                );
            }

            // 2) Descontar packaging automático SOLO si no se saltea
            if (!item.isSkipAutoPackaging()) {
                List<ProductPackaging> packagingItems =
                        productPackagingRepository.findByMainProductId(productSold.getId());

                for (ProductPackaging packagingItem : packagingItems) {
                    BigDecimal quantityToConsume =
                            packagingItem.getQuantity().multiply(item.getQuantity());
                    BigDecimal consumeBase = unitConversionService.toBaseUnit(
                            quantityToConsume, packagingItem.getPackagingProduct().getUnitOfMeasure());

                    inventoryService.processOutgoingStock(
                            packagingItem.getPackagingProduct().getId(),
                            consumeBase,
                            MovementType.PACKAGING_CONSUMPTION,
                            "SALES_ORDER",
                            order.getId().toString(),
                            userId,
                            "Packaging for product: " + productSold.getName()
                    );
                }
            }
        }
    }

    private void returnStockForOrder(SalesOrder order, Long userId) {
        for (SalesOrderItem item : order.getItems()) {
            Product productSold = item.getProduct();

            // 1) Devolver producto de la línea (incluye PACKAGING si se vendió como ítem)
            if (productSold.getProductType() == ProductType.PHYSICAL_GOOD
                    || productSold.getProductType() == ProductType.COMPOUND
                    || productSold.getProductType() == ProductType.PACKAGING) {

                inventoryService.processIncomingStock(
                        productSold.getId(),
                        unitConversionService.toBaseUnit(item.getQuantity(), productSold.getUnitOfMeasure()),
                        MovementType.SALE_CANCELLED,
                        "SALES_ORDER_CANCEL",
                        order.getId().toString(),
                        userId,
                        "Stock returned for cancelled order ID: " + order.getId()
                );
            }

            // 2) Devolver packaging automático SOLO si no se había salteado
            if (!item.isSkipAutoPackaging()) {
                List<ProductPackaging> packagingItems =
                        productPackagingRepository.findByMainProductId(productSold.getId());

                for (ProductPackaging packagingItem : packagingItems) {
                    BigDecimal quantityToReturn =
                            packagingItem.getQuantity().multiply(item.getQuantity());
                    BigDecimal returnBase = unitConversionService.toBaseUnit(
                            quantityToReturn, packagingItem.getPackagingProduct().getUnitOfMeasure());

                    inventoryService.processIncomingStock(
                            packagingItem.getPackagingProduct().getId(),
                            returnBase,
                            MovementType.SALE_CANCELLED,
                            "SALES_ORDER_CANCEL",
                            order.getId().toString(),
                            userId,
                            "Packaging stock returned for product: " + productSold.getName()
                    );
                }
            }
        }
    }

    // =========================
    // Queries
    // =========================
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

    // =========================
    // Quick Sale (opcional)
    // =========================
    @Transactional
    public SalesOrderDTO createQuickSale(QuickSaleRequestDTO req, UserPrincipal currentUser) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ValidationException("Debe enviar al menos un ítem.");
        }

        SalesOrder order = new SalesOrder();
        order.setCustomer(req.getCustomerId() != null
                ? customerRepository.findById(req.getCustomerId()).orElse(null)
                : null);
        order.setOrderDate(LocalDate.now());
        order.setStatus(SalesOrderStatus.PENDING_PAYMENT);
        order.setCreatedByUser(systemUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("SystemUser","id", currentUser.getId())));

        BigDecimal total = BigDecimal.ZERO;
        List<SalesOrderItem> items = new ArrayList<>();

        for (QuickSaleItemRequestDTO it : req.getItems()) {
            Product product;

            if (it.getQuickProduct() != null) {
                QuickProductDTO qp = it.getQuickProduct();

                ProductDTO p = new ProductDTO();
                p.setName(qp.getName());
                p.setProductType(ProductType.SERVICE); // sin stock
                p.setUnitOfMeasure(qp.getUnitOfMeasure() != null ? qp.getUnitOfMeasure() : UnitOfMeasure.UNIT);
                p.setCategoryId(qp.getCategoryId());

                ProductDTO created = productService.createProduct(p);
                product = productRepository.findById(created.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product","id", created.getId()));
            } else if (it.getProductId() != null) {
                product = productRepository.findById(it.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product","id", it.getProductId()));
            } else {
                throw new ValidationException("Cada ítem debe tener productId o quickProduct.");
            }

            SalesOrderItem soi = new SalesOrderItem();
            soi.setSalesOrder(order);
            soi.setProduct(product);
            soi.setQuantity(it.getQuantity());
            soi.setUnitPriceAtSale(it.getUnitPrice());
            soi.setSubtotal(it.getUnitPrice().multiply(it.getQuantity()));
            // soportar también el flag (por si usás quick con productos stockeados):
            soi.setSkipAutoPackaging(Boolean.TRUE.equals(it.getSkipAutoPackaging()));
            items.add(soi);

            total = total.add(soi.getSubtotal());
        }

        order.setSubtotalAmount(total);
        order.setTotalAmount(total);

        order.setItems(items);
        SalesOrder saved = salesOrderRepository.save(order);
        return salesOrderMapper.toDTO(saved);
    }

    // =========================
    // Envío / Entrega
    // =========================
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
