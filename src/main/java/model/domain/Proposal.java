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
    private String meetingDate; 
    private String meetingTime; 

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

    

    
    public void accept() {
        this.status = ProposalStatus.ACCEPTED;
        this.lastUpdated = LocalDateTime.now();
    }

    
    public void decline() {
        this.status = ProposalStatus.REJECTED;
        this.lastUpdated = LocalDateTime.now();
    }

    
    public boolean isExpired(LocalDateTime now) {
        if (this.lastUpdated == null || now == null) return false;
        return this.lastUpdated.plusDays(1).isBefore(now);
    }

    
    public Optional<LocalDateTime> getMeetingLocalDateTime() {
        if (this.meetingDate == null || this.meetingDate.isEmpty()) return Optional.empty();
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(this.meetingDate);
            if (this.meetingTime != null && !this.meetingTime.isEmpty()) {
                Optional<LocalTime> maybeTime = parseMeetingTime(this.meetingTime);
                if (maybeTime.isPresent()) return Optional.of(LocalDateTime.of(date, maybeTime.get()));
            }
            return Optional.of(date.atStartOfDay());
        } catch (DateTimeParseException _) {
            return Optional.empty();
        }
    }

    
    private Optional<LocalTime> parseMeetingTime(String mt) {
        try {
            LocalTime time = LocalTime.parse(mt);
            return Optional.of(time);
        } catch (DateTimeParseException _) {
            return Optional.empty();
        }
    }

    
    public TradeTransaction toTradeTransaction() {
        TradeStatus defaultStatus = TradeStatus.WAITING_FOR_ARRIVAL;
        int txId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        LocalDateTime creation = LocalDateTime.now();
        LocalDateTime tradeDate = creation;

        Optional<LocalDateTime> maybe = getMeetingLocalDateTime();
        if (maybe.isPresent()) tradeDate = maybe.get();

        List<Card> offered = this.cardsOffered != null ? new java.util.ArrayList<>(this.cardsOffered) : java.util.Collections.emptyList();
        List<Card> requested = this.cardsRequested != null ? new java.util.ArrayList<>(this.cardsRequested) : java.util.Collections.emptyList();

        
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
