package com.one.core.domain.service.tenant.expenses.criteria;

import com.one.core.application.dto.tenant.expenses.ExpenseLogFilterDTO;
import com.one.core.domain.model.tenant.expenses.ExpenseLogs;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class ExpenseLogSpecification {
    public static Specification<ExpenseLogs> filterBy(ExpenseLogFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter == null) {
                return cb.conjunction();
            }

            if (StringUtils.hasText(filter.getCategory())) {
                predicates.add(cb.equal(cb.lower(root.get("category")), filter.getCategory().toLowerCase()));
            }
            if (filter.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), filter.getDateFrom()));
            }
            if (filter.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), filter.getDateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}