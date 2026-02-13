package model.dao;

import model.domain.Binder;

import java.util.List;

/**
 * Interfaccia per la gestione dei raccoglitori (Binder) di carte.
 * Fornisce metodi per recuperare e manipolare le collezioni degli utenti
 * suddivise per set.
 */
public interface IBinderDao {

    /**
     * Recupera tutti i raccoglitori appartenenti a un utente specifico.
     * 
     * @param owner Nome utente del proprietario.
     * @return Lista di raccoglitori {@link Binder}.
     */
    List<Binder> getUserBinders(String owner);

    /**
     * Recupera tutti i raccoglitori presenti nel sistema, escludendo quelli di un
     * utente.
     * Utile per la ricerca di carte da altri collezionisti.
     * 
     * @param owner Nome utente da escludere.
     * @return Lista di raccoglitori altrui.
     */
    List<Binder> getBindersExcludingOwner(String owner);

    /**
     * Recupera un raccoglitore specifico identificato da proprietario e set.
     * 
     * @param owner Proprietario del raccoglitore.
     * @param setId ID del set di carte.
     * @return Il raccoglitore trovato o null.
     */
    Binder getBinderByOwnerAndSet(String owner, String setId);

    /**
     * Cerca un raccoglitore esistente o lo crea se non presente.
     * 
     * @param owner   Proprietario.
     * @param setId   ID del set.
     * @param setName Nome del set (utilizzato in caso di creazione).
     * @return Il raccoglitore (nuovo o esistente).
     */
    Binder findOrCreateBinder(String owner, String setId, String setName);

    /**
     * Crea un nuovo raccoglitore per un utente.
     * 
     * @param owner  Proprietario.
     * @param binder L'oggetto {@link Binder} da persistere.
     */
    void createBinder(String owner, Binder binder);

    /**
     * Elimina un raccoglitore esistente.
     * 
     * @param owner  Proprietario.
     * @param binder L'oggetto {@link Binder} da rimuovere.
     */
    void deleteBinder(String owner, Binder binder);

    /**
     * Salva le modifiche apportate a un raccoglitore (es. aggiornamento quantità
     * carte).
     * 
     * @param binder Il raccoglitore con i dati aggiornati.
     */
    void save(Binder binder);

}
