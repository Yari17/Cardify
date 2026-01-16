package model.domain.enumerations;

public enum TradeStatus {
    WAITING_FOR_ARRIVAL,
    PARTIALLY_ARRIVED,
    BOTH_ARRIVED,
    INSPECTION_PHASE,
    INSPECTION_PASSED,
    COMPLETED,
    CANCELLED
}
