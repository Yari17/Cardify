package model.bean;

import model.domain.TradeStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean class for transferring trade transaction data between layers.
 * Follows Pure Fabrication principle - exists for technical reasons, not domain modeling.
 */
public class TradeBean {
    private String id;
    private String senderUsername;
    private String receiverUsername;
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
    public TradeBean() {
        this.offeredCards = new ArrayList<>();
    }

    /**
     * Constructor with essential fields.
     */
    public TradeBean(String id, String senderUsername, String receiverUsername,
                     List<CardBean> offeredCards, CardBean requestedCard, TradeStatus status) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.offeredCards = offeredCards != null ? new ArrayList<>(offeredCards) : new ArrayList<>();
        this.requestedCard = requestedCard;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public List<CardBean> getOfferedCards() {
        return new ArrayList<>(offeredCards);
    }

    public void setOfferedCards(List<CardBean> offeredCards) {
        this.offeredCards = offeredCards != null ? new ArrayList<>(offeredCards) : new ArrayList<>();
    }

    public CardBean getRequestedCard() {
        return requestedCard;
    }

    public void setRequestedCard(CardBean requestedCard) {
        this.requestedCard = requestedCard;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
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

    // Utility Methods

    /**
     * Gets a formatted string of offered card names.
     *
     * @return comma-separated list of card names
     */
    public String getOfferedCardsNames() {
        if (offeredCards == null || offeredCards.isEmpty()) {
            return "None";
        }
        return offeredCards.stream()
                .map(CardBean::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("None");
    }

    /**
     * Gets the number of cards being offered.
     *
     * @return number of offered cards
     */
    public int getOfferedCardsCount() {
        return offeredCards != null ? offeredCards.size() : 0;
    }

    /**
     * Checks if this trade is pending.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return status == TradeStatus.PENDING;
    }

    /**
     * Checks if this trade is accepted.
     *
     * @return true if status is ACCEPTED
     */
    public boolean isAccepted() {
        return status == TradeStatus.ACCEPTED;
    }

    /**
     * Checks if this trade is completed.
     *
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == TradeStatus.COMPLETED;
    }

    /**
     * Gets a human-readable status string.
     *
     * @return formatted status string
     */
    public String getStatusDisplay() {
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case PENDING -> "In attesa";
            case ACCEPTED -> "Accettato";
            case REJECTED -> "Rifiutato";
            case COMPLETED -> "Completato";
            case CANCELLED -> "Annullato";
        };
    }

    @Override
    public String toString() {
        return "TradeBean{" +
                "id='" + id + '\'' +
                ", sender='" + senderUsername + '\'' +
                ", receiver='" + receiverUsername + '\'' +
                ", offeredCards=" + getOfferedCardsCount() +
                ", requestedCard='" + (requestedCard != null ? requestedCard.getName() : "null") + '\'' +
                ", status=" + status +
                '}';
    }
}

