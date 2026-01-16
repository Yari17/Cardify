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

        Optional<User> authenticatedUser = userDao.authenticateAndGetUser(
            userBean.getUsername(),
            userBean.getPassword()
        );

        if (authenticatedUser.isPresent()) {
            User user = authenticatedUser.get();

            UserBean loggedInUserBean = new UserBean(user.getName(), null, user.getUserType());

            view.showSuccess("Login effettuato con successo! Benvenuto " + loggedInUserBean.getUsername());

            view.close();


            navigationController.handleRoleBasedNavigation(loggedInUserBean);
        } else {
            view.showInputError("Credenziali non valide. Riprova.");
        }
    }

    public void onRegisterRequested() {
        navigationController.navigateToRegistration();
    }
}
