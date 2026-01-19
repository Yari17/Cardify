package view.cli;

import controller.LoginController;
import model.bean.UserBean;
import config.InputManager;
import model.domain.enumerations.PersistenceType;
import view.ILoginView;


public class CliILoginView implements ILoginView {
    private final InputManager inputManager;
    private LoginController controller;
    private String username;
    private String password;
    private model.domain.enumerations.PersistenceType selectedPersistence = null;

    public CliILoginView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void display() {
        System.out.println("\n=== CARDIFY LOGIN (CLI) ===");
        System.out.println("1. login");
        System.out.println("2. Registrati");
        System.out.println("0. Esci");
        System.out.print("Scegli un'opzione: ");

        String choice = inputManager.readString();

        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                
                if (controller != null) {
                    controller.onRegisterRequested();
                } else {
                    System.out.println("Registrazione non disponibile: controller non impostato.");
                }
                break;
            case "0":
                System.out.println("Arrivederci!");
                close();
                System.exit(0);
                break;
            default:
                System.out.println("Opzione non valida. Riprova.");
                display();
                break;
        }
    }

    private void handleLogin() {
        System.out.print("\nUsername: ");
        this.username = inputManager.readString();

        System.out.print("Password: ");
        this.password = inputManager.readString();

        
        String appMode = config.AppConfig.getPersistenceType();
        if (!config.AppConfig.DAO_TYPE_MEMORY.equals(appMode)) {
            System.out.println("Scegli persistenza per il login: 1) JSON  2) JDBC");
            System.out.print("Scelta (1-2, default 1): ");
            String p = inputManager.readString();
            selectedPersistence = "2".equals(p) ? model.domain.enumerations.PersistenceType.JDBC : model.domain.enumerations.PersistenceType.JSON;
        } else {
            selectedPersistence = model.domain.enumerations.PersistenceType.DEMO;
        }

        if (controller != null) {
            controller.onLoginRequested();
        }

        display();
    }

    @Override
    public UserBean getUserCredentials() {
        return new UserBean(username, password);
    }

    @Override
    public PersistenceType getPersistenceType() {
        return selectedPersistence;
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
    public void setController(LoginController controller) {
        this.controller = controller;
    }

    @Override
    public void close() {
        /* not used */
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("[ERROR] " + errorMessage);
    }

    
    @Override
    public void refresh() {
        /* not used */
    }
}
