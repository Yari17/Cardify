package controller;

import exception.NavigationException;
import javafx.application.Platform;
import model.bean.UserBean;
import model.dao.IUserDao;
import model.dao.factory.DaoFactory;
import config.InputManager;
import model.domain.enumerations.PersistenceType;
import view.IView;
import view.collection.ICollectionView;
import view.collectorhomepage.ICollectorHPView;
import view.factory.CliIViewFactory;
import view.factory.FXViewFactory;
import view.factory.IViewFactory;
import view.login.ILoginView;
import view.registration.IRegistrationView;
import view.storehomepage.IStoreHPView;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

public class ApplicationController {
    private static final Logger LOGGER = Logger.getLogger(ApplicationController.class.getName());
    private static final String JAVAFX = "JavaFX";
    private final Deque<IView> viewStack = new ArrayDeque<>();
    //configurazione
    private String currentInterface;
    private String currentPersistence;
    //factory
    private DaoFactory daoFactory;
    public DaoFactory getDaoFactory() { return daoFactory; }

    private IViewFactory viewFactory;
    //DAO
    private IUserDao userDao;


    public void start() {
        InputManager inputManager = new InputManager();
        ConfigurationManager config = new ConfigurationManager(inputManager);
        currentInterface = config.chooseInterface();
        currentPersistence = config.choosePersistence();

        // Aggiorna AppConfig con il tipo di persistenza selezionato
        updateAppConfigPersistence();

        // Crea l'Abstract Factory appropriata in base alla configurazione
        daoFactory = createDaoFactory();

        // Usa l'Abstract Factory per creare i DAO della famiglia corretta
        userDao = daoFactory.createUserDao();
        viewFactory = createViewFactory(inputManager);

        if (JAVAFX.equals(currentInterface)) {
            startJavaFx();
        } else {
            navigateToLogin();
        }
    }
    // Restituisce la factory di view appropriata in base all'interfaccia selezionata
    private IViewFactory createViewFactory(InputManager inputManager) {
        return JAVAFX.equals(currentInterface)
                ? new FXViewFactory()
                : new CliIViewFactory(inputManager);
    }
    // Restituisce la factory di DAO appropriata in base alla persistenza selezionata
    private DaoFactory createDaoFactory() {
        PersistenceType persistenceType = switch (currentPersistence) {
            case "DEMO" -> PersistenceType.DEMO;
            case "JSON" -> PersistenceType.JSON;
            case "JDBC" -> PersistenceType.JDBC;
            default -> PersistenceType.JSON; // Default a JSON
        };

        return DaoFactory.getFactory(persistenceType);
    }


    private void updateAppConfigPersistence() {
        String appConfigType = switch (currentPersistence) {
            case "DEMO" -> config.AppConfig.DAO_TYPE_MEMORY; // "demo"
            case "JSON" -> config.AppConfig.DAO_TYPE_JSON; // "json"
            case "JDBC" -> config.AppConfig.DAO_TYPE_JDBC; // "jdbc"
            default -> config.AppConfig.DEFAULT_DAO_TYPE; // "json"
        };

        config.AppConfig.setPersistenceType(appConfigType);
    }

    private void startJavaFx() {
        Platform.startup(() -> Platform.runLater(this::navigateToLogin));
    }



    public void navigateToLogin() throws NavigationException {
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

        displayView(view);
    }


    public void navigateToCollection(String username) throws NavigationException {
        model.dao.IBinderDao binderDao = daoFactory.createBinderDao();
        CollectionController controller = new CollectionController(username, this, binderDao);
        ICollectionView collectionView = viewFactory.createCollectionView(controller);
        controller.setView(collectionView);
        displayView(collectionView);
    }


    public void navigateToTrade(String username) throws NavigationException {
        TradeController controller = new TradeController(username, this);
        view.trade.ITradeView tradeView = viewFactory.createTradeView(controller);
        controller.setView(tradeView);
        displayView(tradeView);
    }


    public void navigateToNegotiation(String proposerUsername, model.bean.CardBean targetCard) throws NavigationException {
        // Create a NegotiationController with basic context (proposer and target owner)
        String targetOwner = targetCard != null ? targetCard.getOwner() : null;
        NegotiationController controller = new NegotiationController(proposerUsername, targetOwner, this);
        view.negotiation.INegotiationView negotiationView = viewFactory.createNegotiationView(controller);
        controller.setView(negotiationView);

        // Build simple inventory/requested lists from the binder DAO
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
                // Show the single requested card (as bean)
                requested.add(targetCard);
            }

            // Start the negotiation controller with assembled data
            controller.start(inventory, requested);
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Could not assemble negotiation data: {0}", e.getMessage());
        }

        // Show the negotiation UI. Let the view implementation decide how to present itself
        try {
            LOGGER.log(java.util.logging.Level.INFO, "Presenting negotiation view: proposer={0}, target={1}", new Object[]{proposerUsername, targetCard != null ? targetCard.getId() : "<null>"});
            // The view implementation (FX or CLI) is responsible for presenting itself correctly.
            negotiationView.display();
            LOGGER.info("Negotiation view presentation requested");
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to present negotiation view: {0}", e.getMessage());
            throw new NavigationException("Failed to present negotiation view", e);
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
        closeAll();
        navigateToLogin();
    }

    private void displayView(IView newView) throws NavigationException {
        // Chiudi la view corrente prima di mostrare quella nuova
        if (!viewStack.isEmpty()) {
            IView currentView = viewStack.getLast();
            closeView(currentView);
        }

        viewStack.addLast(newView);
        try {
            newView.display();
        } catch (Exception e) {
            String viewName = newView.getClass().getSimpleName();
            throw new NavigationException("Failed to display view: " + viewName, e);
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
            } catch (Exception e) {
                String viewName = view.getClass().getSimpleName();
                throw new NavigationException("Failed to close view: " + viewName, e);
            }
        }
    }
}
