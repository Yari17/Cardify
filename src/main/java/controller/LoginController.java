package controller;

import model.bean.UserBean;
import model.dao.IUserDao;
import model.domain.User;
import view.ILoginView;

import java.util.Optional;

public class LoginController {

    private ILoginView view;
    private final IUserDao userDao;
    private final ApplicationController navigationController;

    public LoginController(IUserDao userDao, ApplicationController navigationController) {
        this.userDao = userDao;
        this.navigationController = navigationController;
    }

    public void setView(ILoginView view) {
        this.view = view;
    }

    public void onLoginRequested() {
        UserBean userBean = view.getUserCredentials();

        if (userBean == null) {
            view.showInputError("Impossibile recuperare le credenziali");
            return;
        }

        String validationError = userBean.getValidationError();
        if (validationError != null) {
            view.showInputError(validationError);
            return;
        }

        
        model.domain.enumerations.PersistenceType chosen = view.getPersistenceType();
        IUserDao daoToUse = userDao;
        if (chosen != null) {
            //Se è stata scelta la persistenza DEMO, usa l'userDao in memoria già esistente
            if (chosen == model.domain.enumerations.PersistenceType.DEMO) {
                daoToUse = this.userDao;
            } else {
                try {
                    daoToUse = model.dao.factory.DaoFactory.getFactory(chosen).createUserDao();
                } catch (Exception _) {
                    daoToUse = userDao;
                }
            }
        }

        Optional<User> authenticatedUser = java.util.Optional.empty();
        try {
            authenticatedUser = daoToUse.authenticateAndGetUser(
                    userBean.getUsername(),
                    userBean.getPassword()
            );
        } catch (exception.UserNotFoundException _) {
            
            view.showInputError("Utente non trovato nella persistenza selezionata. Riprova con la persistenza corretta o registrati.");
            return;
        } catch (exception.AuthenticationException _) {
            view.showInputError("Credenziali non valide. Riprova.");
            return;
        } catch (exception.DataPersistenceException dpe) {
            view.showError("Errore di persistenza: " + dpe.getMessage());
            return;
        } catch (RuntimeException rte) {
            
            java.util.logging.Logger.getLogger(LoginController.class.getName()).log(java.util.logging.Level.SEVERE, "Unexpected error during authentication", rte);
            view.showError("Si è verificato un errore durante l'autenticazione. Riprova più tardi.");
            return;
        }

        if (authenticatedUser.isPresent()) {
            User user = authenticatedUser.get();

            UserBean loggedInUserBean = new UserBean(user.getName(), null, user.getUserType());

            view.showSuccess("Login effettuato con successo! Benvenuto " + loggedInUserBean.getUsername());

            view.close();

            
            config.AppConfig.setPersistenceType(config.AppConfig.DAO_TYPE_JSON);

            navigationController.handleRoleBasedNavigation(loggedInUserBean);
        } else {
            view.showInputError("Credenziali non valide. Riprova.");
        }
    }

    public void onRegisterRequested() {
        
        model.domain.enumerations.PersistenceType chosen = view.getPersistenceType();
        model.dao.IUserDao daoForReg = userDao;
        if (chosen != null) {
            if (chosen == model.domain.enumerations.PersistenceType.DEMO) {
                daoForReg = this.userDao; // reuse existing in-memory user dao
            } else {
                try {
                    daoForReg = model.dao.factory.DaoFactory.getFactory(chosen).createUserDao();
                } catch (Exception _) {
                    daoForReg = userDao;
                }
            }
        }
        try {
            navigationController.navigateToRegistrationWithDao(daoForReg);
        } catch (Exception _) {
            
            navigationController.navigateToRegistration();
        }
    }
}
