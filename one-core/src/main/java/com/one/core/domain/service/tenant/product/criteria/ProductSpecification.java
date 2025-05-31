package com.one.core.domain.service.tenant.product.criteria;

import com.one.core.application.dto.tenant.product.ProductFilterDTO;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductCategory;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterBy(ProductFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getSku())) {
                predicates.add(criteriaBuilder.equal(root.get("sku"), filter.getSku()));
            }

            if (filter.getCategoryId() != null) {
                Join<Product, ProductCategory> categoryJoin = root.join("category"); // "category" es el nombre del campo en la entidad Product
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), filter.getCategoryId()));
            }

            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}