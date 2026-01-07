package org.example.view;

/**
 * View interface for the Login use-case.
 * Application controller manipulates the view only via this interface.
 */
public interface ILoginView {
    String getUsername();
    String getPassword();
    void showInputError(String message);
    void showSuccess(String message);
    void setController(org.example.controller.LoginController controller);
    void display();
    void close();
}
