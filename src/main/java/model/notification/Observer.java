package model.notification;

/**
 * Interfaccia che definisce il contratto per i componenti osservatori nel
 * sistema di notifiche.
 * 
 * Qualsiasi classe che desideri ricevere aggiornamenti riguardo ad eventi di
 * business (es. proposte,
 * arrivi in negozio) deve implementare questa interfaccia. Segue il pattern
 * Observer per
 * garantire il disaccoppiamento tra la sorgente dell'evento (Subject) e chi lo
 * processa.
 */
public interface Observer {
    /**
     * Metodo invocato dal {@link Subject} per notificare il verificarsi di un
     * evento.
     * 
     * @param event L'oggetto {@link NotificationEvent} contenente i dettagli della
     *              notifica.
     */
    void update(NotificationEvent event);
}
