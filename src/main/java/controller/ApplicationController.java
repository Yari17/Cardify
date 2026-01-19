package controller;

import exception.NavigationException;
import javafx.application.Platform;
import model.api.ICardProvider;
import model.bean.UserBean;
import model.dao.IBinderDao;
import model.dao.IUserDao;
import model.dao.factory.DaoFactory;
import config.InputManager;
import model.domain.enumerations.PersistenceType;
import view.*;
import view.factory.CliIViewFactory;
import view.factory.FXViewFactory;
import view.factory.IViewFactory;
import view.IRegistrationView;
import view.IStoreHPView;
import view.ICollectorTradeView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

public class ApplicationController {
    private static final Logger LOGGER = Logger.getLogger(ApplicationController.class.getName());
    private static final String JAVAFX = "JavaFX";
    private static final String NULL = "<null>";
    private final Deque<IView> viewStack = new ArrayDeque<>();

    private String currentInterface;
    private String currentPersistence;
    private boolean startedInDemoMode = false;
    private String currentGameType = config.AppConfig.POKEMON_GAME;

    private DaoFactory daoFactory;

    public DaoFactory getDaoFactory() {
        return daoFactory;
    }

    private IViewFactory viewFactory;

    private IUserDao userDao;

    private model.api.ICardProvider cardProvider;

    public void start() {
        InputManager inputManager = new InputManager();
        ConfigurationManager config = new ConfigurationManager(inputManager);
        currentInterface = config.chooseInterface();
        currentPersistence = config.choosePersistence();
        // Remember if the application was started in demo mode so logout can restore demo-only login
        startedInDemoMode = "DEMO".equalsIgnoreCase(currentPersistence);


        updateAppConfigPersistence();


        daoFactory = createDaoFactory();


        userDao = daoFactory.createUserDao();
        viewFactory = createViewFactory(inputManager);

        if (JAVAFX.equals(currentInterface)) {
            startJavaFx();
        } else {
            navigateToLogin();
        }
    }


    private IViewFactory createViewFactory(InputManager inputManager) {
        return JAVAFX.equals(currentInterface) ? new FXViewFactory() : new CliIViewFactory(inputManager);
    }


    private DaoFactory createDaoFactory() {
        PersistenceType persistenceType = switch (currentPersistence) {
            case "DEMO" -> PersistenceType.DEMO;
            case "JSON" -> PersistenceType.JSON;
            case "JDBC" -> PersistenceType.JDBC;
            default -> PersistenceType.JSON;
        };

        return DaoFactory.getFactory(persistenceType);
    }

    private void updateAppConfigPersistence() {
        String appConfigType = switch (currentPersistence) {
            case "DEMO" -> config.AppConfig.DAO_TYPE_MEMORY;
            case "JSON" -> config.AppConfig.DAO_TYPE_JSON;
            case "JDBC" -> config.AppConfig.DAO_TYPE_JDBC;
            default -> config.AppConfig.DEFAULT_DAO_TYPE;
        };

        config.AppConfig.setPersistenceType(appConfigType);
    }

    private void startJavaFx() {
        Platform.startup(() -> Platform.runLater(this::navigateToLogin));
    }

    public void navigateToLogin() throws NavigationException {
        // Se l'app è avviata in modalità demo si assicura che sia globalmente configurata in modalità demo
        if (startedInDemoMode) {
            config.AppConfig.setPersistenceType(config.AppConfig.DAO_TYPE_MEMORY);
            // Do NOT recreate daoFactory/userDao here because that would discard any in-memory changes
            if (daoFactory == null) {
                daoFactory = DaoFactory.getFactory(model.domain.enumerations.PersistenceType.DEMO);
            }
            if (userDao == null) {
                userDao = daoFactory.createUserDao();
            }
        }
        LoginController controller = new LoginController(userDao, this);
        ILoginView view = viewFactory.createLoginView(controller);
        controller.setView(view);
        displayView(view);
    }

    public void navigateToRegistration() throws NavigationException {
        RegistrationController controller = new RegistrationController(userDao, this);
        IRegistrationView view = viewFactory.createRegistrationView(controller);
        controller.setView(view);
        displayView(view);
    }


    public void navigateToRegistrationWithDao(model.dao.IUserDao userDaoForRegistration) throws NavigationException {
        RegistrationController controller = new RegistrationController(userDaoForRegistration, this);
        IRegistrationView view = viewFactory.createRegistrationView(controller);
        controller.setView(view);
        displayView(view);
    }

    public void navigateToCollectorHomePage(UserBean user) throws NavigationException {
        model.dao.IBinderDao binderDao = daoFactory.createBinderDao();
        CollectorHPController controller = new CollectorHPController(user.getUsername(), this, binderDao);
        ICollectorHPView view = viewFactory.createCollectorHomePageView(controller);
        controller.setView(view);
        displayView(view);
    }

    public void navigateToStoreHomePage(UserBean user) throws NavigationException {
        StoreHPController controller = new StoreHPController(user.getUsername(), this);
        IStoreHPView view = viewFactory.createStoreHomePageView(controller);


        controller.setView(view);
        displayView(view);
    }

    public void navigateToCollection(String username) throws NavigationException {
        IBinderDao binderDao = daoFactory.createBinderDao();
        CollectionController controller = new CollectionController(username, this, binderDao);
        ICollectionView collectionView = viewFactory.createCollectionView(controller);
        controller.setView(collectionView);
        displayView(collectionView);
    }


    public void navigateToManageTrade(String username) throws NavigationException {
        try {

            if (!viewStack.isEmpty()) {
                IView top = viewStack.getLast();
                if (top instanceof IManageTradeView) {
                    LOGGER.fine("Already on Manage Trades view; skipping navigation");
                    return;
                }
            }
            ManageTradeController controller = new ManageTradeController(username, this);
            IManageTradeView manageView = viewFactory.createManageTradeView(controller);
            controller.setView(manageView);
            displayView(manageView);
        } catch (Exception err) {
            throw new NavigationException("Failed to navigate to Manage Trades", err);
        }
    }

    public void navigateToTrade(String username) throws NavigationException {
        try {
            LiveTradeController controller = new LiveTradeController(username, this);
            ICollectorTradeView tradeView = viewFactory.createTradeView(controller);
            controller.setView(tradeView);

            LOGGER.info(() -> "navigateToTrade: about to load scheduled and completed trades for user=" + username);
            controller.loadScheduledTrades();
            LOGGER.info(() -> "navigateToTrade: scheduled trades loaded, now loading completed trades for user=" + username);
            controller.loadCollectorCompletedTrades();
            LOGGER.info(() -> "navigateToTrade: completed trades loaded for user=" + username);
            displayView(tradeView);
        } catch (Exception ex) {
            throw new NavigationException("Failed to navigate to trade transaction", ex);
        }
    }

    public void navigateToLiveTrades(String username) throws NavigationException {
        try {
            LiveTradeController controller = new LiveTradeController(username, this);
            ICollectorTradeView tradeView = viewFactory.createTradeView(controller);
            controller.setView(tradeView);


            LOGGER.info(() -> "navigateToLiveTrades: loading scheduled trades for user=" + username);
            controller.loadScheduledTrades();
            LOGGER.info(() -> "navigateToLiveTrades: loading completed trades for user=" + username);
            controller.loadCollectorCompletedTrades();
            displayView(tradeView);
        } catch (Exception ex) {
            throw new NavigationException("Failed to navigate to Live Trades", ex);
        }
    }


    public void navigateToStoreTrades(String username) throws NavigationException {
        try {
            LiveTradeController controller = new LiveTradeController(username, this);

            view.IStoreTradeView storeView = viewFactory.createStoreTradeView(controller);
            LOGGER.info(() -> "navigateToStoreTrades: created view instance: " + (storeView != null ? storeView.getClass().getName() : NULL));

            controller.setStoreView(storeView);

            displayView(storeView);

        } catch (Exception ex) {
            throw new NavigationException("Failed to navigate to Store Trades", ex);
        }
    }

    public void navigateToStoreCompletedTrades(String username) throws NavigationException {
        try {
            LiveTradeController controller = new LiveTradeController(username, this);
            view.IStoreTradeView storeView = viewFactory.createStoreTradeView(controller);
            controller.setStoreView(storeView);
            displayView(storeView);

            controller.loadStoreCompletedTrades();
        } catch (Exception ex) {
            throw new NavigationException("Failed to navigate to Store Completed Trades", ex);
        }
    }

    public void navigateToNegotiation(String proposerUsername, model.bean.CardBean targetCard) throws NavigationException {

        String targetOwner = targetCard != null ? targetCard.getOwner() : null;
        NegotiationController controller = new NegotiationController(proposerUsername, targetOwner, this);
        INegotiationView negotiationView = viewFactory.createNegotiationView(controller);
        controller.setView(negotiationView);


        try {
            model.dao.IBinderDao binderDao = daoFactory.createBinderDao();
            java.util.List<model.bean.CardBean> inventory = new java.util.ArrayList<>();
            if (proposerUsername != null) {
                java.util.List<model.domain.Binder> proposerBinders = binderDao.getUserBinders(proposerUsername);
                for (model.domain.Binder b : proposerBinders) {
                    if (b != null && b.getCards() != null) inventory.addAll(b.getCards());
                }
            }

            java.util.List<model.bean.CardBean> requested = new java.util.ArrayList<>();
            if (targetCard != null) {

                model.bean.CardBean copy = new model.bean.CardBean(targetCard);
                copy.setQuantity(1);
                requested.add(copy);
            }


            controller.start(inventory, requested);
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Could not assemble negotiation data: {0}", ex.getMessage());
        }


        try {
            LOGGER.log(java.util.logging.Level.INFO, "Presenting negotiation view: proposer={0}, target={1}", new Object[]{proposerUsername, targetCard != null ? targetCard.getId() : NULL});
            negotiationView.display();
            LOGGER.info("Negotiation view presentation requested");
        } catch (Exception ex) {

            throw new NavigationException("Failed to present negotiation view", ex);
        }
    }

    public void handleRoleBasedNavigation(UserBean loggedInUser) throws NavigationException {
        if (config.AppConfig.USER_TYPE_COLLECTOR.equals(loggedInUser.getUserType())) {
            navigateToCollectorHomePage(loggedInUser);
        } else if (config.AppConfig.USER_TYPE_STORE.equals(loggedInUser.getUserType())) {
            navigateToStoreHomePage(loggedInUser);
        } else {
            String userType = loggedInUser.getUserType();
            LOGGER.log(java.util.logging.Level.WARNING, "Unknown user type: {0}", userType);
            throw new NavigationException("Unknown user type: " + userType);
        }
    }

    public void logout() throws NavigationException {
        LOGGER.info("Logging out: clearing history and navigating to login");
        if (startedInDemoMode) {
            // Force demo persistence globally so the login screen behaves as demo-only
            config.AppConfig.setPersistenceType(config.AppConfig.DAO_TYPE_MEMORY);
            // Do NOT recreate daoFactory or userDao here; keep the in-memory DAOs so users registered during this session remain available until the app closes.
        } else {
            // keep AppConfig in sync with non-demo persistence
            updateAppConfigPersistence();
        }
        closeAll();
        navigateToLogin();
    }

    private void displayView(IView newView) throws NavigationException {

        if (!viewStack.isEmpty()) {
            IView currentView = viewStack.getLast();
            closeView(currentView);
        }

        viewStack.addLast(newView);
        LOGGER.info(() -> "displayView: about to display view instance: " + (newView != null ? newView.getClass().getName() : NULL));
        try {
            newView.display();
        } catch (Exception ex) {
            String viewName = newView.getClass().getSimpleName();
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to display view: {0}", viewName);
            LOGGER.log(java.util.logging.Level.FINE, "Underlying error while displaying view", ex);

            try {

                newView.showError("Errore interno: " + ex.getMessage());
            } catch (Exception inner) {
                LOGGER.log(java.util.logging.Level.FINER, "Failed to show error on view", inner);
            }

        }
    }

    private void closeAll() throws NavigationException {
        while (!viewStack.isEmpty()) {
            IView view = viewStack.removeLast();
            closeView(view);
        }
    }

    private void closeView(IView view) throws NavigationException {
        if (view != null) {
            try {
                view.close();
            } catch (Exception ex) {
                String viewName = view.getClass().getSimpleName();
                throw new NavigationException("Failed to close view: " + viewName, ex);
            }
        }
    }

    public ICardProvider getCardProvider() {
        if (cardProvider == null) {
            cardProvider = new model.api.ApiFactory().getCardProvider(currentGameType);
        }
        return cardProvider;
    }

    public boolean isStartedInDemoMode() {
        return startedInDemoMode;
    }

}
