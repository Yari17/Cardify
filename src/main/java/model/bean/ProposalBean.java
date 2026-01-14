package model.bean;

import java.util.List;

public class ProposalBean {
    private String fromUser;
    private String toUser;
    private List<CardBean> offered;
    private List<CardBean> requested;
    private String meetingPlace; // store username or id
    private String meetingDate; // date as string, expected format YYYY-MM-DD

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public List<CardBean> getOffered() { return offered; }
    public void setOffered(List<CardBean> offered) { this.offered = offered; }

    public List<CardBean> getRequested() { return requested; }
    public void setRequested(List<CardBean> requested) { this.requested = requested; }

    public String getMeetingPlace() { return meetingPlace; }
    public void setMeetingPlace(String meetingPlace) { this.meetingPlace = meetingPlace; }

    public String getMeetingDate() { return meetingDate; }
    public void setMeetingDate(String meetingDate) { this.meetingDate = meetingDate; }

    @Override
    public String toString() {
        return "ProposalBean{" + "fromUser='" + fromUser + '\'' + ", toUser='" + toUser + '\'' + ", offered=" + offered + ", requested=" + requested + '}';
    }
}
