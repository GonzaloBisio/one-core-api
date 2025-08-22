package com.one.core.domain.service.billing;

import com.one.core.config.multitenancy.TenantContext;
import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage table checks and billing operations.
 *
 * <p>All operations are tenant aware using {@link TenantContext} to ensure
 * isolation between tenants.</p>
 */
@Service
public class TableBillingService {

    private final Map<String, Map<Long, SalesOrder>> openChecks = new ConcurrentHashMap<>();

    /**
     * Opens a new check for the given table.
     *
     * @param tableId table identifier
     */
    public void openCheck(Long tableId) {
        String tenant = requireTenant();
        openChecks.computeIfAbsent(tenant, t -> new ConcurrentHashMap<>());
        Map<Long, SalesOrder> tenantChecks = openChecks.get(tenant);
        if (tenantChecks.containsKey(tableId)) {
            throw new IllegalStateException("Check already open for table " + tableId);
        }
        SalesOrder order = new SalesOrder();
        order.setOrderDate(LocalDate.now());
        order.setStatus(SalesOrderStatus.PENDING_PAYMENT);
        tenantChecks.put(tableId, order);
    }

    /**
     * Adds an item to the open check of the table.
     *
     * @param tableId table identifier
     * @param item    item to add
     */
    public void addItem(Long tableId, SalesOrderItem item) {
        SalesOrder order = getCheck(tableId);
        order.addItem(item); // recalculateTotals is triggered inside
    }

    /**
     * Removes an item from the open check of the table.
     *
     * @param tableId table identifier
     * @param item    item to remove
     */
    public void removeItem(Long tableId, SalesOrderItem item) {
        SalesOrder order = getCheck(tableId);
        order.removeItem(item); // recalculateTotals is triggered inside
    }

    /**
     * Computes the total amount of the open check.
     *
     * @param tableId table identifier
     * @return total amount
     */
    public BigDecimal computeTotal(Long tableId) {
        SalesOrder order = getCheck(tableId);
        BigDecimal total = order.getItems().stream()
                .map(SalesOrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        return total;
    }

    /**
     * Closes the check but keeps it in memory for payment registration.
     *
     * @param tableId table identifier
     * @return the closed {@link SalesOrder}
     */
    public SalesOrder closeCheck(Long tableId) {
        SalesOrder order = getCheck(tableId);
        computeTotal(tableId);
        return order;
    }

    /**
     * Registers a payment for the table and removes the check.
     *
     * @param tableId table identifier
     * @param amount  amount paid
     */
    public void registerPayment(Long tableId, BigDecimal amount) {
        SalesOrder order = getCheck(tableId);
        BigDecimal total = computeTotal(tableId);
        if (amount.compareTo(total) < 0) {
            throw new IllegalArgumentException("Insufficient payment");
        }
        order.setStatus(SalesOrderStatus.COMPLETED);
        removeCheck(tableId);
    }

    private SalesOrder getCheck(Long tableId) {
        String tenant = requireTenant();
        Map<Long, SalesOrder> tenantChecks = openChecks.get(tenant);
        if (tenantChecks == null || !tenantChecks.containsKey(tableId)) {
            throw new IllegalStateException("No open check for table " + tableId);
        }
        return tenantChecks.get(tableId);
    }

    private void removeCheck(Long tableId) {
        String tenant = requireTenant();
        Map<Long, SalesOrder> tenantChecks = openChecks.get(tenant);
        if (tenantChecks != null) {
            tenantChecks.remove(tableId);
        }
    }

    private String requireTenant() {
        String tenant = TenantContext.getCurrentTenantSchema();
        if (tenant == null) {
            throw new IllegalStateException("Tenant context is not set");
        }
        return tenant;
    }
}

