package model.domain;

/**
 * Enumeration representing the status of a trade transaction.
 */
public enum TradeStatus {
    /**
     * Trade proposal has been sent and is awaiting response.
     */
    PENDING,

    /**
     * Trade has been accepted and is scheduled for completion.
     */
    ACCEPTED,

    /**
     * Trade proposal has been rejected.
     */
    REJECTED,

    /**
     * Trade has been successfully completed.
     */
    COMPLETED,

    /**
     * Trade has been cancelled by either party.
     */
    CANCELLED
}

