package model.domain;

import model.domain.enumerations.ProposalStatus;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public String toString() {
        return "Proposal{" + "proposalId='" + proposalId + '\'' + ", proposerId='" + proposerId + '\'' + ", receiverId='" + receiverId + '\'' + ", offered=" + cardsOffered + ", requested=" + cardsRequested + ", status=" + status + '}';
    }
}
