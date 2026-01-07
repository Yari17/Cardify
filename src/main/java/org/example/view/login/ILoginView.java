package org.example.view.login;

import org.example.model.bean.UserBean;

/**
 * View interface for login. The Application Controller uses this contract.
 * Implementations can be JavaFX, CLI, Swing, etc.
 */
public interface ILoginView {
    /**
     * Get user credentials from the view.
     * @return UserBean containing username and password
     */
    UserBean getUserCredentials();

    void showInputError(String message);
    void showSuccess(String message);
    void setController(org.example.controller.LoginController controller);
    void display();
    void close();
}
