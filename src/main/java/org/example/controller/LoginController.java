package org.example.controller;

import org.example.model.dao.UserDao;
import org.example.view.ILoginView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    private final ILoginView view;
    private final UserDao userDao;

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Updated constructor to receive a ILoginView so views are swappable at runtime
    public LoginController(ILoginView view, UserDao userDao) {
        this.view = view;
        this.userDao = userDao;
    }

    // Application Controller entry point called by the Graphic Controller
    public void onLoginRequested() {
        String username = view.getUsername();
        String password = view.getPassword();

        // Validation (business logic lives here)
        if (username == null || username.trim().isEmpty()) {
            view.showInputError("Il campo username non può essere vuoto");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            view.showInputError("Il campo password non può essere vuoto");
            return;
        }

        if (username.length() < MIN_USERNAME_LENGTH) {
            view.showInputError("L'username deve contenere almeno " + MIN_USERNAME_LENGTH + " caratteri");
            return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            view.showInputError("La password deve contenere almeno " + MIN_PASSWORD_LENGTH + " caratteri");
            return;
        }

        boolean authenticated = authenticateUser(username, password);

        if (authenticated) {
            view.showSuccess("Login effettuato con successo! Benvenuto " + username);
        } else {
            view.showInputError("Credenziali non valide. Riprova.");
        }
    }

    private boolean authenticateUser(String username, String password) {
        if (userDao != null) {
            try {
                Method m = userDao.getClass().getMethod("authenticate", String.class, String.class);
                Object result = m.invoke(userDao, username, password);
                if (result instanceof Boolean b) {
                    return b;
                }
            } catch (NoSuchMethodException e) {
                LOGGER.log(Level.FINE, "DAO has no authenticate method: {0}", e.getMessage());
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.log(Level.SEVERE, "Error invoking DAO authenticate", e);
            }
        }
        // Fallback simulation
        return "admin".equals(username) && "password123".equals(password);
    }

    public void show() {
        view.display();
    }

    public void cleanup() {
        view.close();
    }

    /**
     * Get the UserDao instance used by this controller.
     * Used by the view to pass it to the RegistrationController.
     * @return the UserDao instance
     */
    public UserDao getUserDao() {
        return userDao;
    }
}
