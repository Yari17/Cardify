package view.cli;

import controller.RegistrationController;
import model.bean.UserBean;
import config.InputManager;
import view.IRegistrationView;

@SuppressWarnings("java:S106")
public class CliRegistrationView implements IRegistrationView {
    private final InputManager inputManager;
    private RegistrationController controller;
    private String username;
    private String password;
    private String userType;
    private model.domain.enumerations.PersistenceType selectedPersistence;

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
        System.out.println("0. Back (torna al login)");
        System.out.print("Scelta (1-2): ");

        String choice = inputManager.readString();
        if ("0".equals(choice)) {
            if (controller != null) controller.onBackToLoginRequested();
            return;
        }
        this.userType = "2".equals(choice)
                ? UserBean.USER_TYPE_STORE
                : UserBean.USER_TYPE_COLLECTOR;

        // If application not in demo mode, ask which persistence to use for registration
        if (!config.AppConfig.DAO_TYPE_MEMORY.equals(config.AppConfig.getPersistenceType())) {
            System.out.println("Scegli persistenza per la registrazione: 1) JSON  2) JDBC (MySQL)  3) DEMO");
            System.out.print("Scelta (1-3, default 1): ");
            String p = inputManager.readString();
            switch (p) {
                case "2" -> selectedPersistence = model.domain.enumerations.PersistenceType.JDBC;
                case "3" -> selectedPersistence = model.domain.enumerations.PersistenceType.DEMO;
                default -> selectedPersistence = model.domain.enumerations.PersistenceType.JSON;
            }
        } else {
            selectedPersistence = model.domain.enumerations.PersistenceType.DEMO;
        }

        if (controller != null) {
            controller.onRegisterRequested();
        }
    }

    @Override
    public void close() {
        System.out.println("Chiusura della vista di registrazione.");
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("ERROR: " + errorMessage);
    }

    @Override
    public void refresh() {
        // CLI: refresh is a no-op for interactive views; caller may invoke display().
    }

    @Override
    public void setStage(javafx.stage.Stage stage) {
        // CLI does not use JavaFX stages, method present for interface compatibility.
    }

    @Override
    public model.domain.enumerations.PersistenceType getPersistenceType() {
        return selectedPersistence != null ? selectedPersistence : model.domain.enumerations.PersistenceType.JSON;
    }
}
