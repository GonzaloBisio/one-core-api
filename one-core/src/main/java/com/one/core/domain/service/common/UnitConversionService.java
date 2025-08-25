package com.one.core.domain.service.common;

import com.one.core.domain.model.enums.UnitOfMeasure;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Utility service to normalize quantities to and from base units.
 * Weight is stored in grams and volume in milliliters to keep
 * stock values consistent regardless of the unit originally
 * provided by clients.
 */
@Service
public class UnitConversionService {

    /**
     * Converts a quantity expressed in the given unit to its base unit
     * (grams for weight, milliliters for volume, unit for countable items).
     *
     * @param quantity amount expressed in the provided unit
     * @param unit unit of measure of the quantity
     * @return quantity converted to the corresponding base unit
     */
    public BigDecimal toBaseUnit(BigDecimal quantity, UnitOfMeasure unit) {
        if (quantity == null || unit == null) {
            return quantity;
        }
        return unit.toBase(quantity);
    }

    /**
     * Converts a quantity expressed in a base unit to the requested unit
     * of measure.
     *
     * @param quantity amount expressed in base units
     * @param unit target unit of measure
     * @return quantity converted to the target unit
     */
    public BigDecimal fromBaseUnit(BigDecimal quantity, UnitOfMeasure unit) {
        if (quantity == null || unit == null) {
            return quantity;
        }
        return unit.fromBase(quantity);
    }
}
