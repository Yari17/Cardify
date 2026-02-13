package model.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean per il trasporto dei dati di una proposta di scambio.
 * Include informazioni sui partecipanti, le carte coinvolte e i dettagli
 * dell'incontro.
 */
public class ProposalBean {
    /** Identificativo unico della proposta. */
    private String proposalId;
    /** Nome utente del proponente. */
    private String fromUser;
    /** Nome utente del ricevente. */
    private String toUser;
    /** Lista di carte offerte dal proponente. */
    private List<CardBean> offered = new ArrayList<>();
    /** Lista di carte richieste in cambio. */
    private List<CardBean> requested = new ArrayList<>();
    /** Luogo dell'incontro (nome del negozio). */
    private String meetingPlace;
    /** Data dell'incontro (formattata come stringa). */
    private String meetingDate;
    /** Ora dell'incontro (formattata come stringa). */
    private String meetingTime;
    /** Stato della proposta (es. PENDING, ACCEPTED). */
    private String status;
    /**
     * Indica se la proposta è stata inviata dall'utente corrente (popolato dal
     * Controller).
     */
    private boolean isSentByMe;

    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public List<CardBean> getOffered() {
        return new ArrayList<>(offered);
    }

    public void setOffered(List<CardBean> offered) {
        this.offered = offered != null ? new ArrayList<>(offered) : new ArrayList<>();
    }

    public List<CardBean> getRequested() {
        return new ArrayList<>(requested);
    }

    public void setRequested(List<CardBean> requested) {
        this.requested = requested != null ? new ArrayList<>(requested) : new ArrayList<>();
    }

    public String getMeetingPlace() {
        return meetingPlace;
    }

    public void setMeetingPlace(String meetingPlace) {
        this.meetingPlace = meetingPlace;
    }

    public String getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(String meetingDate) {
        this.meetingDate = meetingDate;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSentByMe() {
        return isSentByMe;
    }

    public void setIsSentByMe(boolean isSentByMe) {
        this.isSentByMe = isSentByMe;
    }

    /**
     * Genera descrizione formattata dei dettagli della proposta.
     * Utilizzato dalle View per evitare costruzione testuale in-line.
     */
    public String getFormattedDetails() {
        StringBuilder content = new StringBuilder();
        content.append("From: ").append(fromUser).append("\n");
        content.append("To: ").append(toUser).append("\n\n");

        content.append("Cards Offered:\n");
        for (CardBean card : offered) {
            content.append("  - ").append(card.getName()).append(" (x").append(card.getQuantity()).append(")\n");
        }

        content.append("\nCards Requested:\n");
        for (CardBean card : requested) {
            content.append("  - ").append(card.getName()).append(" (x").append(card.getQuantity()).append(")\n");
        }

        if (status != null) {
            content.append("\nStatus: ").append(status);
        }

        return content.toString();
    }

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
