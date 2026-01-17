package model.bean;


import java.util.ArrayList;
import java.util.List;

public class ProposalBean {
    private String proposalId;
    private String fromUser;
    private String toUser;
    private List<CardBean> offered = new ArrayList<>();
    private List<CardBean> requested = new ArrayList<>();
    private String meetingPlace; // store username or id
    private String meetingDate; // date as string, expected format YYYY-MM-DD
    private String meetingTime; // time as string, expected format HH:mm (optional)
    private String status; // ProposalStatus name: PENDING, ACCEPTED, REJECTED, EXPIRED

    public String getProposalId() { return proposalId; }
    public void setProposalId(String proposalId) { this.proposalId = proposalId; }

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public List<CardBean> getOffered() { return new ArrayList<>(offered); }
    public void setOffered(List<CardBean> offered) { this.offered = offered != null ? new ArrayList<>(offered) : new ArrayList<>(); }

    public List<CardBean> getRequested() { return new ArrayList<>(requested); }
    public void setRequested(List<CardBean> requested) { this.requested = requested != null ? new ArrayList<>(requested) : new ArrayList<>(); }

    public String getMeetingPlace() { return meetingPlace; }
    public void setMeetingPlace(String meetingPlace) { this.meetingPlace = meetingPlace; }

    public String getMeetingDate() { return meetingDate; }
    public void setMeetingDate(String meetingDate) { this.meetingDate = meetingDate; }

    public String getMeetingTime() { return meetingTime; }
    public void setMeetingTime(String meetingTime) { this.meetingTime = meetingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "ProposalBean{" +
                "proposalId='" + proposalId + '\'' +
                ", fromUser='" + fromUser + '\'' +
                ", toUser='" + toUser + '\'' +
                ", status='" + status + '\'' +
                ", offered=" + offered +
                ", requested=" + requested +
                '}';
    }
}
