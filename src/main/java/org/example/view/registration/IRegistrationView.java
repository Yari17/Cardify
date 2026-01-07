package org.example.view.registration;

import org.example.model.bean.UserBean;

/**
 * View interface for the registration use-case.
 * The application controller manipulates the view only via this interface.
 */
public interface IRegistrationView {
    /**
     * Get user registration data from the view.
     * @return UserBean containing username and password
     */
    UserBean getUserData();

    void showInputError(String message);
    void showSuccess(String message);
    void setController(org.example.controller.RegistrationController controller);
    void display();
    void close();
}
