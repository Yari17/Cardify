        
package model.domain;


import model.domain.enumerations.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


public class TradeTransaction {
    private int transactionId;
    private TradeStatus tradeStatus;
    private String proposerId;
    private String receiverId;
    private int proposerSessionCode;
    private int receiverSessionCode;
    
    private boolean proposerArrived;
    private boolean receiverArrived;
    private String storeId;
    private LocalDateTime creationTimestamp;
    private LocalDateTime tradeDate;
    private List<Card> offeredCards;
    private List<Card> requestedCards;
    private boolean proposerReviewed;
    private boolean receiverReviewed;

    
    private Boolean proposerInspectionOk;
    private Boolean receiverInspectionOk;

    private static final Random RANDOM = new Random();

    
    public static class TradeParticipants {
        public final String proposerId;
        public final String receiverId;
        public final String storeId;

        public TradeParticipants(String proposerId, String receiverId, String storeId) {
            this.proposerId = proposerId;
            this.receiverId = receiverId;
            this.storeId = storeId;
        }
    }

    
    public static class TradeDetails {
        public final LocalDateTime creationTimestamp;
        public final LocalDateTime tradeDate;
        public final List<Card> offeredCards;
        public final List<Card> requestedCards;

        public TradeDetails(LocalDateTime creationTimestamp, LocalDateTime tradeDate, List<Card> offeredCards, List<Card> requestedCards) {
            this.creationTimestamp = creationTimestamp;
            this.tradeDate = tradeDate;
            this.offeredCards = offeredCards;
            this.requestedCards = requestedCards;
        }
    }

    
    public TradeTransaction(int transactionId, TradeStatus tradeStatus, TradeParticipants participants, TradeDetails details) {
        this.transactionId = transactionId;
        this.tradeStatus = tradeStatus;
        this.proposerId = participants.proposerId;
        this.receiverId = participants.receiverId;
        this.storeId = participants.storeId;
        this.creationTimestamp = details.creationTimestamp;
        this.tradeDate = details.tradeDate;
        this.offeredCards = details.offeredCards;
        this.requestedCards = details.requestedCards;
        this.proposerReviewed = false;
        this.receiverReviewed = false;
        this.proposerArrived = false;
        this.receiverArrived = false;
        this.proposerInspectionOk = null;
        this.receiverInspectionOk = null;
    }

    public int generateSessionCode() {
        
        return RANDOM.nextInt(900000) + 100000;
    }

    public void updateTradeStatus(TradeStatus newStatus) {
        this.tradeStatus = newStatus;
    }

    public int confirmPresence(String userId) {
        int code = -1;
        if (userId == null) return -1;
        if (userId.equals(proposerId)) {
            this.proposerSessionCode = generateSessionCode();
            this.proposerArrived = true;
            code = this.proposerSessionCode;
        } else if (userId.equals(receiverId)) {
            this.receiverSessionCode = generateSessionCode();
            this.receiverArrived = true;
            code = this.receiverSessionCode;
        }

        
        if (this.proposerArrived && this.receiverArrived) {
            this.tradeStatus = TradeStatus.BOTH_ARRIVED;
        } else if (this.proposerArrived || this.receiverArrived) {
            this.tradeStatus = TradeStatus.PARTIALLY_ARRIVED;
        }

        return code;
    }

    
    public boolean acceptSessionCode(int code) {
        if (code <= 0) return false;
        if (code == proposerSessionCode) {
            proposerArrived = true;
        } else if (code == receiverSessionCode) {
            receiverArrived = true;
        } else {
            return false;
        }

        
        if (proposerArrived && receiverArrived) {
            this.tradeStatus = TradeStatus.INSPECTION_PHASE;
        } else {
            this.tradeStatus = TradeStatus.PARTIALLY_ARRIVED;
        }
        return true;
    }

    
    public void markInspectionResult(String collectorId, boolean ok) {
        if (collectorId == null) return;
        if (collectorId.equals(proposerId)) {
            this.proposerInspectionOk = ok;
        } else if (collectorId.equals(receiverId)) {
            this.receiverInspectionOk = ok;
        }

        
        if (Boolean.FALSE.equals(this.proposerInspectionOk) || Boolean.FALSE.equals(this.receiverInspectionOk)) {
            this.tradeStatus = TradeStatus.CANCELLED;
            return;
        }
        this.tradeStatus = TradeStatus.INSPECTION_PASSED;

    }

    public boolean isProposerArrived() {
        return proposerArrived;
    }

    public boolean isReceiverArrived() {
        return receiverArrived;
    }

    public Boolean getProposerInspectionOk() {
        return proposerInspectionOk;
    }

    public Boolean getReceiverInspectionOk() {
        return receiverInspectionOk;
    }

    
    public int getTransactionId() {
        return transactionId;
    }

    
    public int getProposerSessionCode() {
        return proposerSessionCode;
    }

    public int getReceiverSessionCode() {
        return receiverSessionCode;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public String getProposerId() {
        return proposerId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getStoreId() {
        return storeId;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public LocalDateTime getTradeDate() {
        return tradeDate;
    }

    public List<Card> getOfferedCards() {
        return offeredCards;
    }

    public List<Card> getRequestedCards() {
        return requestedCards;
    }

    public boolean isProposerReviewed() {
        return proposerReviewed;
    }

    public boolean isReceiverReviewed() {
        return receiverReviewed;
    }

}
