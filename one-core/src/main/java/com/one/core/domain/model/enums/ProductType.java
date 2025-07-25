package com.one.core.domain.model.enums;

public enum ProductType {
    PHYSICAL_GOOD, // Bien físico que usa stock
    SERVICE,       // Un servicio que no usa stock
    SUBSCRIPTION,  // Un abono o membresía recurrente
    DIGITAL,        // Un producto digital
    COMPOUND,        // Un producto compuesto por otros productos
    PACKAGING // Un producto de empaque o embalaje
}