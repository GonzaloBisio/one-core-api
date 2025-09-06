// com.one.core.domain.service.tenant.inventory.criteria.StockMovementSpecification
package com.one.core.domain.service.tenant.inventory.criteria;

import com.one.core.application.dto.tenant.inventory.StockMovementFilterDTO;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.tenant.product.StockMovement;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class StockMovementSpecification {

    public static Specification<StockMovement> filterBy(StockMovementFilterDTO f) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (f == null) return cb.and(predicates.toArray(new Predicate[0]));

            if (f.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), f.getProductId()));
            }

            if (f.getMovementTypes() != null && !f.getMovementTypes().isEmpty()) {
                predicates.add(root.get("movementType").in(f.getMovementTypes()));
            } else if (f.getMovementType() != null) {
                predicates.add(cb.equal(root.get("movementType"), f.getMovementType()));
            }

            // movementDate es OffsetDateTime. Convertimos LocalDate -> inicio/fin del día en zona del servidor.
            if (f.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("movementDate"),
                        startOfDay(f.getDateFrom())
                ));
            }
            if (f.getDateTo() != null) {
                predicates.add(cb.lessThan(
                        root.get("movementDate"),
                        startOfDay(f.getDateTo().plusDays(1))
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static java.time.OffsetDateTime startOfDay(LocalDate d) {
        return d.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
