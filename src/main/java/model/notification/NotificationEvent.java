package model.notification;

/**
 * Classe astratta che definisce il contratto per tutti gli eventi di notifica
 * del sistema.
 * 
 * Implementa il principio GRASP **Information Expert**: ogni sottoclasse
 * concreta è responsabile
 * di conoscere gli attori coinvolti (destinatario) e di formattare il messaggio
 * specifico per l'evento.
 * Questo permette all'osservatore di processare l'evento in modo polimorfico.
 */
public abstract class NotificationEvent {
    /**
     * Restituisce l'identificativo (username) del destinatario principale della
     * notifica.
     * 
     * @return Lo username del destinatario.
     */
    public abstract String getRecipientUsername();

    /**
     * Genera e restituisce il testo formattato del messaggio di notifica.
     * 
     * @return Il testo del messaggio pronto per essere mostrato o archiviato.
     */
    public abstract String getMessage();
}
