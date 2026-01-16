package model.domain;

import model.domain.enumerations.ProposalStatus;
import model.domain.enumerations.TradeStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class Proposal {
    private String proposalId;
    private String proposerId;
    private String receiverId;
    private List<Card> cardsOffered;
    private List<Card> cardsRequested;
    private LocalDateTime lastUpdated;
    private ProposalStatus status;
    private String meetingPlace;
    private String meetingDate; // YYYY-MM-DD
    private String meetingTime; // HH:mm (optional)

    public String getProposalId() { return proposalId; }
    public void setProposalId(String proposalId) { this.proposalId = proposalId; }

    public String getProposerId() { return proposerId; }
    public void setProposerId(String proposerId) { this.proposerId = proposerId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public List<Card> getCardsOffered() { return cardsOffered; }
    public void setCardsOffered(List<Card> cardsOffered) { this.cardsOffered = cardsOffered; }

    public List<Card> getCardsRequested() { return cardsRequested; }
    public void setCardsRequested(List<Card> cardsRequested) { this.cardsRequested = cardsRequested; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public ProposalStatus getStatus() { return status; }
    public void setStatus(ProposalStatus status) { this.status = status; }

    public String getMeetingPlace() { return meetingPlace; }
    public void setMeetingPlace(String meetingPlace) { this.meetingPlace = meetingPlace; }

    public String getMeetingDate() { return meetingDate; }
    public void setMeetingDate(String meetingDate) { this.meetingDate = meetingDate; }

    public String getMeetingTime() { return meetingTime; }
    public void setMeetingTime(String meetingTime) { this.meetingTime = meetingTime; }

    @Override
    public String toString() {
        return "Proposal{" + "proposalId='" + proposalId + '\'' + ", proposerId='" + proposerId + '\'' + ", receiverId='" + receiverId + '\'' + ", offered=" + cardsOffered + ", requested=" + cardsRequested + ", status=" + status + '}';
    }

    // -------------------- Domain behavior (Information Expert) --------------------

    /**
     * Mark this proposal as accepted and update timestamp.
     * Business rules about transition can be enforced here.
     */
    public void accept() {
        this.status = ProposalStatus.ACCEPTED;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Mark this proposal as declined/rejected and update timestamp.
     */
    public void decline() {
        this.status = ProposalStatus.REJECTED;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Returns true if this proposal is expired according to lastUpdated + 1 day rule.
     */
    public boolean isExpired(LocalDateTime now) {
        if (this.lastUpdated == null || now == null) return false;
        return this.lastUpdated.plusDays(1).isBefore(now);
    }

    /**
     * Try to parse meetingDate and meetingTime into a LocalDateTime.
     * Returns empty Optional if parsing fails or no date specified.
     */
    public Optional<LocalDateTime> getMeetingLocalDateTime() {
        if (this.meetingDate == null || this.meetingDate.isEmpty()) return Optional.empty();
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(this.meetingDate);
            if (this.meetingTime != null && !this.meetingTime.isEmpty()) {
                try {
                    LocalTime time = LocalTime.parse(this.meetingTime);
                    return Optional.of(LocalDateTime.of(date, time));
                } catch (DateTimeParseException ex) {
                    // ignore and fallback to start of day
                }
            }
            return Optional.of(date.atStartOfDay());
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    /**
     * Build a TradeTransaction representing the scheduled trade for this proposal.
     * Returns null if insufficient data.
     */
    public TradeTransaction toTradeTransaction() {
        TradeStatus defaultStatus = TradeStatus.WAITING_FOR_ARRIVAL;
        int txId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        LocalDateTime creation = LocalDateTime.now();
        LocalDateTime tradeDate = creation;

        Optional<LocalDateTime> maybe = getMeetingLocalDateTime();
        if (maybe.isPresent()) tradeDate = maybe.get();

        List<Card> offered = this.cardsOffered != null ? new java.util.ArrayList<>(this.cardsOffered) : java.util.Collections.emptyList();
        List<Card> requested = this.cardsRequested != null ? new java.util.ArrayList<>(this.cardsRequested) : java.util.Collections.emptyList();

        // Crea i value object richiesti dal costruttore
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants(
            this.proposerId,
            this.receiverId,
            this.meetingPlace
        );
        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(
            creation,
            tradeDate,
            offered,
            requested
        );

        return new TradeTransaction(txId, defaultStatus, participants, details);
    }

}
