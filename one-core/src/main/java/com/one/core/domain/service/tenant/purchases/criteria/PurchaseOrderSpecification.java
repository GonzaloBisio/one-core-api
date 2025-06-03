package com.one.core.domain.service.tenant.purchases.criteria;

import com.one.core.application.dto.tenant.purchases.PurchaseOrderFilterDTO;
import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.tenant.supplier.Supplier;     // Necesario para el join a Supplier
import com.one.core.domain.model.tenant.purchases.PurchaseOrder; // Tu entidad PurchaseOrder
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderSpecification {

    public static Specification<PurchaseOrder> filterBy(PurchaseOrderFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction(); // Devuelve un predicado que siempre es true (ningún filtro)
            }

            // Filtrar por ID de Proveedor
            if (filter.getSupplierId() != null) {
                // Asume que en tu entidad PurchaseOrder tienes un campo 'supplier' de tipo Supplier
                Join<PurchaseOrder, Supplier> supplierJoin = root.join("supplier");
                predicates.add(criteriaBuilder.equal(supplierJoin.get("id"), filter.getSupplierId()));
            }

            // Filtrar por Estado de la Orden
            if (filter.getStatus() != null) {
                // Asume que en tu entidad PurchaseOrder tienes un campo 'status' del tipo PurchaseOrderStatus
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filtrar por Rango de Fechas de la Orden ('orderDate')
            if (filter.getOrderDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), filter.getOrderDateFrom()));
            }
            if (filter.getOrderDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), filter.getOrderDateTo()));
            }

            // Filtrar por Rango de Fechas de Entrega Esperada ('expectedDeliveryDate')
            if (filter.getExpectedDeliveryDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("expectedDeliveryDate"), filter.getExpectedDeliveryDateFrom()));
            }
            if (filter.getExpectedDeliveryDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expectedDeliveryDate"), filter.getExpectedDeliveryDateTo()));
            }

            // Filtrar por Username del Creador
            if (StringUtils.hasText(filter.getCreatedByUsername())) {
                // Asume que en tu entidad PurchaseOrder tienes un campo 'createdByUser' de tipo TenantUser,
                // y que TenantUser tiene un campo 'username'.
                Join<PurchaseOrder, SystemUser> userJoin = root.join("createdByUser"); // Asegúrate que el campo es "createdByUser"
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