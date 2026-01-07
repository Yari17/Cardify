package org.example.controller;

import org.example.view.homepage.IHomePageView;

import java.util.logging.Logger;

/**
 * Application Controller for HomePage use-case.
 * Handles the main page after successful login.
 *
 * GRASP Principles:
 * - Controller: Handles home page operations
 * - Low Coupling: Uses NavigationController for navigation
 */
public class HomePageController {
    private static final Logger LOGGER = Logger.getLogger(HomePageController.class.getName());

    private IHomePageView view;
    private final String username;
    private final NavigationController navigationController;

    /**
     * Constructor with dependency injection.
     * @param view the home page view (can be null initially for circular dependency resolution)
     * @param username the authenticated user's username
     * @param navigationController controller for navigation between views
     */
    public HomePageController(IHomePageView view, String username, NavigationController navigationController) {
        this.view = view;
        this.username = username;
        this.navigationController = navigationController;
    }

    /**
     * Sets the view (used to resolve circular dependency).
     * @param view the home page view
     */
    public void setView(IHomePageView view) {
        this.view = view;
    }

    /**
     * Get the current username.
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Called when user clicks "Logout".
     */
    public void onLogoutRequested() {
        LOGGER.info("User " + username + " logging out");
        navigationController.navigateToLogin();
    }

    /**
     * Called when user clicks "Exit".
     */
    public void onExitRequested() {
        navigationController.exit();
    }
}

