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

        // Use view-selected persistence for this auth action (polymorphic choice)
        model.domain.enumerations.PersistenceType chosen = view.getPersistenceType();
        IUserDao daoToUse = userDao;
        if (chosen != null) {
            try {
                daoToUse = model.dao.factory.DaoFactory.getFactory(chosen).createUserDao();
            } catch (Exception ex) {
                // fallback to default app DAO
                daoToUse = userDao;
            }
        }

        Optional<User> authenticatedUser = daoToUse.authenticateAndGetUser(
            userBean.getUsername(),
            userBean.getPassword()
        );

        if (authenticatedUser.isPresent()) {
            User user = authenticatedUser.get();

            UserBean loggedInUserBean = new UserBean(user.getName(), null, user.getUserType());

            view.showSuccess("Login effettuato con successo! Benvenuto " + loggedInUserBean.getUsername());

            view.close();

            // After login, ensure application-wide persistence reverts to JSON
            config.AppConfig.setPersistenceType(config.AppConfig.DAO_TYPE_JSON);

            navigationController.handleRoleBasedNavigation(loggedInUserBean);
        } else {
            view.showInputError("Credenziali non valide. Riprova.");
        }
    }

    public void onRegisterRequested() {
        // Use view-selected persistence to start registration process
        model.domain.enumerations.PersistenceType chosen = view.getPersistenceType();
        model.dao.IUserDao daoForReg = userDao;
        if (chosen != null) {
            try {
                daoForReg = model.dao.factory.DaoFactory.getFactory(chosen).createUserDao();
            } catch (Exception ex) {
                daoForReg = userDao;
            }
        }
        try {
            navigationController.navigateToRegistrationWithDao(daoForReg);
        } catch (Exception e) {
            // fallback
            navigationController.navigateToRegistration();
        }
    }
}
