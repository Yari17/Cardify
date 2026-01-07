package org.example.controller;

import org.example.model.dao.UserDao;
import org.example.view.factory.IViewFactory;
import org.example.view.homepage.IHomePageView;
import org.example.view.login.ILoginView;
import org.example.view.registration.IRegistrationView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Navigation Controller - manages the navigation flow between different views.
 * Works with any view type (CLI, JavaFX, etc.) through polymorphism.
 *
 * GRASP Principles:
 * - Controller: Coordinates system operations and delegates to appropriate objects
 * - Low Coupling: Controllers don't know about each other's views
 * - High Cohesion: Only handles navigation logic
 * - Polymorphism: Works with any IViewFactory implementation
 *
 * Design Patterns:
 * - Mediator Pattern: Mediates communication between controllers
 * - Dependency Injection: Receives factory and DAO as dependencies
 */
public class NavigationController {
    private static final Logger LOGGER = Logger.getLogger(NavigationController.class.getName());

    private final IViewFactory viewFactory;
    private final UserDao userDao;

    // Current active views (for lifecycle management)
    private ILoginView currentLoginView;
    private IRegistrationView currentRegistrationView;
    private IHomePageView currentHomePageView;

    /**
     * Constructor with dependency injection.
     * @param viewFactory factory for creating views (CLI or JavaFX)
     * @param userDao data access object for user operations
     */
    public NavigationController(IViewFactory viewFactory, UserDao userDao) {
        this.viewFactory = viewFactory;
        this.userDao = userDao;
    }

    /**
     * Navigate to the login view.
     */
    public void navigateToLogin() {
        LOGGER.info("Navigating to Login view");

        closeCurrentViews();

        try {
            // Create LoginController with this navigation controller
            LoginController loginController = new LoginController(null, userDao, this);

            // Create view with injected controller
            currentLoginView = viewFactory.createLoginView(loginController);

            // Update controller's view reference
            loginController.setView(currentLoginView);

            // Display the view
            currentLoginView.display();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to Login view", e);
            handleNavigationError("Impossibile caricare la schermata di login");
        }
    }

    /**
     * Navigate to the registration view.
     */
    public void navigateToRegistration() {
        LOGGER.info("Navigating to Registration view");

        closeCurrentViews();

        try {
            // Create RegistrationController with this navigation controller
            RegistrationController registrationController = new RegistrationController(null, userDao, this);

            // Create view with injected controller
            currentRegistrationView = viewFactory.createRegistrationView(registrationController);

            // Update controller's view reference
            registrationController.setView(currentRegistrationView);

            // Display the view
            currentRegistrationView.display();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to Registration view", e);
            handleNavigationError("Impossibile caricare la schermata di registrazione");
        }
    }

    /**
     * Navigate to the home page after successful login.
     * @param username the authenticated user's username
     */
    public void navigateToHomePage(String username) {
        LOGGER.info("Navigating to HomePage for user: " + username);

        closeCurrentViews();

        try {
            // Create HomePageController with this navigation controller
            HomePageController homePageController = new HomePageController(null, username, this);

            // Create view with injected controller
            currentHomePageView = viewFactory.createHomePageView(homePageController);

            // Update controller's view reference
            homePageController.setView(currentHomePageView);

            // Display the view
            currentHomePageView.display();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to HomePage", e);
            handleNavigationError("Impossibile caricare la home page");
        }
    }

    /**
     * Exit the application.
     */
    public void exit() {
        LOGGER.info("Exiting application");
        closeCurrentViews();
        System.exit(0);
    }

    /**
     * Closes all currently open views.
     * Ensures proper cleanup of resources.
     */
    private void closeCurrentViews() {
        if (currentLoginView != null) {
            try {
                currentLoginView.close();
                currentLoginView = null;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing login view", e);
            }
        }

        if (currentRegistrationView != null) {
            try {
                currentRegistrationView.close();
                currentRegistrationView = null;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing registration view", e);
            }
        }

        if (currentHomePageView != null) {
            try {
                currentHomePageView.close();
                currentHomePageView = null;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing home page view", e);
            }
        }
    }

    /**
     * Handles navigation errors by showing an error and returning to login.
     * @param errorMessage the error message to display
     */
    private void handleNavigationError(String errorMessage) {
        LOGGER.severe(errorMessage);
        try {
            navigateToLogin();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Critical error: cannot navigate to login", e);
            exit();
        }
    }
}

