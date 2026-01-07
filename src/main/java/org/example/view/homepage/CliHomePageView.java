package org.example.view.homepage;

import org.example.controller.HomePageController;

import java.util.Scanner;

/**
 * CLI implementation of the HomePage view.
 */
public class CliHomePageView implements IHomePageView {
    private final Scanner scanner;
    private HomePageController controller;

    public CliHomePageView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setController(HomePageController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        if (controller == null) {
            System.out.println("ERROR: Controller not set");
            return;
        }

        showWelcomeMessage(controller.getUsername());

        boolean running = true;
        while (running) {
            System.out.println("\n=== CARDIFY HOME PAGE ===");
            System.out.println("1. Gestisci collezione");
            System.out.println("2. Scambia carte");
            System.out.println("3. Visualizza profilo");
            System.out.println("4. Logout");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Funzionalità in sviluppo...");
                    break;
                case "2":
                    System.out.println("Funzionalità in sviluppo...");
                    break;
                case "3":
                    System.out.println("Profilo utente: " + controller.getUsername());
                    break;
                case "4":
                    controller.onLogoutRequested();
                    running = false;
                    break;
                case "0":
                    controller.onExitRequested();
                    running = false;
                    break;
                default:
                    System.out.println("Opzione non valida. Riprova.");
            }
        }
    }

    @Override
    public void close() {
        // No operation for CLI
    }

    @Override
    public void showWelcomeMessage(String username) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   Benvenuto in CARDIFY, " + username + "!   ║");
        System.out.println("╚════════════════════════════════════╝");
    }
}

