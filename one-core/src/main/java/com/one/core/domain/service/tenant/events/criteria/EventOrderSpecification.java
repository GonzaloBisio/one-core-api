package com.one.core.domain.service.tenant.events.criteria;

import com.one.core.application.dto.tenant.events.EventOrderFilterDTO;
import com.one.core.domain.model.enums.events.EventOrderStatus;
import com.one.core.domain.model.tenant.events.EventOrder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class EventOrderSpecification {

    public static Specification<EventOrder> filterBy(EventOrderFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), filter.getCustomerId()));
            }

            if (StringUtils.hasText(filter.getStatus())) {
                try {
                    EventOrderStatus statusEnum = EventOrderStatus.valueOf(filter.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    // Ignorar si el status no es válido
                }
            }

            if (filter.getEventDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), filter.getEventDateFrom()));
            }

            if (filter.getEventDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), filter.getEventDateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}