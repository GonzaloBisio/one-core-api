package com.one.core.domain.service.tenant.product.criteria;

import com.one.core.application.dto.tenant.product.ProductCategoryFilterDTO;
import com.one.core.domain.model.tenant.product.ProductCategory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductCategorySpecification {

    public static Specification<ProductCategory> filterBy(ProductCategoryFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getDescription())) {
                predicates.add(criteriaBuilder.equal(root.get("description"), filter.getDescription()));
            }

            if (filter.getHasParent() != null) {
                if (filter.getHasParent()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("parentCategory")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("parentCategory")));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}