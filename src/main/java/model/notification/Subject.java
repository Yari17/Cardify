package model.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta che implementa il ruolo di "Soggetto" nel pattern Observer.
 * Fornisce l'infrastruttura necessaria per gestire una lista di
 * {@link Observer} registrati
 * e per propagare loro gli eventi di notifica. Le classi di business (es. i
 * Controller)
 * estendono questa classe per diventare sorgenti di eventi.
 */
public abstract class Subject {
    /** Lista degli osservatori correntemente registrati per questo soggetto. */
    private final List<Observer> observers = new ArrayList<>();

    /**
     * Registra un nuovo osservatore interessato alle notifiche di questo soggetto.
     * 
     * @param observer L'istanza dell'osservatore da aggiungere alla lista.
     */
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     * 
     * @param observer L'osservatore da disiscrivere dalle notifiche.
     */
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Propaga un evento di notifica a tutti gli osservatori registrati.
     * Questo metodo è protetto in quanto deve essere invocato dalle classi derivate
     * quando si verifica un cambio di stato rilevante.
     * 
     * @param event L'evento di notifica da inviare agli osservatori.
     */
    protected void notifyObservers(NotificationEvent event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }
}
