package controller;

import exception.NavigationException;
import javafx.application.Platform;
import model.bean.UserBean;
import model.dao.IUserDao;
import model.dao.factory.DaoFactory;
import view.InputManager;
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
    private String currentInterface;
    private String currentPersistence;
    private DaoFactory daoFactory;
    private IUserDao userDao;
    private IViewFactory viewFactory;

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

    private IViewFactory createViewFactory(InputManager inputManager) {
        return JAVAFX.equals(currentInterface)
                ? new FXViewFactory()
                : new CliIViewFactory(inputManager);
    }

    private DaoFactory createDaoFactory() {
        DaoFactory.PersistenceType persistenceType = switch (currentPersistence) {
            case "DEMO" -> DaoFactory.PersistenceType.DEMO;
            case "JSON" -> DaoFactory.PersistenceType.JSON;
            case "JDBC" -> DaoFactory.PersistenceType.JDBC;
            default -> DaoFactory.PersistenceType.JSON; // Default a JSON
        };

        return DaoFactory.getFactory(persistenceType);
    }

    /**
     * Aggiorna AppConfig con il tipo di persistenza selezionato.
     * Mappa i valori del ConfigurationManager ai valori di AppConfig.
     */
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

    // ============ Navigation Methods (ex-Navigator) ============

    /**
     * Navigate to the login page.
     */
    public void navigateToLogin() throws NavigationException {
        LoginController controller = new LoginController(userDao, this);
        ILoginView view = viewFactory.createLoginView(controller);
        controller.setView(view);
        displayView(view);
    }

    /**
     * Navigate to the registration page.
     */
    public void navigateToRegistration() throws NavigationException {
        RegistrationController controller = new RegistrationController(userDao, this);
        IRegistrationView view = viewFactory.createRegistrationView(controller);
        controller.setView(view);
        displayView(view);
    }

    /**
     * Navigate to the collector home page.
     */
    public void navigateToCollectorHomePage(UserBean user) throws NavigationException {
        model.dao.ICardDao cardDao = daoFactory.createCardDao();
        CollectorHPController controller = new CollectorHPController(user.getUsername(), this, cardDao);
        ICollectorHPView view = viewFactory.createCollectorHomePageView(controller);
        controller.setView(view);
        displayView(view);
    }

    /**
     * Navigate to the store home page.
     */
    public void navigateToStoreHomePage(UserBean user) throws NavigationException {
        StoreHPController controller = new StoreHPController(user.getUsername(), this);
        IStoreHPView view = viewFactory.createStoreHomePageView(controller);

        displayView(view);
    }

    /**
     * Navigate to the collection page.
     */
    public void navigateToCollection(String username) throws NavigationException {
        model.dao.IBinderDao binderDao = daoFactory.createBinderDao();
        model.dao.ICardDao cardDao = daoFactory.createCardDao();
        CollectionController controller = new CollectionController(username, this, binderDao, cardDao);
        ICollectionView collectionView = viewFactory.createCollectionView(controller);
        controller.setView(collectionView);
        displayView(collectionView);
    }

    /**
     * Navigate to the trade page.
     */
    public void navigateToTrade(String username) throws NavigationException {
        TradeController controller = new TradeController(username, this);
        view.trade.ITradeView tradeView = viewFactory.createTradeView(controller);
        controller.setView(tradeView);
        displayView(tradeView);
    }

    /**
     * Handle role-based navigation after login.
     */
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

    /**
     * Logout: clear navigation history and return to login.
     */
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

    /**
     * Close all views in the stack.
     */
    private void closeAll() throws NavigationException {
        while (!viewStack.isEmpty()) {
            IView view = viewStack.removeLast();
            closeView(view);
        }
    }

    /**
     * Close a single view.
     */
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
