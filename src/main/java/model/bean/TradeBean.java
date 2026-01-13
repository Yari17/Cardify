package model.bean;

import model.domain.enumerations.TradeStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean class for transferring trade transaction data between layers.
 * Follows Pure Fabrication principle - exists for technical reasons, not domain modeling.
 */
public class TradeBean {
    private String tradeId;
    private String storeId;
    private TradeStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ParticipantBean> participants = new ArrayList<>();

    public TradeBean() {}

    public TradeBean(String tradeId, String storeId, TradeStatus status, Instant createdAt, Instant updatedAt, List<ParticipantBean> participants) {
        this.tradeId = tradeId;
        this.storeId = storeId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
    }

    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public TradeStatus getStatus() { return status; }
    public void setStatus(TradeStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ParticipantBean> getParticipants() { return new ArrayList<>(participants); }
    public void setParticipants(List<ParticipantBean> participants) {
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
    }

    // Utility: get participant by userId
    public ParticipantBean getParticipant(String userId) {
        if (participants == null) return null;
        return participants.stream()
                .filter(p -> userId.equals(p.getUserId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "TradeBean{" +
                "tradeId='" + tradeId + '\'' +
                ", storeId='" + storeId + '\'' +
                ", status=" + status +
                ", participants=" + (participants != null ? participants.size() : 0) +
                '}';
    }

    // --- Nested beans for participant structure ---

    public static class ParticipantBean {
        private String userId;
        private String role;
        private PresenceBean presence;
        private InspectionBean inspection;
        private List<ItemBean> items = new ArrayList<>();
        private FeedbackBean feedback;

        public ParticipantBean() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public PresenceBean getPresence() { return presence; }
        public void setPresence(PresenceBean presence) { this.presence = presence; }

        public InspectionBean getInspection() { return inspection; }
        public void setInspection(InspectionBean inspection) { this.inspection = inspection; }

        public List<ItemBean> getItems() { return new ArrayList<>(items); }
        public void setItems(List<ItemBean> items) { this.items = items != null ? new ArrayList<>(items) : new ArrayList<>(); }

        public FeedbackBean getFeedback() { return feedback; }
        public void setFeedback(FeedbackBean feedback) { this.feedback = feedback; }
    }

    public static class PresenceBean {
        private boolean confirmed;
        private Instant confirmedAt;
        private String verificationCode;
        private boolean verifiedByStore;
        private Instant verifiedAt;
        private String verifiedBy;

        public boolean isConfirmed() { return confirmed; }
        public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

        public Instant getConfirmedAt() { return confirmedAt; }
        public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }

        public String getVerificationCode() { return verificationCode; }
        public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

        public boolean isVerifiedByStore() { return verifiedByStore; }
        public void setVerifiedByStore(boolean verifiedByStore) { this.verifiedByStore = verifiedByStore; }

        public Instant getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

        public String getVerifiedBy() { return verifiedBy; }
        public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
    }

    public static class InspectionBean {
        private String status; // Use InspectionResult enum name as string
        private String inspectedBy;
        private String notes;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getInspectedBy() { return inspectedBy; }
        public void setInspectedBy(String inspectedBy) { this.inspectedBy = inspectedBy; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class ItemBean {
        private String id;
        private String name;
        private String gameType;
        private int quantity;
        private boolean tradable;
        private String status; // Use ItemStatus enum name as string

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGameType() { return gameType; }
        public void setGameType(String gameType) { this.gameType = gameType; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public boolean isTradable() { return tradable; }
        public void setTradable(boolean tradable) { this.tradable = tradable; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class FeedbackBean {
        private Integer rating;
        private String comment;
        private boolean canReview;

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public boolean isCanReview() { return canReview; }
        public void setCanReview(boolean canReview) { this.canReview = canReview; }
    }
}
