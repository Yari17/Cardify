package org.example.view;

import org.example.controller.RegistrationController;

/**
 * View interface for the Registration use-case.
 * The application controller manipulates the view only via this interface.
 */
public interface IRegistrationView {
    String getUsername();
    String getPassword();
    void showInputError(String message);
    void showSuccess(String message);
    void setController(RegistrationController controller);
    void display();
    void close();
}

