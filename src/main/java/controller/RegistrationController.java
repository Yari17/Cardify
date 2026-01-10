package controller;

import model.bean.UserBean;
import model.dao.IUserDao;
import view.registration.IRegistrationView;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrationController {
    private static final Logger LOGGER = Logger.getLogger(RegistrationController.class.getName());

    private IRegistrationView view;
    private final IUserDao userDao;
    private final Navigator navigator;

    public RegistrationController(IUserDao userDao, Navigator navigator) {
        this.userDao = userDao;
        this.navigator = navigator;
    }

    public void setView(IRegistrationView view) {
        this.view = view;
    }

    public void onRegisterRequested() {
        UserBean userBean = view.getUserData();

        if (userBean == null) {
            view.showInputError("Impossibile recuperare i dati utente");
            return;
        }

        String validationError = userBean.getValidationError();
        if (validationError != null) {
            view.showInputError(validationError);
            return;
        }

        try {
            userDao.register(userBean.getUsername(), userBean.getPassword(), userBean.getUserType());

            view.showSuccess("Registrazione completata! Ora puoi effettuare il login.");

            navigator.navigateToLogin();

        } catch (IllegalArgumentException e) {
            view.showInputError(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in registration controller", e);
            view.showInputError("Si è verificato un errore. Riprova.");
        }
    }

    public void onBackToLoginRequested() {
        navigator.navigateToLogin();
    }
}
