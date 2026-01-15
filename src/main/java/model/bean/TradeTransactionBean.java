package model.bean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TradeTransactionBean {
    private int transactionId;
    private String proposerId;
    private String receiverId;
    private String storeId;
    private LocalDateTime tradeDate;
    private List<CardBean> offered = new ArrayList<>();
    private List<CardBean> requested = new ArrayList<>();
    private String status;
    private String proposalId;

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public String getProposerId() { return proposerId; }
    public void setProposerId(String proposerId) { this.proposerId = proposerId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public LocalDateTime getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDateTime tradeDate) { this.tradeDate = tradeDate; }

    public List<CardBean> getOffered() { return new ArrayList<>(offered); }
    public void setOffered(List<CardBean> offered) { this.offered = offered != null ? new ArrayList<>(offered) : new ArrayList<>(); }

    public List<CardBean> getRequested() { return new ArrayList<>(requested); }
    public void setRequested(List<CardBean> requested) { this.requested = requested != null ? new ArrayList<>(requested) : new ArrayList<>(); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProposalId() { return proposalId; }
    public void setProposalId(String proposalId) { this.proposalId = proposalId; }
}
