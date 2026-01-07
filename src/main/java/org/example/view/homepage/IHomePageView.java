package org.example.view.homepage;

import org.example.controller.HomePageController;

/**
 * View interface for the HomePage.
 * The application controller manipulates the view only via this interface.
 */
public interface IHomePageView {
    /**
     * Set the controller for this view.
     * @param controller the home page controller
     */
    void setController(HomePageController controller);

    /**
     * Display the view.
     */
    void display();

    /**
     * Close the view.
     */
    void close();

    /**
     * Show a welcome message to the user.
     * @param username the username to display
     */
    void showWelcomeMessage(String username);
}

