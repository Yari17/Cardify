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
    private List<Card> offeredCards;
    private List<Card> requestedCards;
    private boolean proposerReviewed;
    private boolean receiverReviewed;

    public TradeTransaction(int transactionId, TradeStatus tradeStatus, String proposerId, String receiverId, String storeId,
                            LocalDateTime creationTimestamp, LocalDateTime tradeDate,
                            List<Card> offeredCards, List<Card> requestedCards) {
        this.transactionId = transactionId;
        this.tradeStatus = tradeStatus;
        this.proposerId = proposerId;
        this.receiverId = receiverId;
        this.storeId = storeId;
        this.creationTimestamp = creationTimestamp;
        this.tradeDate = tradeDate;
        this.offeredCards = offeredCards;
        this.requestedCards = requestedCards;
        this.proposerReviewed = false;
        this.receiverReviewed = false;
    }
    public int generateSessionCode() {
        return (int)(Math.random() * 900000) + 100000; // Generates a random 6-digit code
    }
    public void updateTradeStatus(TradeStatus newStatus) {
        this.tradeStatus = newStatus;
    }

    public int confirmPresence(String userId) {
        if (userId.equals(proposerId)) {
            this.proposerSessionCode = generateSessionCode();
        } else if (userId.equals(receiverId)) {
            this.receiverSessionCode = generateSessionCode();
        }
        return userId.equals(proposerId) ? proposerSessionCode : receiverSessionCode;
    }

    // ===== Getters used by controllers/views =====
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public TradeStatus getTradeStatus() { return tradeStatus; }
    public String getProposerId() { return proposerId; }
    public String getReceiverId() { return receiverId; }
    public String getStoreId() { return storeId; }
    public LocalDateTime getCreationTimestamp() { return creationTimestamp; }
    public LocalDateTime getTradeDate() { return tradeDate; }
    public List<Card> getOfferedCards() { return offeredCards; }
    public List<Card> getRequestedCards() { return requestedCards; }
    public boolean isProposerReviewed() { return proposerReviewed; }
    public boolean isReceiverReviewed() { return receiverReviewed; }

}
