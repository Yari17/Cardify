package model.bean;

/**
 * Bean per il trasporto dei dati di una notifica.
 * Utilizzato per visualizzare avvisi all'utente riguardanti scambi o messaggi
 * di sistema.
 */
public class NotificationBean {
    /** Identificativo unico della notifica. */
    private int id;
    /** ID dell'utente destinatario. */
    private String userId;
    /** Testo del messaggio di notifica. */
    private String message;
    /** Stato di lettura della notifica. */
    private boolean isRead;

    /**
     * Non è necessaria logica di inizializzazione poiché i campi vengono popolati
     * tramite setter.
     */
    public NotificationBean() {
        // costruttore vuoto
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
