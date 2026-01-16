package model.domain;


import model.domain.enumerations.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


public class TradeTransaction {
    private int transactionId;
    private TradeStatus tradeStatus;
    private String proposerId;
    private String receiverId;
    private int proposerSessionCode;
    private int receiverSessionCode;
    // Flag che indicano se il collector è effettivamente arrivato in negozio
    private boolean proposerArrived;
    private boolean receiverArrived;
    private String storeId;
    private LocalDateTime creationTimestamp;
    private LocalDateTime tradeDate;
    private List<Card> offeredCards;
    private List<Card> requestedCards;
    private boolean proposerReviewed;
    private boolean receiverReviewed;

    // Nuovi campi per ispezione
    private Boolean proposerInspectionOk;
    private Boolean receiverInspectionOk;

    private static final Random RANDOM = new Random();

    /**
     * Value Object che raggruppa i dati degli utenti coinvolti nello scambio.
     */
    public static class TradeParticipants {
        public final String proposerId;
        public final String receiverId;
        public final String storeId;
        public TradeParticipants(String proposerId, String receiverId, String storeId) {
            this.proposerId = proposerId;
            this.receiverId = receiverId;
            this.storeId = storeId;
        }
    }

    /**
     * Value Object che raggruppa i dettagli dello scambio.
     */
    public static class TradeDetails {
        public final LocalDateTime creationTimestamp;
        public final LocalDateTime tradeDate;
        public final List<Card> offeredCards;
        public final List<Card> requestedCards;
        public TradeDetails(LocalDateTime creationTimestamp, LocalDateTime tradeDate, List<Card> offeredCards, List<Card> requestedCards) {
            this.creationTimestamp = creationTimestamp;
            this.tradeDate = tradeDate;
            this.offeredCards = offeredCards;
            this.requestedCards = requestedCards;
        }
    }

    // Costruttore refattorizzato: ora accetta oggetti value invece di tanti parametri singoli
    public TradeTransaction(int transactionId, TradeStatus tradeStatus, TradeParticipants participants, TradeDetails details) {
        this.transactionId = transactionId;
        this.tradeStatus = tradeStatus;
        this.proposerId = participants.proposerId;
        this.receiverId = participants.receiverId;
        this.storeId = participants.storeId;
        this.creationTimestamp = details.creationTimestamp;
        this.tradeDate = details.tradeDate;
        this.offeredCards = details.offeredCards;
        this.requestedCards = details.requestedCards;
        this.proposerReviewed = false;
        this.receiverReviewed = false;
        this.proposerArrived = false;
        this.receiverArrived = false;
        this.proposerInspectionOk = null;
        this.receiverInspectionOk = null;
    }

    public int generateSessionCode() {
        // Genera un codice casuale a 6 cifre usando Random statico
        return RANDOM.nextInt(900000) + 100000;
    }

    public void updateTradeStatus(TradeStatus newStatus) {
        this.tradeStatus = newStatus;
    }

    public int confirmPresence(String userId) {
        int code = -1;
        if (userId == null) return -1;
        if (userId.equals(proposerId)) {
            this.proposerSessionCode = generateSessionCode();
            this.proposerArrived = true;
            code = this.proposerSessionCode;
        } else if (userId.equals(receiverId)) {
            this.receiverSessionCode = generateSessionCode();
            this.receiverArrived = true;
            code = this.receiverSessionCode;
        }

        // Update status: if both collectors have arrived, set BOTH_ARRIVED; otherwise PARTIALLY_ARRIVED
        if (this.proposerArrived && this.receiverArrived) {
            this.tradeStatus = TradeStatus.BOTH_ARRIVED;
        } else if (this.proposerArrived || this.receiverArrived) {
            this.tradeStatus = TradeStatus.PARTIALLY_ARRIVED;
        }

        return code;
    }

    /**
     * Verifica se il session code passato corrisponde a uno dei codici generati
     * e marca l'arrivo del relativo collector. Ritorna true se il codice è valido.
     * Metodo di dominio: la logica di transizione di stato è qui (Information Expert).
     */
    public boolean acceptSessionCode(int code) {
        if (code <= 0) return false;
        if (code == proposerSessionCode) {
            proposerArrived = true;
        } else if (code == receiverSessionCode) {
            receiverArrived = true;
        } else {
            return false;
        }

        // Aggiorna lo stato in base agli arrivi
        if (proposerArrived && receiverArrived) {
            this.tradeStatus = TradeStatus.INSPECTION_PHASE;
        } else {
            this.tradeStatus = TradeStatus.PARTIALLY_ARRIVED;
        }
        return true;
    }

    /**
     * Registra l'esito dell'ispezione per uno dei collector (true = ok, false = rejected).
     * Se uno dei due fallisce, lo scambio viene annullato; se entrambi passano, lo scambio è completato.
     */
    public void markInspectionResult(String collectorId, boolean ok) {
        if (collectorId == null) return;
        if (collectorId.equals(proposerId)) {
            this.proposerInspectionOk = ok;
        } else if (collectorId.equals(receiverId)) {
            this.receiverInspectionOk = ok;
        }

        // Se uno dei due è false -> annulla immediatamente
        if (Boolean.FALSE.equals(this.proposerInspectionOk) || Boolean.FALSE.equals(this.receiverInspectionOk)) {
            this.tradeStatus = TradeStatus.CANCELLED;
            return;
        }

        // Se entrambi sono true -> inspection passed (store must finalize exchange)
        if (Boolean.TRUE.equals(this.proposerInspectionOk) && Boolean.TRUE.equals(this.receiverInspectionOk)) {
            this.tradeStatus = TradeStatus.INSPECTION_PASSED;
        }
    }

    public boolean isProposerArrived() { return proposerArrived; }
    public boolean isReceiverArrived() { return receiverArrived; }

    public Boolean getProposerInspectionOk() { return proposerInspectionOk; }
    public Boolean getReceiverInspectionOk() { return receiverInspectionOk; }

    // ===== Getters used by controllers/views =====
    public int getTransactionId() {
        return transactionId;
    }

    // Getters per i session code (utili per lo store per verificare i codici generati dai collector)
    public int getProposerSessionCode() {
        return proposerSessionCode;
    }

    public int getReceiverSessionCode() {
        return receiverSessionCode;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public String getProposerId() {
        return proposerId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getStoreId() {
        return storeId;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public LocalDateTime getTradeDate() {
        return tradeDate;
    }

    public List<Card> getOfferedCards() {
        return offeredCards;
    }

    public List<Card> getRequestedCards() {
        return requestedCards;
    }

    public boolean isProposerReviewed() {
        return proposerReviewed;
    }

    public boolean isReceiverReviewed() {
        return receiverReviewed;
    }

    // proposalId intentionally removed: TradeTransaction no longer keeps a link to the originating Proposal

}
