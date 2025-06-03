// src/main/java/com/one/core/domain/service/tenant/sales/criteria/SalesOrderSpecification.java
package com.one.core.domain.service.tenant.sales.criteria;

import com.one.core.application.dto.tenant.sales.SalesOrderFilterDTO;
import com.one.core.domain.model.admin.SystemUser; // <--- IMPORTANTE: Usar SystemUser
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SalesOrderSpecification {

    public static Specification<SalesOrder> filterBy(SalesOrderFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            // Filtrar por ID de Cliente
            if (filter.getCustomerId() != null) {
                // Asume que en SalesOrder el campo es 'customer' de tipo Customer
                Join<SalesOrder, Customer> customerJoin = root.join("customer");
                predicates.add(criteriaBuilder.equal(customerJoin.get("id"), filter.getCustomerId()));
            }

            // Filtrar por Estado de la Orden
            if (filter.getStatus() != null) {
                // Asume que en SalesOrder el campo es 'status' de tipo SalesOrderStatus
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filtrar por Rango de Fechas de la Orden ('orderDate')
            if (filter.getOrderDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), filter.getOrderDateFrom()));
            }
            if (filter.getOrderDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), filter.getOrderDateTo()));
            }

            // Filtrar por Monto Total Mínimo
            if (filter.getMinTotalAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), filter.getMinTotalAmount()));
            }

            // Filtrar por Monto Total Máximo
            if (filter.getMaxTotalAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), filter.getMaxTotalAmount()));
            }

            // Filtrar por Username del Creador (SystemUser)
            if (StringUtils.hasText(filter.getCreatedByUsername())) {
                // Asume que en SalesOrder el campo es 'createdByUser' de tipo SystemUser,
                // y que SystemUser tiene un campo 'username'.
                Join<SalesOrder, SystemUser> userJoin = root.join("createdByUser"); // <--- CAMBIO IMPORTANTE: Join con SystemUser
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("username")),
                        "%" + filter.getCreatedByUsername().toLowerCase() + "%")
                );
            }

            // Combinar todos los predicados con un AND lógico
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}