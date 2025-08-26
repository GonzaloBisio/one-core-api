package com.one.core.domain.model.enums;

import java.math.BigDecimal;

/**
 * Supported units of measure with helpers to convert quantities to and from
 * their base units. Each unit also declares the magnitude type it belongs to
 * so domain rules can apply specific logic.
 */
public enum UnitOfMeasure {
    UNIT(Magnitude.UNIDAD, BigDecimal.ONE),
    KG(Magnitude.PESO, new BigDecimal("1000")),
    G(Magnitude.PESO, BigDecimal.ONE),
    L(Magnitude.VOLUMEN, new BigDecimal("1000")),
    ML(Magnitude.VOLUMEN, BigDecimal.ONE),
    CM3(Magnitude.VOLUMEN, BigDecimal.ONE),
    PERCENTAGE(Magnitude.UNIDAD, BigDecimal.ONE);


    private final Magnitude magnitude;
    private final BigDecimal factorToBase;

    UnitOfMeasure(Magnitude magnitude, BigDecimal factorToBase) {
        this.magnitude = magnitude;
        this.factorToBase = factorToBase;
    }

    /**
     * Converts a quantity expressed in this unit to the corresponding base
     * unit of its magnitude (grams for weight, milliliters for volume and unit
     * for countable items).
     *
     * @param quantity quantity expressed in this unit
     * @return quantity converted to the base unit
     */
    public BigDecimal toBase(BigDecimal quantity) {
        return quantity.multiply(factorToBase);
    }

    /**
     * Converts a quantity expressed in the base unit of the magnitude to this
     * unit of measure.
     *
     * @param quantity quantity expressed in base units
     * @return quantity converted to this unit
     */
    public BigDecimal fromBase(BigDecimal quantity) {
        return quantity.divide(factorToBase);
    }
    public boolean isPercentage() { return this == PERCENTAGE; }


    public Magnitude getMagnitude() {
        return magnitude;
    }

    public enum Magnitude {
        PESO,
        VOLUMEN,
        UNIDAD
    }
}
