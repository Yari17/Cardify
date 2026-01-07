package org.example.controller;

import org.example.model.bean.UserBean;
import org.example.model.dao.UserDao;
import org.example.view.registration.IRegistrationView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application Controller for registration use-case.
 * Handles registration business logic and delegates navigation to NavigationController.
 *
 * GRASP Principles:
 * - Controller: Handles registration use-case operations
 * - Low Coupling: Doesn't know about other views, uses NavigationController
 * - Information Expert: Delegates user creation to UserDao
 */
public class RegistrationController {
    private static final Logger LOGGER = Logger.getLogger(RegistrationController.class.getName());

    private IRegistrationView view;
    private final UserDao userDao;
    private final NavigationController navigationController;

    /**
     * Constructor with dependency injection.
     * @param view the registration view (can be null initially for circular dependency resolution)
     * @param userDao data access object for user operations
     * @param navigationController controller for navigation between views
     */
    public RegistrationController(IRegistrationView view, UserDao userDao, NavigationController navigationController) {
        this.view = view;
        this.userDao = userDao;
        this.navigationController = navigationController;
    }

    /**
     * Sets the view (used to resolve circular dependency).
     * @param view the registration view
     */
    public void setView(IRegistrationView view) {
        this.view = view;
    }

    /**
     * Entry point invoked by the view when user clicks "Registrati".
     */
    public void onRegisterRequested() {
        UserBean userBean = view.getUserData();

        if (userBean == null) {
            view.showInputError("Impossibile recuperare i dati utente");
            return;
        }

        // Delegate validation to the Bean (Information Expert principle)
        String validationError = userBean.getValidationError();
        if (validationError != null) {
            view.showInputError(validationError);
            return;
        }

        try {
            // Delegate to DAO - it will check for duplicates and throw exception if exists
            userDao.register(userBean.getUsername(), userBean.getPassword(), userBean.getUserType());

            // Success
            view.showSuccess("Registrazione completata! Ora puoi effettuare il login.");

            // Navigate back to login
            navigationController.navigateToLogin();

        } catch (IllegalArgumentException e) {
            // Username already exists
            view.showInputError(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in registration controller", e);
            view.showInputError("Si è verificato un errore. Riprova.");
        }
    }

    /**
     * Called when user clicks "Torna al login" or back button.
     */
    public void onBackToLoginRequested() {
        navigationController.navigateToLogin();
    }
}
