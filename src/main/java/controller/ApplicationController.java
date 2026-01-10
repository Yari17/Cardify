package controller;

import javafx.application.Platform;
import model.dao.IUserDao;
import model.dao.factory.DaoFactory;
import view.InputManager;
import view.factory.CliIViewFactory;
import view.factory.FXViewFactory;
import view.factory.IViewFactory;


public class ApplicationController {
    private static final String JAVAFX = "JavaFX";

    private Navigator navigator;
    private String currentInterface;
    private String currentPersistence;
    private DaoFactory daoFactory;

    public void start() {
        InputManager inputManager = new InputManager();
        ConfigurationManager config = new ConfigurationManager(inputManager);
        currentInterface = config.chooseInterface();
        currentPersistence = config.choosePersistence();

        // Crea l'Abstract Factory appropriata in base alla configurazione
        daoFactory = createDaoFactory();

        // Usa l'Abstract Factory per creare i DAO della famiglia corretta
        IUserDao userDao = daoFactory.createUserDao();
        IViewFactory viewFactory = createViewFactory(inputManager);
        navigator = new Navigator(userDao, viewFactory, daoFactory);

        if (JAVAFX.equals(currentInterface)) {
            startJavaFx();
        } else {
            navigator.navigateToLogin();
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

    private void startJavaFx() {
        Platform.startup(() ->
            Platform.runLater(navigator::navigateToLogin)
        );
    }
    public void logout() {
        navigator.logout();
    }

}
