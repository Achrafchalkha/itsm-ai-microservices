package com.itsm.analytics.domain.model;

/**
 * Enumeration for different period types used in analytics
 */
public enum PeriodeType {
    DAILY("DAILY", "Quotidien"),
    WEEKLY("WEEKLY", "Hebdomadaire"),
    MONTHLY("MONTHLY", "Mensuel"),
    QUARTERLY("QUARTERLY", "Trimestriel"),
    YEARLY("YEARLY", "Annuel");

    private final String code;
    private final String description;

    PeriodeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }

    /**
     * Get PeriodeType from code
     */
    public static PeriodeType fromCode(String code) {
        for (PeriodeType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PeriodeType code: " + code);
    }
}
