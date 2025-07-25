package com.one.core.domain.service.reports.criteria;

import com.one.core.application.dto.reports.ReportFilterDTO;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderSpecification {

    public static Specification<SalesOrder> filterBy(ReportFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            // Filtrar por método de pago
            if (StringUtils.hasText(filter.getPaymentMethod())) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}