package org.example.view.login;

import org.example.controller.LoginController;
import org.example.model.bean.UserBean;

import java.util.Scanner;

public class CliILoginView implements ILoginView {
    private final Scanner scanner;
    private LoginController controller;
    private String username;
    private String password;

    public CliILoginView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void display() {
        System.out.println("\n=== CARDIFY LOGIN (CLI) ===");
        System.out.println("1. login");
        System.out.println("2. Registrati");
        System.out.println("0. Esci");
        System.out.print("Scegli un'opzione: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegistration();
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
        this.username = scanner.nextLine();

        System.out.print("Password: ");
        this.password = scanner.nextLine();

        if (controller != null) {
            controller.onLoginRequested();
        }

        // Return to menu
        display();
    }

    private void handleRegistration() {
        System.out.println("\n=== REGISTRAZIONE ===");
        System.out.print("Username (min 3 caratteri): ");
        this.username = scanner.nextLine();

        System.out.print("Password (min 6 caratteri): ");
        this.password = scanner.nextLine();

        // For CLI, we need to directly call the DAO
        // This is a limitation of the current architecture
        // In a real scenario, we'd have a RegistrationController that accepts CLI input
        System.out.println("La registrazione via CLI non è ancora completamente implementata.");
        System.out.println("Usa l'interfaccia JavaFX per registrarti.");

        // Return to menu
        display();
    }

    @Override
    public UserBean getUserCredentials() {
        return new UserBean(username, password);
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
        scanner.close();
    }
}

