package model.bean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean per il trasporto dei dati di una sessione di scambio fisico tra
 * collezionisti.
 * Contiene lo stato dell'incontro presso il negozio, i codici di verifica e
 * l'esito delle ispezioni.
 */
public class TradeSessionBean {
    /** ID della transazione/sessione. */
    private int transactionId;
    /** ID dell'utente proponente. */
    private String proposerId;
    /** ID dell'utente ricevente. */
    private String receiverId;
    /** ID del negozio fisico (store). */
    private String storeId;
    /** Data e ora pianificata per lo scambio fisico. */
    private LocalDateTime tradeDate;
    /** Lista di carte che il proponente porterà per lo scambio. */
    private List<CardBean> offered = new ArrayList<>();
    /** Lista di carte che il ricevente porterà per lo scambio. */
    private List<CardBean> requested = new ArrayList<>();
    /** Stato corrente della sessione (es. BOTH_ARRIVED, COMPLETED). */
    private String status;
    /** Codice segreto del proponente. */
    private int proposerSessionCode;
    /** Codice segreto del ricevente. */
    private int receiverSessionCode;
    /** Esito ispezione proponente (true=visto e accettato). */
    private Boolean proposerInspectionOk;
    /** Esito ispezione ricevente (true=visto e accettato). */
    private Boolean receiverInspectionOk;
    /** Flag presenza proponente. */
    private boolean proposerArrived;
    /** Flag presenza ricevente. */
    private boolean receiverArrived;

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getProposerId() {
        return proposerId;
    }

    public void setProposerId(String proposerId) {
        this.proposerId = proposerId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public LocalDateTime getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDateTime tradeDate) {
        this.tradeDate = tradeDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProposerSessionCode() {
        return proposerSessionCode;
    }

    public void setProposerSessionCode(int proposerSessionCode) {
        this.proposerSessionCode = proposerSessionCode;
    }

    public int getReceiverSessionCode() {
        return receiverSessionCode;
    }

    public void setReceiverSessionCode(int receiverSessionCode) {
        this.receiverSessionCode = receiverSessionCode;
    }

    public Boolean getProposerInspectionOk() {
        return proposerInspectionOk;
    }

    public void setProposerInspectionOk(Boolean proposerInspectionOk) {
        this.proposerInspectionOk = proposerInspectionOk;
    }

    public Boolean getReceiverInspectionOk() {
        return receiverInspectionOk;
    }

    public void setReceiverInspectionOk(Boolean receiverInspectionOk) {
        this.receiverInspectionOk = receiverInspectionOk;
    }

    public boolean isProposerArrived() {
        return proposerArrived;
    }

    public void setProposerArrived(boolean proposerArrived) {
        this.proposerArrived = proposerArrived;
    }

    public boolean isReceiverArrived() {
        return receiverArrived;
    }

    public void setReceiverArrived(boolean receiverArrived) {
        this.receiverArrived = receiverArrived;
    }
}
