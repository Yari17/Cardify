package controller;

import config.AppConfig;
import javafx.application.Platform;
import model.bean.UserBean;
import model.dao.UserDao;
import model.dao.factory.JdbcDaoFactory;
import model.dao.factory.JsonDaoFactory;
import view.InputManager;
import view.factory.CliIViewFactory;
import view.factory.IViewFactory;
import view.factory.FXViewFactory;

import java.util.logging.Logger;


public class ApplicationController {
    private static final Logger LOGGER = Logger.getLogger(ApplicationController.class.getName());
    private static final String JAVAFX = "JavaFX";

    private Navigator navigator;
    private String currentInterface;

    public void start() {
        InputManager inputManager = new InputManager();
        ConfigurationManager config = new ConfigurationManager(inputManager);

        currentInterface = config.chooseInterface();
        config.choosePersistence();

        UserDao userDao = createUserDao();
        IViewFactory viewFactory = createViewFactory(inputManager);
        navigator = new Navigator(userDao, viewFactory);

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

    private UserDao createUserDao() {
        return AppConfig.DAO_TYPE_JDBC.equals(AppConfig.getPersistenceType())
            ? new JdbcDaoFactory().createUserDao()
            : new JsonDaoFactory().createUserDao();
    }

    private void startJavaFx() {
        Platform.startup(() ->
            Platform.runLater(navigator::navigateToLogin)
        );
    }

    public void navigateToLogin() {
        navigator.navigateToLogin();
    }

    public void logout() {
        navigator.logout();
    }

    public void navigateToRegistration() {
        navigator.navigateToRegistration();
    }

    public void handleRoleBasedNavigation(UserBean loggedInUser) {
        navigator.handleRoleBasedNavigation(loggedInUser);
    }

    public void back() {
        navigator.back();
    }

    public void exit() {
        LOGGER.info("Exiting application");
        navigator.closeAll();

        if (JAVAFX.equals(currentInterface)) {
            javafx.application.Platform.exit();
        }
        System.exit(0);
    }
}
