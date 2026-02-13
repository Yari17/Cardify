package controller;

import model.dao.IProposalDao;
import model.dao.IUserDao;
import model.domain.User;
import model.domain.enumerations.ViewPage;
import model.notification.NotificationService;
import view.ICollectorHPView;
import view.IView;
import model.bean.CardBean;
import java.util.List;
import java.util.ArrayList;
import model.dao.IBinderDao;
import model.domain.Binder;
import java.util.Map;
import model.api.ApiFactory;
import model.api.ICardProvider;
import model.domain.enumerations.CardGameType;

/**
 * Controller per la Homepage del Collezionista.
 * Gestisce la visualizzazione della community, la ricerca di carte,
 * i dettagli delle carte e coordina la navigazione verso altre sezioni.
 */
public class CollectorHPController {
    /** Controller principale per la gestione della navigazione e degli overlay. */
    ApplicationController applicationController;
    /** Vista associata alla homepage del collezionista. */
    ICollectorHPView view;
    /** Utente della sessione corrente. */
    User sessionUser;
    /** DAO per l'accesso ai binder degli utenti. */
    IBinderDao binderDao;
    /** DAO per l'accesso ai dati degli utenti (es. per recuperare i negozi). */
    IUserDao userDao;
    /** DAO per la gestione delle proposte di scambio. */
    IProposalDao proposalDao;
    /** Factory per la creazione dei provider di carte API. */
    ApiFactory apiFactory;
    /** Provider specifico per il recupero dati dalle API (es. Pokemon). */
    ICardProvider provider;
    /** Servizio per il controllo e recupero delle notifiche pendenti. */
    NotificationService notificationService;

    /**
     * Costruttore del controller.
     * Inizializza i componenti e avvia il controllo asincrono delle notifiche
     * pendenti.
     * Delega il controllo delle notifiche a {@link #checkNotifications()}.
     */
    public CollectorHPController(ApplicationController applicationController, User sessionUser,
                                 IBinderDao binderDao, IUserDao userDao, IProposalDao proposalDao) {
        this.applicationController = applicationController;
        this.sessionUser = sessionUser;
        this.binderDao = binderDao;
        this.userDao = userDao;
        this.proposalDao = proposalDao;
        this.apiFactory = new ApiFactory();
        this.provider = this.apiFactory.getCardProvider(CardGameType.POKEMON);
        this.notificationService = new NotificationService();

        // Avvio controllo notifiche basato sul tipo di UI
        if (config.AppConfig.getUiType() == model.domain.enumerations.ViewType.JAVAFX) {
            javafx.application.Platform.runLater(this::checkNotifications);
        } else {
            // In ambiente CLI, il controllo è immediato
            checkNotifications();
        }
    }

    /**
     * Verifica la presenza di notifiche non lette per l'utente corrente.
     * Se presenti, delega al controller principale la visualizzazione dell'overlay
     * di notifica.
     */
    private void checkNotifications() {
        if (sessionUser == null)
            return;
        List<model.domain.Notification> notifications = notificationService.getUnreadNotifications(sessionUser);
        if (!notifications.isEmpty()) {
            applicationController.showNotificationOverlay();
        }
    }

    /**
     * Collega la vista al controller.
     * 
     * @param collectorView L'istanza della vista homepage.
     */
    public void setView(IView collectorView) {
        this.view = (ICollectorHPView) collectorView;
    }

    /**
     * Carica l'elenco delle carte disponibili nella community (escludendo quelle
     * dell'utente).
     * Itera su tutti i binder pubblici e mappa gli item in {@link CardBean}.
     *
     * @return Una lista di bean rappresentanti le carte degli altri collezionisti.
     */
    public List<CardBean> loadCommunityCards() {
        if (binderDao == null || sessionUser == null) {
            return new ArrayList<>();
        }

        List<Binder> binders = binderDao.getBindersExcludingOwner(sessionUser.getUsername());
        List<CardBean> communityCards = new ArrayList<>();

        for (model.domain.Binder binder : binders) {
            for (model.domain.CollectionItem item : binder.getOwnedCards()) {
                CardBean cardBean = model.bean.mapper.CardMapper.toBean(item);
                cardBean.setOwner(binder.getOwner());
                communityCards.add(cardBean);
            }
        }

        return communityCards;
    }

    /**
     * Carica i set disponibili tramite il provider API.
     * 
     * @return Mappa ID set -> Nome set.
     */
    public Map<String, String> loadAvailableSets() {
        return provider.getSetNameList();
    }

    /**
     * Lookup inverso: trova il codice del set dato il nome.
     * Metodo helper per disaccoppiare View dalla logica di iterazione Map.
     */
    public String getSetCodeByName(String setName) {
        if (setName == null || setName.isEmpty()) {
            return null;
        }
        Map<String, String> sets = loadAvailableSets();
        for (Map.Entry<String, String> entry : sets.entrySet()) {
            if (entry.getValue().equals(setName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Esegue una ricerca filtrata delle carte nella community.
     * 
     * @param cardName Nome parziale della carta.
     * @param setCode  Codice univoco del set (opzionale).
     * @return Lista di {@link CardBean} corrispondenti ai criteri.
     */
    public List<CardBean> searchCards(String cardName, String setCode) {
        if (binderDao == null || sessionUser == null) {
            return new ArrayList<>();
        }

        List<Binder> binders = binderDao.getBindersExcludingOwner(sessionUser.getUsername());
        List<CardBean> results = new ArrayList<>();

        for (Binder binder : binders) {
            // Delega la logica di filtraggio e raccolta ai metodi helper
            if (shouldIncludeBinder(binder, setCode)) {
                collectMatchingCards(binder, cardName, results);
            }
        }
        return results;
    }

    /**
     * Esegue la ricerca recuperando i filtri direttamente dalla vista (Pull Model).
     * Questo metodo valida l'uso dei metodi getCardNameFilter() e getSetFilter()
     * dell'interfaccia.
     * 
     * @return La lista dei risultati trovati.
     */
    public List<CardBean> performSearch() {
        if (view == null) {
            return new ArrayList<>();
        }
        String cardName = view.getCardNameFilter();
        String setCode = view.getSetFilter();
        List<CardBean> results = searchCards(cardName, setCode);
        view.displayCardList(results);
        return results;
    }

    /**
     * Verifica se un binder deve essere incluso nella ricerca basandosi sul
     * setCode.
     */
    private boolean shouldIncludeBinder(Binder binder, String setCode) {
        return setCode == null || setCode.isEmpty() || binder.getSetID().equals(setCode);
    }

    private void collectMatchingCards(Binder binder, String cardName, List<CardBean> results) {
        for (model.domain.CollectionItem item : binder.getOwnedCards()) {
            model.domain.Card card = item.getCard();
            if (card != null && shouldIncludeCard(card, cardName)) {
                model.bean.CardBean cardBean = model.bean.mapper.CardMapper.toBean(card);
                cardBean.setQuantity(item.getQuantity());
                cardBean.setOwner(binder.getOwner());
                results.add(cardBean);
            }
        }
    }

    /**
     * Verifica se una carta corrisponde al nome cercato (case-insensitive).
     */
    private boolean shouldIncludeCard(model.domain.Card card, String cardNameSearch) {
        if (cardNameSearch == null || cardNameSearch.isEmpty()) {
            return true;
        }
        String cName = card.getCardName();
        return cName != null && cName.toLowerCase().contains(cardNameSearch.toLowerCase());
    }

    /**
     * Recupera dettagli approfonditi per una specifica carta tramite le API
     * esterne.
     * Arricchisce il bean fornito con le informazioni testuali (HP, debolezze,
     * ecc.).
     * 
     * @param cardBean Il bean sintetico della carta.
     * @return Il bean arricchito con i dettagli dell'API.
     */
    public CardBean getCardDetails(CardBean cardBean) {
        if (cardBean == null || provider == null) {
            return cardBean;
        }

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(getClass().getName());
        logger.info(() -> "Richiesta dettagli per carta: " + cardBean.getName() + " (ID: " + cardBean.getId()
                + ", SetID: " + cardBean.getSetId() + ")");

        try {
            // Crea un'entità di dominio temporanea per l'interazione con il provider
            model.domain.Card card = new model.domain.Card(
                    cardBean.getName(),
                    cardBean.getId(),
                    cardBean.getSetId(),
                    cardBean.getImageUrl(),
                    cardBean.getGameType());

            // Arricchimento via API
            model.domain.Card enrichedCard = provider.getCardDetails(card);
            if (enrichedCard != null && enrichedCard.getDetails() != null) {
                cardBean.setDetails(enrichedCard.getDetails());
                logger.info(() -> "Dettagli recuperati con successo dall'API per: " + cardBean.getName());
            } else {
                logger.warning(() -> "Dettagli NON disponibili dall'API per: " + cardBean.getName());
            }
        } catch (Exception e) {
            logger.warning(
                    () -> "Errore durante il recupero dettagli per " + cardBean.getName() + ": " + e.getMessage());
        }
        return cardBean;
    }

    /**
     * Gestisce l'apertura della vista di dettaglio per una carta.
     * Recupera prima i dati necessari tramite {@link #getCardDetails(CardBean)}
     * e poi aggiorna la vista.
     * 
     * @param card Il bean della carta da mostrare.
     */
    public void openCardDetails(CardBean card) {
        if (card == null)
            return;
        CardBean detailedCard = getCardDetails(card);
        if (view != null) {
            view.displayCardOverview(detailedCard);
        }
    }

    // Getter minimi per esporre le informazioni richieste alla vista senza rendere
    // pubblici i campi
    /** @return Il controller principale. */
    public ApplicationController getApplicationController() {
        return this.applicationController;
    }

    /** @return L'utente della sessione. */
    public User getSessionUser() {
        return this.sessionUser;
    }

    /**
     * Recupera l'elenco di tutti gli utenti di tipo Negozio.
     * 
     * @return Lista di oggetti User rappresentanti i negozi.
     */
    public List<User> getStores() {
        if (userDao != null) {
            return userDao.getStores();
        }
        return new ArrayList<>();
    }

    // Metodi di navigazione minimi utilizzati dalla navbar — delega al
    // ApplicationController
    /** Naviga alla homepage. */
    public void goToHomepage() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTOR_HOMEPAGE);
    }

    /** Naviga alla gestione collezione. */
    public void goToCollection() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTION);
    }

    /** Naviga alla sezione scambi. */
    public void goToTrade() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTOR_TRADE);
    }

    /** Naviga alla gestione proposte. */
    public void manageProposals() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.MANAGE_PROPOSAL);
    }

    /** Esegue il logout. */
    public void logout() {
        if (applicationController != null) {
            applicationController.logout();
        }
    }

    /**
     * Recupera la collezione completa dell'utente corrente operando su tutti i suoi
     * binder.
     * 
     * @return Lista di {@link CardBean} posseduti dall'utente.
     */
    public List<CardBean> getUserCollection() {
        if (binderDao == null || sessionUser == null) {
            return new ArrayList<>();
        }

        List<Binder> userBinders = binderDao.getUserBinders(sessionUser.getUsername());
        List<CardBean> myCards = new ArrayList<>();

        for (Binder binder : userBinders) {
            for (model.domain.CollectionItem item : binder.getOwnedCards()) {
                CardBean cardBean = model.bean.mapper.CardMapper.toBean(item);
                cardBean.setOwner(binder.getOwner());
                myCards.add(cardBean);
            }
        }
        return myCards;
    }
}
