package controller;

import config.AppConfig;
import javafx.application.Platform;
import model.dao.IUserDao;
import model.dao.factory.DaoFactory;
import model.dao.factory.DemoDaoFactory;
import model.dao.factory.JdbcDaoFactory;
import model.dao.factory.JsonDaoFactory;
import view.InputManager;
import view.factory.CliIViewFactory;
import view.factory.FXViewFactory;
import view.factory.IViewFactory;


public class ApplicationController {
    private static final String JAVAFX = "JavaFX";

    private Navigator navigator;
    private String currentInterface;
    private DaoFactory daoFactory;

    public void start() {
        InputManager inputManager = new InputManager();
        ConfigurationManager config = new ConfigurationManager(inputManager);

        currentInterface = config.chooseInterface();

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

    /**
     * Crea l'Abstract Factory appropriata in base alla configurazione.
     * Utilizza il pattern Abstract Factory per ottenere una famiglia coerente di DAO.
     *
     * @return l'Abstract Factory concreta per il tipo di persistenza configurato
     */
    private DaoFactory createDaoFactory() {
        DaoFactory.PersistenceType persistenceType = switch (AppConfig.getPersistenceType()) {
            case AppConfig.DAO_TYPE_JDBC -> DaoFactory.PersistenceType.JDBC;
            case AppConfig.DAO_TYPE_MEMORY -> DaoFactory.PersistenceType.DEMO;
            default -> DaoFactory.PersistenceType.JSON;
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
