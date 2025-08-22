package com.one.core.application.dto.billing;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * DTO representing a check associated with a dining table.
 */
@Data
public class TableCheckDTO {
    private Long id;
    private BigDecimal totalAmount;
    private List<TableCheckItemDTO> items;
}

