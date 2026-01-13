package model.domain;

import model.domain.enumerations.InspectionResult;
import model.domain.enumerations.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;


public class TradeTransaction {
    private int transactionId;
    private TradeStatus tradeStatus;
    private String proposerId;
    private String receiverId;
    private int proposerSessionCode;
    private int receiverSessionCode;
    private String storeId;
    private LocalDateTime creationTimestamp;
    private LocalDateTime tradeDate;
    private InspectionResult proposerInspectionResult;
    private InspectionResult receiverInspectionResult;
    private List<Card> offeredCards;
    private List<Card> requestedCards;
    private boolean proposerReviewed;
    private boolean receiverReviewed;

    public int generateSessionCode() {
        return (int)(Math.random() * 900000) + 100000; // Generates a random 6-digit code
    }
    public void updateTradeStatus(TradeStatus newStatus) {
        this.tradeStatus = newStatus;
    }
    public void recordInspectionResult(String userId, InspectionResult result) {
        if (userId.equals(proposerId)) {
            this.proposerInspectionResult = result;
        } else if (userId.equals(receiverId)) {
            this.receiverInspectionResult = result;
        }
    }
    public int confirmPresence(String userId) {
        if (userId.equals(proposerId)) {
            this.proposerSessionCode = generateSessionCode();
        } else if (userId.equals(receiverId)) {
            this.receiverSessionCode = generateSessionCode();
        }
        return userId.equals(proposerId) ? proposerSessionCode : receiverSessionCode;
    }

}
