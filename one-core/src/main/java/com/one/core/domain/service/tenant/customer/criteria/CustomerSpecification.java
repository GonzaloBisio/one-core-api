package com.one.core.domain.service.tenant.customer.criteria;

import com.one.core.application.dto.tenant.customer.CustomerFilterDTO;
import com.one.core.domain.model.tenant.customer.Customer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {

    public static Specification<Customer> filterBy(CustomerFilterDTO filter) {
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
            if (StringUtils.hasText(filter.getCustomerType())) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("customerType")), filter.getCustomerType().toLowerCase()));
            }
            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}