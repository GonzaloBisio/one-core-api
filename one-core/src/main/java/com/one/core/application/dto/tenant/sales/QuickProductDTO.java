package com.one.core.application.dto.tenant.sales;

import com.one.core.domain.model.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuickProductDTO {
    @NotBlank
    private String name;
    private UnitOfMeasure unitOfMeasure = UnitOfMeasure.UNIT;
    private Long categoryId;
}