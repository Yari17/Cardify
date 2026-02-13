package controller;

import config.AppConfig;
import javafx.application.Platform;
import model.dao.IBinderDao;
import model.dao.IProposalDao;
import model.dao.ITradeSessionDao;
import model.dao.factory.DaoFactory;
import model.domain.User;
import model.domain.enumerations.PersistenceType;
import model.domain.enumerations.ViewPage;
import model.notification.NotificationService;
import view.*;
import view.factory.ViewFactory;

import java.util.ArrayDeque;
import java.util.Deque;
//application controller rappresenta una violazione della legge di demetra, in quanto un controller applicativo
//non dovrebbe occuparsi di impaginazione
//questo design rappresenta una soluzione seppur modulare non scalabile in quanto non estendibile ad altri ecosistemi
//la soluzione sarebbe delegare al controller grafico la gestione della presentazione evitando il concetto di paginazione
/**
 * Controller principale dell'applicazione (Root Controller).
 * Gestisce il ciclo di vita dell'applicazione, la navigazione tra le diverse
 * viste
 * tramite uno stack di navigazione e coordina l'integrazione tra DAO, Factory e
 * servizi.
 */
public class ApplicationController {
    /**
     * Stack per tenere traccia delle viste caricate e permettere la navigazione a
     * ritroso.
     */
    private final Deque<IView> navigationStack = new ArrayDeque<>();

    /**
     * Factory per la creazione delle viste (CLI o JavaFX).
     */
    private final ViewFactory viewFactory;
    /**
     * Factory per la creazione dei DAO (Demo, JDBC o JSON).
     */
    private final DaoFactory daoFactory;
    /**
     * Servizio centralizzato per la gestione e l'invio delle notifiche.
     */
    private final NotificationService notificationService;

    /**
     * Utente attualmente autenticato nel sistema.
     */
    private User currentUser;
    /**
     * Mappa per il passaggio di dati temporanei tra controller durante la
     * navigazione.
     */
    private final java.util.Map<String, Object> temporaryData = new java.util.HashMap<>();

    /**
     * Inizializza il controller principale configurando le factory e i servizi
     * base.
     * Imposta inoltre la chiusura implicita della piattaforma JavaFX su false per
     * gestire manualmente l'uscita.
     */
    public ApplicationController() {
        this.viewFactory = ViewFactory.getFactory(AppConfig.getUiType());
        this.daoFactory = DaoFactory.getFactory(AppConfig.getPersistenceType());
        this.notificationService = new NotificationService();
        Platform.setImplicitExit(false);
    }

    /**
     * Avvia l'applicazione indirizzando l'utente alla schermata di login.
     */
    public void start() {
        navigateTO(ViewPage.LOGIN);
    }

    /**
     * Gestisce la navigazione verso una specifica pagina dell'applicazione.
     * Si occupa di chiudere la vista precedente, creare la nuova vista e
     * aggiungerla allo stack.
     * Il metodo garantisce l'esecuzione sul thread corretto (JavaFX Application
     * Thread per JAVAFX).
     * Delega la creazione della vista al metodo helper
     * {@link #createViewForPage(ViewPage)}
     * e la chiusura della precedente a {@link #closePreviousView()}.
     *
     * @param page La pagina di destinazione.
     */
    public void navigateTO(ViewPage page) {
        Runnable nav = () -> {
            try {
                closePreviousView();

                // Creazione della vista e del relativo controller associato
                IView newView = createViewForPage(page);

                if (newView != null) {
                    navigationStack.push(newView);
                    newView.display();
                }
            } catch (Exception e) {
                System.err.println("ERRORE CRITICO in navigateTO: " + e.getMessage());

            }
        };

        if (AppConfig.getUiType() == model.domain.enumerations.ViewType.JAVAFX) {
            if (Platform.isFxApplicationThread())
                nav.run();
            else
                Platform.runLater(nav);
        } else {
            // Modalità CLI o altro: esecuzione diretta sul thread corrente
            nav.run();
        }
    }

    /**
     * Chiude la vista attualmente in cima allo stack di navigazione.
     * Gestisce eventuali eccezioni durante la chiusura in modalità "best effort".
     */
    private void closePreviousView() {
        IView previous = navigationStack.peek();
        if (previous != null) {
            try {
                previous.close();
            } catch (Exception _) {
                // Tenta la chiusura senza interrompere il flusso
            }
        }
    }

    /**
     * Factory interna per la creazione dinamica delle viste e dei loro controller.
     *
     * @param page La pagina da istanziare.
     * @return L'istanza della vista pronta per l'uso.
     * @throws IllegalArgumentException Se la pagina richiesta non è gestita.
     */
    private IView createViewForPage(ViewPage page) {
        IView newView;
        switch (page) {
            case LOGIN -> newView = createLoginView();
            case COLLECTOR_HOMEPAGE -> newView = createCollectorHPView();
            case STORE_HOMEPAGE -> newView = createStoreHPView();
            case STORE_TRADE -> newView = createStoreTradeView();
            case COLLECTION -> newView = createCollectionView();
            case COLLECTOR_TRADE -> newView = createCollectorTradeView();
            case PROPOSAL -> newView = createProposalView();
            case MANAGE_PROPOSAL -> newView = createManageProposalsView();
            case NOTIFICATIONS -> newView = createNotificationsView();
            default -> throw new IllegalArgumentException("Pagina sconosciuta: " + page);
        }
        return newView;
    }

    /**
     * Crea la vista di login configurando il controller associato.
     */
    private IView createLoginView() {
        LoginController loginController = new LoginController(this, daoFactory.createUserDao());
        ILoginView loginView = viewFactory.getLoginView();
        loginView.setController(loginController);
        loginController.setView(loginView);
        return loginView;
    }

    /**
     * Crea la homepage del collezionista con le relative dipendenze.
     */
    private IView createCollectorHPView() {
        model.dao.IProposalDao proposalDao = daoFactory.createProposalDao();
        CollectorHPController collectorController = new CollectorHPController(this, currentUser,
                daoFactory.createBinderDao(),
                daoFactory.createUserDao(), proposalDao);
        IView collectorView = viewFactory.getCollectorHPView();
        collectorView.setController(collectorController);
        collectorController.setView(collectorView);
        return collectorView;
    }

    /**
     * Crea la homepage del negozio.
     */
    private IView createStoreHPView() {
        StoreHPController storeController = new StoreHPController(this, currentUser);
        IView storeView = viewFactory.getStoreHPView();
        storeView.setController(storeController);
        storeController.setView(storeView);
        return storeView;
    }

    /**
     * Crea la vista di gestione scambi per il negozio.
     */
    private IView createStoreTradeView() {
        model.dao.ITradeSessionDao tradeDao = daoFactory.createTradeDao();
        TradeController storeTradeController = new TradeController(this, currentUser, tradeDao,
                daoFactory.createBinderDao());
        // Registra NotificationService come observer per le notifiche in tempo reale
        storeTradeController.attach(notificationService);
        IStoreTradeView tradeView = viewFactory.getStoreTradeView();
        tradeView.setController(storeTradeController);
        storeTradeController.setView(tradeView);

        // Se è presente un ID sessione nei dati temporanei, apri direttamente i
        // dettagli
        Object sessionIdObj = getTemporaryData("TRADE_SESSION_ID");
        if (sessionIdObj instanceof Integer sessionId) {
            storeTradeController.openTradeDetails(sessionId);
        }
        return tradeView;
    }

    /**
     * Crea la vista della collezione dell'utente.
     */
    private IView createCollectionView() {
        // Selezione dinamica della persistenza specifica per i Binder (JSON o JDBC)
        // Fatta per realizzare un caso d'uso che funzioni con due persistense intercambiabili a runtime
        PersistenceType binderType = config.AppConfig
                .getBinderPersistenceType();
        DaoFactory binderFactory = DaoFactory
                .getFactory(binderType);
        IBinderDao specificBinderDao = binderFactory.createBinderDao();

        CollectionController collectionController = new CollectionController(this, currentUser,
                specificBinderDao);
        IView collectionView = viewFactory.getCollectionView();
        collectionView.setController(collectionController);
        collectionController.setView(collectionView);
        return collectionView;
    }

    /**
     * Crea la vista scambi per il collezionista.
     */
    private IView createCollectorTradeView() {
        ITradeSessionDao tradeDao = daoFactory.createTradeDao();
        TradeController tradeController = new TradeController(this, currentUser, tradeDao,
                daoFactory.createBinderDao());
        // Abilita le notifiche osservando il controller
        tradeController.attach(notificationService);
        ICollectorTradeView tradeView = viewFactory.getCollectorTradeView();
        tradeView.setController(tradeController);
        tradeController.setView(tradeView);
        return tradeView;
    }

    /**
     * Crea la vista per formulare una nuova proposta di scambio.
     */
    private IView createProposalView() {
        IProposalDao proposalDao = daoFactory.createProposalDao();
        IBinderDao binderDao = daoFactory.createBinderDao();

        TradeProposalController tradeProposalController = new TradeProposalController(this, currentUser,
                daoFactory.createUserDao(), proposalDao, binderDao);
        tradeProposalController.attach(notificationService);

        // Inizializza la proposta se è stata selezionata una carta target
        Object targetCardObj = getTemporaryData("PROPOSAL_TARGET_CARD");
        if (targetCardObj instanceof model.bean.CardBean cardBean) {
            tradeProposalController.initProposal(cardBean);
            setTemporaryData("PROPOSAL_TARGET_CARD", null); // Pulisce dopo l'uso
        }

        ITradeProposalView proposalView = viewFactory.getTradeProposalView();
        proposalView.setController(tradeProposalController);
        tradeProposalController.setView(proposalView);
        return proposalView;
    }

    /**
     * Crea la vista per la gestione delle proposte ricevute/inviate.
     */
    private IView createManageProposalsView() {
        IProposalDao proposalDao = daoFactory.createProposalDao();
        ITradeSessionDao tradeSessionDao = daoFactory.createTradeDao();
        ManageProposalsController manageProposalsController = new ManageProposalsController(this,
                currentUser, proposalDao, tradeSessionDao);
        manageProposalsController.attach(notificationService);
        IView manageProposalsView = viewFactory.getManageProposalsView();
        manageProposalsView.setController(manageProposalsController);
        manageProposalsController.setView(manageProposalsView);
        return manageProposalsView;
    }

    /**
     * Crea la vista per la visualizzazione delle notifiche.
     */
    private IView createNotificationsView() {
        NotificationsController notificationsController = new NotificationsController(
                currentUser);
        view.INotificationsView notificationsView = viewFactory.getNotificationsView();
        notificationsView.setController(notificationsController);
        notificationsController.setView(notificationsView);
        return notificationsView;
    }

    /**
     * Mostra l'overlay delle notifiche come finestra pop-up.
     * Gestisce il thread-safety della piattaforma grafica per visualizzare le
     * notifiche in tempo reale.
     */
    public void showNotificationOverlay() {
        Runnable show = () -> {
            NotificationsController notificationsController = new NotificationsController(currentUser);
            INotificationsView notificationsView = viewFactory.getNotificationsView();
            notificationsView.setController(notificationsController);
            notificationsController.setView(notificationsView);
            notificationsView.display();
        };

        if (AppConfig.getUiType() == model.domain.enumerations.ViewType.JAVAFX) {
            if (Platform.isFxApplicationThread())
                show.run();
            else
                Platform.runLater(show);
        } else {
            show.run();
        }
    }

    /**
     * Imposta l'utente correntemente loggato nel sistema.
     *
     * @param user L'oggetto User autenticato.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Salva un oggetto nei dati temporanei per il passaggio tra viste.
     *
     * @param key   Identificatore del dato.
     * @param value Valore da memorizzare.
     */
    public void setTemporaryData(String key, Object value) {
        this.temporaryData.put(key, value);
    }

    /**
     * Recupera un dato temporaneo salvato in precedenza.
     *
     * @param key Identificatore del dato.
     * @return L'oggetto memorizzato o {@code null}.
     */
    public Object getTemporaryData(String key) {
        return this.temporaryData.get(key);
    }

    /**
     * Esegue la chiusura della sessione utente.
     * Svuota lo stack di navigazione chiudendo tutte le finestre aperte e riporta
     * l'utente alla schermata di login.
     */
    public void logout() {
        this.currentUser = null;

        // chiude tutte le viste nello stack in ordine LIFO
        while (!navigationStack.isEmpty()) {
            IView view = navigationStack.pop();
            try {
                view.close();
            } catch (Exception _) {
                // Procedi nonostante eventuali errori di chiusura finestra
            }
        }
        navigateTO(ViewPage.LOGIN);
    }
}
