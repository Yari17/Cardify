package org.example.controller;

import org.example.model.dao.UserDao;
import org.example.view.IRegistrationView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application Controller for registration use-case.
 * Pure Java, UI-agnostic; it communicates with the view only through IRegistrationView.
 */
public class RegistrationController {
    private static final Logger LOGGER = Logger.getLogger(RegistrationController.class.getName());

    private final IRegistrationView view;
    private final UserDao userDao;

    public RegistrationController(IRegistrationView view, UserDao userDao) {
        this.view = view;
        this.userDao = userDao;
    }

    /**
     * Entry point invoked by the graphic controller when user clicks "Registrati".
     */
    public void onRegisterRequested() {
        try {
            // Read inputs from view
            String username = view.getUsername();
            String password = view.getPassword();

            // Validation
            if (username == null || username.trim().isEmpty()) {
                view.showInputError("Il campo username non può essere vuoto");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                view.showInputError("Il campo password non può essere vuoto");
                return;
            }

            // Validate minimum length
            if (username.length() < 3) {
                view.showInputError("L'username deve contenere almeno 3 caratteri");
                return;
            }

            if (password.length() < 6) {
                view.showInputError("La password deve contenere almeno 6 caratteri");
                return;
            }

            // Delegate to DAO - it will check for duplicates and throw exception if exists
            userDao.register(username, password);

            // Success
            view.showSuccess("Registrazione completata! Ora puoi effettuare il login.");

            // Close registration dialog after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    cleanup();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (IllegalArgumentException e) {
            // Username already exists
            view.showInputError(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in registration controller", e);
            view.showInputError("Si è verificato un errore. Riprova.");
        }
    }

    public void show() {
        view.display();
    }

    public void cleanup() {
        view.close();
    }
}
