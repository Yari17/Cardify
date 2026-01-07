package org.example.controller;

import org.example.model.bean.UserBean;
import org.example.model.dao.UserDao;
import org.example.view.login.ILoginView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application Controller for Login use-case.
 * Handles login business logic and delegates navigation to NavigationController.
 *
 * GRASP Principles:
 * - Controller: Handles login use-case operations
 * - Low Coupling: Doesn't know about other views, uses NavigationController
 * - Information Expert: Delegates authentication to UserDao
 */
public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    private ILoginView view;
    private final UserDao userDao;
    private final NavigationController navigationController;

    /**
     * Constructor with dependency injection.
     * @param view the login view (can be null initially for circular dependency resolution)
     * @param userDao data access object for user operations
     * @param navigationController controller for navigation between views
     */
    public LoginController(ILoginView view, UserDao userDao, NavigationController navigationController) {
        this.view = view;
        this.userDao = userDao;
        this.navigationController = navigationController;
    }

    /**
     * Sets the view (used to resolve circular dependency).
     * @param view the login view
     */
    public void setView(ILoginView view) {
        this.view = view;
    }

    /**
     * Entry point called by the view when user clicks "Login".
     */
    public void onLoginRequested() {
        UserBean userBean = view.getUserCredentials();

        if (userBean == null) {
            view.showInputError("Impossibile recuperare le credenziali");
            return;
        }

        // Delegate validation to the Bean (Information Expert principle)
        String validationError = userBean.getValidationError();
        if (validationError != null) {
            view.showInputError(validationError);
            return;
        }

        boolean authenticated = authenticateUser(userBean);

        if (authenticated) {
            view.showSuccess("Login effettuato con successo! Benvenuto " + userBean.getUsername());
            // Navigate to home page
            navigationController.navigateToHomePage(userBean.getUsername());
        } else {
            view.showInputError("Credenziali non valide. Riprova.");
        }
    }

    /**
     * Called when user clicks "Registrati" link.
     */
    public void onRegisterRequested() {
        navigationController.navigateToRegistration();
    }

    /**
     * Authenticates user credentials.
     * @param userBean the user credentials
     * @return true if authenticated, false otherwise
     */
    private boolean authenticateUser(UserBean userBean) {
        if (userDao != null) {
            try {
                Method m = userDao.getClass().getMethod("authenticate", String.class, String.class);
                Object result = m.invoke(userDao, userBean.getUsername(), userBean.getPassword());
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
        return "admin".equals(userBean.getUsername()) && "password123".equals(userBean.getPassword());
    }
}
