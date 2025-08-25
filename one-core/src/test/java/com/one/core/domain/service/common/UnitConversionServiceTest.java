package com.one.core.domain.service.common;

import com.one.core.domain.model.enums.UnitOfMeasure;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UnitConversionServiceTest {

    private final UnitConversionService service = new UnitConversionService();

    @Test
    void toBaseUnitConvertsKilogramsToGrams() {
        BigDecimal result = service.toBaseUnit(new BigDecimal("0.5"), UnitOfMeasure.KG);
        assertEquals(new BigDecimal("500"), result);
    }

    @Test
    void fromBaseUnitConvertsGramsToKilograms() {
        BigDecimal result = service.fromBaseUnit(new BigDecimal("1500"), UnitOfMeasure.KG);
        assertEquals(new BigDecimal("1.5"), result);
    }

    @Test
    void normalizeFromBaseUnitUsesSmallerUnitWhenLessThanOne() {
        UnitConversionService.NormalizedQuantity normalized =
                service.normalizeFromBaseUnit(new BigDecimal("500"), UnitOfMeasure.KG);
        assertEquals(new BigDecimal("500"), normalized.quantity());
        assertEquals(UnitOfMeasure.G, normalized.unit());
    }

    @Test
    void normalizeFromBaseUnitKeepsUnitWhenGreaterOrEqualToOne() {
        UnitConversionService.NormalizedQuantity normalized =
                service.normalizeFromBaseUnit(new BigDecimal("2000"), UnitOfMeasure.KG);
        assertEquals(new BigDecimal("2"), normalized.quantity());
        assertEquals(UnitOfMeasure.KG, normalized.unit());
    }

    @Test
    void normalizeFromBaseUnitHandlesVolume() {
        UnitConversionService.NormalizedQuantity normalized =
                service.normalizeFromBaseUnit(new BigDecimal("500"), UnitOfMeasure.L);
        assertEquals(new BigDecimal("500"), normalized.quantity());
        assertEquals(UnitOfMeasure.ML, normalized.unit());
    }
}
