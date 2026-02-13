
package model.domain;

import model.domain.enumerations.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Rappresenta una sessione di scambio (TradeSession) tra due utenti presso un
 * negozio.
 * Gestisce l'intero ciclo di vita dello scambio fisico: arrivo dei
 * partecipanti,
 * ispezione delle carte e finalizzazione o annullamento.
 */
public class TradeSession {
    /** Identificativo univoco della sessione. */
    private int sessionId;
    /**
     * Stato corrente dello scambio (es. WAITING_FOR_ARRIVAL, COMPLETED, CANCELLED).
     */
    private TradeStatus tradeStatus;
    /** ID dell'utente proponente. */
    private final String proposerId;
    /** ID dell'utente ricevente. */
    private final String receiverId;
    /** Codice segreto generato per il proponente per confermare la presenza. */
    private int proposerSessionCode;
    /** Codice segreto generato per il ricevente per confermare la presenza. */
    private int receiverSessionCode;

    /** Indica se il proponente è arrivato in negozio. */
    private boolean proposerArrived;
    /** Indica se il ricevente è arrivato in negozio. */
    private boolean receiverArrived;
    /** ID del negozio fisico ospitante lo scambio. */
    private final String storeId;
    /** Timestamp di creazione della sessione. */
    private final LocalDateTime creationTimestamp;
    /** Data e ora pianificate per lo scambio. */
    private final LocalDateTime tradeDate;
    /** Lista delle carte offerte dal proponente. */
    private final List<Card> offeredCards;
    /** Lista delle carte richieste in cambio dal proponente. */
    private final List<Card> requestedCards;

    /**
     * Risultato dell'ispezione delle carte da parte del proponente (true=OK,
     * false=KO).
     */
    private Boolean proposerInspectionOk;
    /**
     * Risultato dell'ispezione delle carte da parte del ricevente (true=OK,
     * false=KO).
     */
    private Boolean receiverInspectionOk;

    /** Generatore di numeri casuali per i codici di sessione. */
    private static final Random RANDOM = new Random();

    /**
     * Classe helper per raggruppare i partecipanti di uno scambio.
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
     * Classe helper per raggruppare i dettagli logistici e il contenuto dello
     * scambio.
     */
    public static class TradeDetails {
        public final LocalDateTime creationTimestamp;
        public final LocalDateTime tradeDate;
        public final List<Card> offeredCards;
        public final List<Card> requestedCards;

        public TradeDetails(LocalDateTime creationTimestamp, LocalDateTime tradeDate, List<Card> offeredCards,
                List<Card> requestedCards) {
            this.creationTimestamp = creationTimestamp;
            this.tradeDate = tradeDate;
            this.offeredCards = offeredCards;
            this.requestedCards = requestedCards;
        }
    }

    /**
     * Costruttore della sessione di scambio.
     * 
     * @param transactionId ID transazione.
     * @param tradeStatus   Stato iniziale.
     * @param participants  Dati dei partecipanti.
     * @param details       Dati logistici e carte.
     */
    public TradeSession(int transactionId, TradeStatus tradeStatus, TradeParticipants participants,
            TradeDetails details) {
        this.sessionId = transactionId;
        this.tradeStatus = tradeStatus;
        this.proposerId = participants.proposerId;
        this.receiverId = participants.receiverId;
        this.storeId = participants.storeId;
        this.creationTimestamp = details.creationTimestamp;
        this.tradeDate = details.tradeDate;
        this.offeredCards = details.offeredCards;
        this.requestedCards = details.requestedCards;
        this.proposerArrived = false;
        this.receiverArrived = false;
        this.proposerInspectionOk = null;
        this.receiverInspectionOk = null;
    }

    /**
     * Genera un codice numerico casuale a 6 cifre per la conferma di presenza.
     * 
     * @return Codice intero tra 100.000 e 999.999.
     */
    public int generateSessionCode() {
        return RANDOM.nextInt(900000) + 100000;
    }

    public void updateTradeStatus(TradeStatus newStatus) {
        this.tradeStatus = newStatus;
    }

    /**
     * Conferma la presenza di un utente generandone il codice se non già presente.
     * Delega la generazione effettiva a {@link #generateSessionCode()}.
     * 
     * @param userId ID dell'utente che conferma l'arrivo.
     * @return Il codice di sessione assegnato o -1 se l'ID non corrisponde ai
     *         partecipanti.
     */
    public int confirmPresence(String userId) {
        int code=-1;
        if (userId.equals(proposerId)) {
            if (this.proposerSessionCode <= 0)
                this.proposerSessionCode = generateSessionCode();
            code = this.proposerSessionCode;
        } else if (userId.equals(receiverId)) {
            if (this.receiverSessionCode <= 0)
                this.receiverSessionCode = generateSessionCode();
            code = this.receiverSessionCode;
        }

        return code;
    }

    /**
     * Valida il codice inserito dal negozio per segnare l'arrivo fisico di un
     * partecipante.
     * Se entrambi sono arrivati, aggiorna lo stato a BOTH_ARRIVED.
     * 
     * @param code Il codice fornito dall'utente.
     * @return true se il codice è valido e appartiene a un partecipante, false
     *         altrimenti.
     */
    public boolean acceptSessionCode(int code) {
        if (code <= 0)
            return false;
        if (code == proposerSessionCode) {
            proposerArrived = true;
        } else if (code == receiverSessionCode) {
            receiverArrived = true;
        } else {
            return false;
        }

        if (proposerArrived && receiverArrived) {
            this.tradeStatus = TradeStatus.BOTH_ARRIVED;
        } else {
            this.tradeStatus = TradeStatus.PARTIALLY_ARRIVED;
        }
        return true;
    }

    /**
     * Registra il risultato dell'ispezione delle carte per uno dei partecipanti.
     * Se uno dei due rifiuta (ok=false), lo scambio viene annullato (CANCELLED).
     * Se entrambi accettano, lo stato passa a INSPECTION_PASSED.
     * 
     * @param collectorId ID del collezionista che ha ispezionato.
     * @param ok          Esito dell'ispezione.
     */
    public void markInspectionResult(String collectorId, boolean ok) {
        if (collectorId == null)
            return;
        if (collectorId.equals(proposerId)) {
            this.proposerInspectionOk = ok;
        } else if (collectorId.equals(receiverId)) {
            this.receiverInspectionOk = ok;
        }

        if (Boolean.FALSE.equals(this.proposerInspectionOk) || Boolean.FALSE.equals(this.receiverInspectionOk)) {
            this.tradeStatus = TradeStatus.CANCELLED;
            return;
        }
        this.tradeStatus = TradeStatus.INSPECTION_PASSED;
    }

    public boolean isProposerArrived() {
        return proposerArrived;
    }

    public boolean isReceiverArrived() {
        return receiverArrived;
    }

    public Boolean getProposerInspectionOk() {
        return proposerInspectionOk;
    }

    public Boolean getReceiverInspectionOk() {
        return receiverInspectionOk;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getProposerSessionCode() {
        return proposerSessionCode;
    }

    public int getReceiverSessionCode() {
        return receiverSessionCode;
    }

    public void setSessionId(int transactionId) {
        this.sessionId = transactionId;
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

    public void setProposerSessionCode(int proposerSessionCode) {
        this.proposerSessionCode = proposerSessionCode;
    }

    public void setReceiverSessionCode(int receiverSessionCode) {
        this.receiverSessionCode = receiverSessionCode;
    }

    public void setProposerArrived(boolean proposerArrived) {
        this.proposerArrived = proposerArrived;
    }

    public void setReceiverArrived(boolean receiverArrived) {
        this.receiverArrived = receiverArrived;
    }

}
