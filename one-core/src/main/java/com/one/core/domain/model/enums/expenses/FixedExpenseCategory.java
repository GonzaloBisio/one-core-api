package com.one.core.domain.model.enums.expenses;


public enum FixedExpenseCategory {
    ALQUILER("Alquiler"),
    SERVICIOS("Servicios (Luz, Gas, Agua, Internet)"),
    SALARIOS("Salarios y Cargas Sociales"),
    IMPUESTOS_Y_TASAS("Impuestos y Tasas Municipales"),
    SEGUROS("Seguros"),
    MARKETING("Marketing y Publicidad"),
    SOFTWARE_Y_SUSCRIPCIONES("Software y Suscripciones (POS, Apps)"),
    LIMPIEZA_Y_MANTENIMIENTO("Limpieza y Mantenimiento"),
    CONTADURIA("Servicios de Contaduría"),
    OTROS("Otros Gastos Fijos");

    private final String displayName;

    FixedExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}