
package com.one.core.domain.service.tenant.supplier.criteria;

import com.one.core.application.dto.tenant.supplier.SupplierFilterDTO;
import com.one.core.domain.model.tenant.supplier.Supplier;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SupplierSpecification {

    public static Specification<Supplier> filterBy(SupplierFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getEmail())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + filter.getEmail().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getTaxId())) {
                predicates.add(criteriaBuilder.equal(root.get("taxId"), filter.getTaxId()));
            }

            // Añade más predicados para otros campos de filtro aquí

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}