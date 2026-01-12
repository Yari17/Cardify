package model.domain;

import model.bean.CardBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain class representing a trade transaction between two users.
 * Follows Information Expert principle - knows all trade details and validation logic.
 */
public class TradeTransaction {
    private String id;
    private String senderId;
    private String receiverId;
    private List<CardBean> offeredCards;
    private CardBean requestedCard;
    private TradeStatus status;
    private String storeLocation;
    private LocalDateTime scheduledDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor.
     */
    public TradeTransaction() {
        this.id = UUID.randomUUID().toString();
        this.offeredCards = new ArrayList<>();
        this.status = TradeStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with essential fields.
     */
    public TradeTransaction(String senderId, String receiverId, 
                           List<CardBean> offeredCards, CardBean requestedCard) {
        this();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.offeredCards = new ArrayList<>(offeredCards);
        this.requestedCard = requestedCard;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public List<CardBean> getOfferedCards() {
        return new ArrayList<>(offeredCards);
    }

    public void setOfferedCards(List<CardBean> offeredCards) {
        this.offeredCards = offeredCards != null ? new ArrayList<>(offeredCards) : new ArrayList<>();
        updateTimestamp();
    }

    public CardBean getRequestedCard() {
        return requestedCard;
    }

    public void setRequestedCard(CardBean requestedCard) {
        this.requestedCard = requestedCard;
        updateTimestamp();
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
        updateTimestamp();
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
        updateTimestamp();
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
        updateTimestamp();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business Methods

    /**
     * Accepts the trade proposal and schedules it for completion.
     * 
     * @param store the store location where the trade will take place
     * @param date the scheduled date and time for the trade
     * @throws IllegalStateException if trade is not in PENDING status
     */
    public void accept(String store, LocalDateTime date) {
        if (status != TradeStatus.PENDING) {
            throw new IllegalStateException("Only pending trades can be accepted");
        }
        if (store == null || store.trim().isEmpty()) {
            throw new IllegalArgumentException("Store location is required");
        }
        if (date == null || date.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled date must be in the future");
        }
        
        this.status = TradeStatus.ACCEPTED;
        this.storeLocation = store;
        this.scheduledDate = date;
        updateTimestamp();
    }

    /**
     * Rejects the trade proposal.
     * 
     * @throws IllegalStateException if trade is not in PENDING status
     */
    public void reject() {
        if (status != TradeStatus.PENDING) {
            throw new IllegalStateException("Only pending trades can be rejected");
        }
        
        this.status = TradeStatus.REJECTED;
        updateTimestamp();
    }

    /**
     * Marks the trade as completed.
     * 
     * @throws IllegalStateException if trade is not in ACCEPTED status
     */
    public void complete() {
        if (status != TradeStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted trades can be completed");
        }
        
        this.status = TradeStatus.COMPLETED;
        updateTimestamp();
    }

    /**
     * Cancels the trade.
     * 
     * @throws IllegalStateException if trade is already completed
     */
    public void cancel() {
        if (status == TradeStatus.COMPLETED) {
            throw new IllegalStateException("Completed trades cannot be cancelled");
        }
        
        this.status = TradeStatus.CANCELLED;
        updateTimestamp();
    }

    /**
     * Checks if the trade can be modified.
     * 
     * @return true if the trade is in PENDING status, false otherwise
     */
    public boolean canBeModified() {
        return status == TradeStatus.PENDING;
    }

    /**
     * Validates the trade transaction.
     * 
     * @return true if all required fields are valid, false otherwise
     */
    public boolean isValid() {
        if (senderId == null || senderId.trim().isEmpty()) {
            return false;
        }
        if (receiverId == null || receiverId.trim().isEmpty()) {
            return false;
        }
        if (senderId.equals(receiverId)) {
            return false;
        }
        if (offeredCards == null || offeredCards.isEmpty()) {
            return false;
        }
        if (requestedCard == null) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks if the trade is pending.
     * 
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return status == TradeStatus.PENDING;
    }

    /**
     * Checks if the trade is accepted.
     * 
     * @return true if status is ACCEPTED
     */
    public boolean isAccepted() {
        return status == TradeStatus.ACCEPTED;
    }

    /**
     * Checks if the trade is completed.
     * 
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == TradeStatus.COMPLETED;
    }

    /**
     * Updates the updatedAt timestamp.
     */
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "TradeTransaction{" +
                "id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", offeredCards=" + (offeredCards != null ? offeredCards.size() : 0) +
                ", requestedCard=" + (requestedCard != null ? requestedCard.getName() : "null") +
                ", status=" + status +
                ", storeLocation='" + storeLocation + '\'' +
                ", scheduledDate=" + scheduledDate +
                '}';
    }
}
