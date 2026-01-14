package model.domain.enumerations;

public enum PersistenceType {
    /** Persistenza su file JSON */
    JSON,
    /** Persistenza su database JDBC */
    JDBC,
    /** Persistenza in memoria (per demo/test) */
    DEMO
}
