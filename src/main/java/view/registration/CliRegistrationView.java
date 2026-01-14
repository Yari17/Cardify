
package view.registration;

import controller.RegistrationController;
import model.bean.UserBean;
import config.InputManager;

@SuppressWarnings("java:S106")
public class CliRegistrationView implements IRegistrationView {
    private final InputManager inputManager;
    private RegistrationController controller;
    private String username;
    private String password;
    private String userType;

    public CliRegistrationView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public UserBean getUserData() {
        return new UserBean(username, password, userType);
    }

    @Override
    public void showInputError(String message) {
        System.out.println("ERROR: " + message);
    }

    @Override
    public void showSuccess(String message) {
        System.out.println("SUCCESS: " + message);
    }

    @Override
    public void setController(RegistrationController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        System.out.println("\n=== REGISTRAZIONE ===");
        System.out.print("Username (min 3 caratteri): ");
        this.username = inputManager.readString();

        System.out.print("Password (min 6 caratteri): ");
        this.password = inputManager.readString();

        System.out.println("\nSeleziona il tipo di utente:");
        System.out.println("1. " + UserBean.USER_TYPE_COLLECTOR);
        System.out.println("2. " + UserBean.USER_TYPE_STORE);
        System.out.print("Scelta (1-2): ");

        String choice = inputManager.readString();
        this.userType = "2".equals(choice)
                ? UserBean.USER_TYPE_STORE
                : UserBean.USER_TYPE_COLLECTOR;

        if (controller != null) {
            controller.onRegisterRequested();
        }
    }

    @Override
    public void close() {

    }
}
