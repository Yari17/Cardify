package controller;

import config.AppConfig;
import model.bean.UserBean;
import model.dao.UserDao;
import model.dao.factory.JdbcDaoFactory;
import model.dao.factory.JsonDaoFactory;
import view.InputManager;
import view.collectorHomepage.ICollectorHomePageView;
import view.factory.CliIViewFactory;
import view.factory.IViewFactory;
import view.factory.JavaFxViewFactory;
import view.login.ILoginView;
import view.registration.IRegistrationView;
import view.storeHomepage.IStoreHomePageView;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationController {
    private static final Logger LOGGER = Logger.getLogger(ApplicationController.class.getName());
    private static final String JAVAFX = "JavaFX";
    private static final String CLI = "CLI";
    private UserDao userDao;
    private final Navigator navigator = new Navigator();
    private IViewFactory viewFactory;
    private String currentInterface = CLI;

    public ApplicationController() {
    }

    public void start() {
        chooseInterface();
        choosePersistence();

        if (JAVAFX.equals(currentInterface)) {
            startJavaFx();
        } else {
            startCli();
        }
    }

    private void startCli() {
        userDao = createUserDao();
        InputManager inputManager = new InputManager();
        viewFactory = new CliIViewFactory(inputManager);
        navigateToLogin();
    }

    private void startJavaFx() {
        // Inizializza JavaFX toolkit manualmente usando Platform.startup()
        javafx.application.Platform.startup(() -> {
            // Questo runnable viene eseguito quando JavaFX è pronto
            userDao = createUserDao();
            viewFactory = new JavaFxViewFactory();

            // Naviga al login dopo l'inizializzazione
            javafx.application.Platform.runLater(this::navigateToLogin);
        });
    }

    private UserDao createUserDao() {
        String persistenceType = AppConfig.getPersistenceType();

        if (AppConfig.DAO_TYPE_JDBC.equals(persistenceType)) {
            return new JdbcDaoFactory().createUserDao();
        } else {
            return new JsonDaoFactory().createUserDao();
        }
    }

    private void chooseInterface() {
        Scanner scanner = new Scanner(System.in);
        boolean validChoice = false;

        while (!validChoice) {
            System.out.println("\n=== CARDIFY - Scegli Interfaccia ===");
            System.out.println("1) JavaFX (Interfaccia Grafica)");
            System.out.println("2) CLI (Interfaccia Testuale)");
            System.out.print("Scelta (1-2): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    currentInterface = JAVAFX;
                    validChoice = true;
                    break;
                case "2":
                    currentInterface = CLI;
                    validChoice = true;
                    break;
                default:
                    System.out.println("Scelta non valida. Riprova.");
            }
        }
        System.out.println("Interfaccia selezionata: " + currentInterface);
    }

    private void choosePersistence() {
        Scanner scanner = new Scanner(System.in);
        boolean validChoice = false;

        while (!validChoice) {
            System.out.println("\n=== CARDIFY - Scegli Persistenza ===");
            System.out.println("1) JSON (File System)");
            System.out.println("2) JDBC (Database)");
            System.out.print("Scelta (1-2): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    AppConfig.setPersistenceType(AppConfig.DAO_TYPE_JSON);
                    validChoice = true;
                    break;
                case "2":
                    AppConfig.setPersistenceType(AppConfig.DAO_TYPE_JDBC);
                    validChoice = true;
                    break;
                default:
                    System.out.println("Scelta non valida. Riprova.");
            }
        }
        System.out.println("Persistenza selezionata: " + AppConfig.getPersistenceType());
    }

    public void navigateToLogin() {
        try {
            LoginController loginController = new LoginController(userDao, this);
            ILoginView loginView = viewFactory.createLoginView(loginController);
            loginController.setView(loginView);
            navigator.navigateTo(loginView);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to login", e);
            handleNavigationError(e);
        }
    }

    public void navigateToRegistration() {
        try {
            RegistrationController registrationController = new RegistrationController(userDao, this);
            IRegistrationView registrationView = viewFactory.createRegistrationView(registrationController);
            registrationController.setView(registrationView);
            navigator.navigateTo(registrationView);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to registration", e);
            handleNavigationError(e);
        }
    }

    public void handleRoleBasedNavigation(UserBean loggedInUser) {
        String userType = loggedInUser.getUserType();

        if (UserBean.USER_TYPE_COLLECTOR.equals(userType)) {
            navigateToCollectorHomePage(loggedInUser);
        } else if (UserBean.USER_TYPE_STORE.equals(userType)) {
            navigateToStoreHomePage(loggedInUser);
        } else {
            LOGGER.warning("Unknown user type: " + userType);
            navigateToLogin();
        }
    }

    public void navigateToCollectorHomePage(UserBean loggedInUser) {
        try {
            CollectorHomePageController controller = new CollectorHomePageController(loggedInUser.getUsername(), this);
            ICollectorHomePageView view = viewFactory.createCollectorHomePageView(controller);
            controller.setView(view);
            navigator.navigateTo(view);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to collector home page", e);
            handleNavigationError(e);
        }
    }

    public void navigateToStoreHomePage(UserBean loggedInUser) {
        try {
            StoreHomePageController controller = new StoreHomePageController(loggedInUser.getUsername(), this);
            IStoreHomePageView view = viewFactory.createStoreHomePageView(controller);
            controller.setView(view);
            navigator.navigateTo(view);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to store home page", e);
            handleNavigationError(e);
        }
    }

    public void back() {
        LOGGER.info("Going back to login");
        navigateToLogin();
    }

    public void exit() {
        LOGGER.info("Exiting application");
        navigator.closeAll();

        if (JAVAFX.equals(currentInterface)) {
            javafx.application.Platform.exit();
        }
        System.exit(0);
    }

    private void handleNavigationError(Exception e) {
        System.err.println("Errore di navigazione: " + e.getMessage());
        navigateToLogin();
    }
}
