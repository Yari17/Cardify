package controller;

import model.bean.BinderBean;
import model.bean.mapper.BinderMapper;
import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.Card;
import model.domain.CollectionItem;
import model.domain.User;
import model.domain.enumerations.CardGameType;
import model.api.ApiFactory;
import model.api.ICardProvider;
import view.ICollectionView;
import view.IView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller responsabile della gestione della collezione di carte dell'utente.
 * Coordina le operazioni sui binder (creazione, eliminazione, modifica) e
 * l'interazione
 * con le API esterne per il recupero delle informazioni sulle carte.
 */
public class CollectionController {
    /** Vista associata per la visualizzazione della collezione. */
    ICollectionView collectionView;
    /** Controller principale per la gestione della navigazione. */
    private final ApplicationController appController;
    /** Utente proprietario della collezione. */
    private final User currentUser;
    /** DAO per la persistenza dei binder. */
    private IBinderDao binderDao;
    /** Factory per l'accesso ai provider di carte basati su API. */
    private final ApiFactory apiFactory;

    /** Stato dei binder caricati per la gestione del salvataggio batch. */
    private List<Binder> sessionBinders;
    /**
     * Flag che indica se sono presenti modifiche non ancora persistite sul
     * database.
     */
    private boolean hasUnsavedChanges = false;

    /**
     * Costruttore del controller della collezione.
     * 
     * @param applicationController Il controller radice per la navigazione.
     * @param currentUser           L'utente autenticato.
     * @param binderDao             Il DAO iniziale per l'accesso ai binder.
     */
    public CollectionController(ApplicationController applicationController, User currentUser, IBinderDao binderDao) {
        this.appController = applicationController;
        this.currentUser = currentUser;
        this.binderDao = binderDao;
        this.apiFactory = new ApiFactory();
    }

    /**
     * Cambia dinamicamente il tipo di persistenza per i binder (es. da JDBC a
     * JSON).
     * Re-inizializza il DAO, svuota la sessione corrente e ricarica i dati.
     * Delega il ricaricamento a {@link #loadUserCollection()}.
     * 
     * @param type Il nuovo tipo di persistenza selezionato.
     */
    public void switchPersistence(model.domain.enumerations.PersistenceType type) {
        config.AppConfig.setBinderPersistenceType(type);
        // Ricrea il DAO basandosi sul nuovo tipo di persistenza
        model.dao.factory.DaoFactory factory = model.dao.factory.DaoFactory.getFactory(type);
        this.binderDao = factory.createBinderDao();

        // Resetta lo stato della sessione e ricarica i dati dal nuovo storage
        this.sessionBinders = null;
        loadUserCollection();
    }

    /**
     * Associa la vista al controller.
     * 
     * @param collectionView L'istanza della vista collezione.
     */
    public void setView(IView collectionView) {
        this.collectionView = (ICollectionView) collectionView;
    }

    /**
     * Restituisce il controller di applicazione principale.
     * 
     * @return L'istanza di ApplicationController.
     */
    public ApplicationController getAppController() {
        return appController;
    }

    /**
     * Restituisce l'utente proprietario della collezione corrente.
     * 
     * @return L'oggetto User della sessione.
     */
    public User getSessionUser() {
        return currentUser;
    }

    /**
     * Carica i binder dell'utente e aggiorna la visualizzazione.
     * Implementa una logica di caching in memoria: carica dal DAO solo se lo stato
     * di sessione è vuoto, altrimenti utilizza i dati già presenti per mantenere
     * le modifiche non salvate.
     */
    public void loadUserCollection() {
        // Carica dal DAO solo se la sessione è vuota (primo caricamento o reset
        // richiesto)
        if (sessionBinders == null) {
            sessionBinders = binderDao.getUserBinders(currentUser.getUsername());
            hasUnsavedChanges = false;
            if (collectionView != null) {
                collectionView.setSaveButtonVisible(false);
            }
        }

        List<BinderBean> returnBinders = new ArrayList<>();
        for (Binder binder : sessionBinders) {
            returnBinders.add(BinderMapper.toBean(binder));
        }
        collectionView.displayUserBinders(returnBinders);
    }

    /**
     * Prepara il processo di creazione di un nuovo binder recuperando i set
     * disponibili.
     * Il controller recupera i dati e li spinge alla vista.
     */
    public void prepareCreateBinder() {
        Map<String, String> sets = getAvailableSets(CardGameType.POKEMON);
        if (collectionView != null) {
            collectionView.showAvailableSets(sets);
        }
    }

    /**
     * Crea un nuovo binder per un determinato set.
     * Verifica preventivamente se il set è già presente nella collezione
     * dell'utente.
     * 
     * @param setID   ID univoco del set di carte.
     * @param setName Nome descrittivo del set.
     */
    public void createNewBinder(String setID, String setName) {
        // Assicura che la sessione sia inizializzata prima del controllo duplicati
        if (sessionBinders == null)
            sessionBinders = binderDao.getUserBinders(currentUser.getUsername());

        for (Binder binder : sessionBinders) {
            if (binder.getSetID().equals(setID)) {
                collectionView.showError("Set già presente nella collezione.");
                return;
            }
        }
        Binder newBinder = new Binder(currentUser.getUsername(), setID, setName, new ArrayList<>());
        binderDao.createBinder(currentUser.getUsername(), newBinder);
        // Forza il ricaricamento per includere il nuovo binder nello stato della
        // sessione
        sessionBinders = null;
    }

    /**
     * Elimina un binder esistente e ricarica la visualizzazione.
     * 
     * @param setID ID del set associato al binder da rimuovere.
     */
    public void deleteBinder(String setID) {
        if (sessionBinders == null)
            sessionBinders = binderDao.getUserBinders(currentUser.getUsername());

        sessionBinders.stream()
                .filter(b -> b.getSetID().equals(setID))
                .findFirst()
                .ifPresent(binderToDelete -> {
                    binderDao.deleteBinder(currentUser.getUsername(), binderToDelete);
                    sessionBinders = null; // Forza ricaricamento al prossimo accesso
                    loadUserCollection();
                });
    }

    /**
     * Recupera l'elenco completo delle carte per un binder, unendo quelle possedute
     * con quelle mancanti (recuperate via API).
     * 
     * @param binder Il bean del binder selezionato.
     * @return Una lista di {@link model.bean.CardBean} che rappresenta l'intero
     *         set.
     */
    public List<model.bean.CardBean> getCompleteBinderCards(BinderBean binder) {
        // 1. Recupera tutte le carte del set dall'API esterna
        ICardProvider provider = apiFactory.getCardProvider(CardGameType.POKEMON);
        List<Card> allSetCards = provider.getCardsBySet(binder.getSetID());

        // 2. Crea una mappa delle carte possedute per un accesso efficiente (O(1))
        java.util.Map<String, CollectionItem> ownedMap = new java.util.HashMap<>();
        if (binder.getOwnedCards() != null) {
            for (CollectionItem item : binder.getOwnedCards()) {
                if (item.getCard() != null && item.getCard().getCardID() != null) {
                    ownedMap.put(item.getCard().getCardID(), item);
                }
            }
        }

        // 3. Elabora la lista completa trasformando le entità in Bean
        List<model.bean.CardBean> completeList = new ArrayList<>();
        for (Card card : allSetCards) {
            model.bean.CardBean bean;
            if (ownedMap.containsKey(card.getCardID())) {
                bean = model.bean.mapper.CardMapper.toBean(ownedMap.get(card.getCardID()));
            } else {
                // Per le carte non possedute, crea un bean con quantità zero
                bean = model.bean.mapper.CardMapper.toBean(card);
                bean.setQuantity(0);
            }
            bean.setOwner(binder.getOwner());
            completeList.add(bean);
        }

        return completeList;
    }

    /**
     * Interroga il provider API per ottenere l'elenco dei set disponibili.
     * 
     * @param gameType Il tipo di gioco (es. POKEMON).
     * @return Una mappa che associa gli ID dei set ai loro nomi descrittivi.
     */
    public Map<String, String> getAvailableSets(CardGameType gameType) {
        ICardProvider provider = apiFactory.getCardProvider(gameType);
        return provider.getSetNameList();
    }

    /**
     * Aggiunge una carta al binder specificato (modifica solo lo stato in memoria).
     * Delega l'aggiunta logica a {@link #addCardToSpecificBinder(Binder, String)}.
     * 
     * @param setId  ID del set (binder) di destinazione.
     * @param cardId ID della carta da aggiungere.
     */
    public void addCardToBinder(String setId, String cardId) {
        if (sessionBinders == null)
            loadUserCollection();

        for (Binder binder : sessionBinders) {
            if (binder.getSetID().equals(setId)) {
                addCardToSpecificBinder(binder, cardId);
                return;
            }
        }
    }

    /**
     * Esegue l'aggiunta materiale di una carta a un oggetto Binder.
     * Attiva il pulsante di salvataggio nella vista per segnalare modifiche
     * pendenti.
     */
    private void addCardToSpecificBinder(Binder binder, String cardId) {
        ICardProvider provider = apiFactory.getCardProvider(CardGameType.POKEMON);
        List<Card> allCards = provider.getCardsBySet(binder.getSetID());
        for (Card card : allCards) {
            if (card.getCardID().equals(cardId)) {
                binder.addCard(card);
                hasUnsavedChanges = true;
                if (collectionView != null) {
                    collectionView.setSaveButtonVisible(true);
                    collectionView.onAddCard(model.bean.mapper.CardMapper.toBean(card));
                }
                return;
            }
        }
    }

    /**
     * Rimuove una carta dal binder specificato (modifica solo lo stato in memoria).
     * Delega la rimozione logica a
     * {@link #removeCardFromSpecificBinder(Binder, String)}.
     * 
     * @param setId  ID del set di appartenenza.
     * @param cardId ID della carta da rimuovere.
     */
    public void removeCardFromBinder(String setId, String cardId) {
        if (sessionBinders == null)
            loadUserCollection();

        for (Binder binder : sessionBinders) {
            if (binder.getSetID().equals(setId)) {
                removeCardFromSpecificBinder(binder, cardId);
                return;
            }
        }
    }

    /**
     * Esegue la rimozione materiale di una carta da un oggetto Binder.
     * Attiva il pulsante di salvataggio nella vista.
     */
    private void removeCardFromSpecificBinder(Binder binder, String cardId) {
        ICardProvider provider = apiFactory.getCardProvider(CardGameType.POKEMON);
        List<Card> allCards = provider.getCardsBySet(binder.getSetID());
        for (Card card : allCards) {
            if (card.getCardID().equals(cardId)) {
                binder.removeCard(card);
                hasUnsavedChanges = true;
                if (collectionView != null) {
                    collectionView.setSaveButtonVisible(true);
                    collectionView.onRemoveCard(model.bean.mapper.CardMapper.toBean(card));
                }
                return;
            }
        }
    }

    /**
     * Persiste tutte le modifiche pendenti nello storage selezionato tramite il
     * DAO.
     * Dopo il salvataggio, forza il ricaricamento dei dati per sincronizzare lo
     * stato
     * con il database (es. corretta gestione degli item con quantità zero).
     */
    public void saveChanges() {
        if (!hasUnsavedChanges || sessionBinders == null)
            return;

        for (Binder binder : sessionBinders) {
            binderDao.save(binder);
        }

        hasUnsavedChanges = false;
        if (collectionView != null)
            collectionView.setSaveButtonVisible(false);

        // Reset della sessione per forzare il ricaricamento dai dati persistiti
        sessionBinders = null;
        loadUserCollection();
    }
}
