package model.domain;

/**
 * Rappresenta una notifica di sistema inviata a un utente.
 */
public class Notification {
    /** ID progressivo della notifica. */
    private int id;
    /** ID dell'utente destinatario della notifica. */
    private String userId;
    /** Il contenuto testuale della notifica. */
    private String message;
    /** Stato di lettura della notifica. */
    private boolean isRead;

    /**
     * Costruttore completo (tipicamente usato per il caricamento dal database).
     * 
     * @param id      ID notifica.
     * @param userId  Destinatario.
     * @param message Messaggio.
     * @param isRead  Stato lettura.
     */
    public Notification(int id, String userId, String message, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
    }

    /**
     * Costruttore rapido per nuove notifiche (impone lo stato non letto).
     * 
     * @param userId  Destinatario.
     * @param message Messaggio.
     */
    public Notification(String userId, String message) {
        this.userId = userId;
        this.message = message;
        this.isRead = false;
    }

    public int getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
