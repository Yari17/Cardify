package org.example.controller;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.config.AppConfig;
import org.example.model.dao.UserDao;
import org.example.model.dao.factory.DaoFactory;
import org.example.model.dao.factory.JdbcDaoFactory;
import org.example.model.dao.factory.JsonDaoFactory;
import org.example.view.factory.CliIViewFactory;
import org.example.view.factory.IViewFactory;
import org.example.view.factory.JavaFxViewFactory;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application Controller - Orchestrates application startup and configuration.
 * Handles both CLI and JavaFX modes.
 *
 * GRASP Principles:
 * - Controller: Coordinates system startup
 * - Information Expert: Knows how to configure the application
 * - Low Coupling: Components are created but not tightly coupled
 */
public class ApplicationController extends Application {
    private static final Logger LOGGER = Logger.getLogger(ApplicationController.class.getName());

    private NavigationController navigationController;
    private String viewType;
    private String daoType;

    /**
     * Constructor for programmatic instantiation (CLI mode).
     * @param viewType the view type (cli or javafx)
     * @param daoType the DAO type (json or jdbc)
     */
    public ApplicationController(String viewType, String daoType) {
        this.viewType = viewType != null ? viewType : AppConfig.DEFAULT_VIEW_TYPE;
        this.daoType = daoType != null ? daoType : AppConfig.DEFAULT_DAO_TYPE;
    }

    /**
     * No-args constructor for JavaFX Application.launch().
     */
    public ApplicationController() {
        // Will be configured in start() from JavaFX parameters
    }

    /**
     * Start the application (for CLI mode).
     */
    public void startCli() {
        try {
            LOGGER.info("Starting application in CLI mode with " + daoType + " persistence");

            // Set global persistence type
            AppConfig.setPersistenceType(daoType);

            // Create components
            UserDao userDao = createUserDao();
            IViewFactory viewFactory = new CliIViewFactory();

            // Create navigation controller
            navigationController = new NavigationController(viewFactory, userDao);

            // Start navigation flow
            navigationController.navigateToLogin();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting CLI application", e);
            throw new RuntimeException("Failed to start CLI application", e);
        }
    }

    /**
     * JavaFX application start method (for JavaFX mode).
     * @param primaryStage the primary stage (not used directly)
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Parse configuration from JavaFX parameters
            parseConfiguration(getParameters().getRaw());

            LOGGER.info("Starting application in JavaFX mode with " + daoType + " persistence");

            // Set global persistence type
            AppConfig.setPersistenceType(daoType);

            // Create components
            UserDao userDao = createUserDao();
            IViewFactory viewFactory = new JavaFxViewFactory();

            // Create navigation controller
            navigationController = new NavigationController(viewFactory, userDao);

            // Start navigation flow
            navigationController.navigateToLogin();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting JavaFX application", e);
            throw new RuntimeException("Failed to start JavaFX application", e);
        }
    }

    /**
     * JavaFX application stop method.
     * Cleanup resources when application closes.
     */
    @Override
    public void stop() {
        cleanup();
    }

    /**
     * Cleanup application resources.
     */
    public void cleanup() {
        if (navigationController != null) {
            navigationController.exit();
        }
    }

    /**
     * Parse configuration from command-line arguments.
     * @param params the parameters
     */
    private void parseConfiguration(List<String> params) {
        viewType = AppConfig.DEFAULT_VIEW_TYPE;
        daoType = AppConfig.DEFAULT_DAO_TYPE;

        for (String p : params) {
            String lower = p.toLowerCase(Locale.ROOT);
            if (lower.startsWith(AppConfig.ARG_PREFIX_VIEW)) {
                viewType = lower.substring(AppConfig.ARG_PREFIX_VIEW.length());
            } else if (lower.startsWith(AppConfig.ARG_PREFIX_DAO)) {
                daoType = lower.substring(AppConfig.ARG_PREFIX_DAO.length());
            } else {
                if (AppConfig.VIEW_TYPE_CLI.equals(lower) || AppConfig.VIEW_TYPE_JAVAFX.equals(lower)) {
                    viewType = lower;
                }
                if (AppConfig.DAO_TYPE_JDBC.equals(lower) || AppConfig.DAO_TYPE_JSON.equals(lower)) {
                    daoType = lower;
                }
            }
        }
    }

    /**
     * Create the UserDao based on configuration.
     * @return the UserDao instance
     */
    private UserDao createUserDao() {
        DaoFactory daoFactory = AppConfig.DAO_TYPE_JDBC.equals(daoType)
                ? new JdbcDaoFactory()
                : new JsonDaoFactory();

        return daoFactory.createUserDao();
    }
}

