package view.cli;

import controller.LoginController;
import view.ILoginView;

import java.util.Scanner;

/**
 * Implementazione CLI della vista di login e registrazione.
 * Gestisce l'interazione testuale con l'utente tramite standard input/output.
 */
public class CliLoginView implements ILoginView {
    private static final String PROMPT_USERNAME = "Username: ";
    private static final String PROMPT_PASSWORD = "Password: ";
    private static final String MSG_NO_CONTROLLER = "Controller non impostato.";

    private LoginController loginController;
    private String username = "";
    private String password = "";

    /** Flag per gestire il loop del menu CLI fino al successo del login. */
    private boolean loginSucceeded = false;

    /**
     * Scanner per la lettura degli input, mantenuto come campo per evitare leak.
     */
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void showLoginSuccess() {
        System.out.println("Login effettuato con successo.");
        this.loginSucceeded = true;
    }

    @Override
    public void showLoginFailure(String errorMessage) {
        System.out.println("Login fallito: " + errorMessage);
    }

    @Override
    public void showRegistrationSuccess() {
        System.out.println("Registrazione completata con successo. Torna al login.");
    }

    @Override
    public void showRegistrationFailure(String errorMessage) {
        System.out.println("Registrazione fallita: " + errorMessage);
    }

    /**
     * Avvia il loop principale del menu di login.
     * Continua a mostrare le opzioni finché l'utente non effettua l'accesso con
     * successo.
     */
    @Override
    public void display() {
        while (!loginSucceeded) {
            showMenu();
            String choice = scanner.nextLine();
            handleChoice(choice);
            System.out.println();
        }
    }

    /**
     * Mostra le opzioni disponibili nel menu di autenticazione.
     * Stampa a video le scelte per login e registrazione.
     */
    private void showMenu() {
        System.out.println("=== Cardify CLI - Login / Registrazione ===");
        System.out.println("1) Login");
        System.out.println("2) Registrati come Collezionista");
        System.out.println("3) Registrati come Store");
        System.out.print("Scegli un'opzione: ");
    }

    /**
     * Gestisce la scelta dell'utente dal menu principale.
     * 
     * @param choice L'opzione selezionata.
     */
    private void handleChoice(String choice) {
        switch (choice) {
            case "1" -> handleLogin();
            case "2" -> handleRegistration(this::delegateCollectorRegistration);
            case "3" -> handleRegistration(this::delegateStoreRegistration);
            default -> System.out.println("Opzione non valida.");
        }
    }

    /**
     * Gestisce il flusso di login.
     * Legge le credenziali e delega al controller l'autenticazione.
     */
    private void handleLogin() {
        readCredentials();
        delegateToController(this::delegateLogin);
    }

    /**
     * Gestisce il flusso di registrazione.
     * Legge le credenziali e delega al controller la registrazione specifica.
     * 
     * @param registrationAction L'azione di registrazione da eseguire
     *                           (Collezionista/Store).
     */
    private void handleRegistration(Runnable registrationAction) {
        readCredentials();
        delegateToController(registrationAction);
    }

    /**
     * Legge username e password da standard input.
     * I valori letti vengono salvati nei campi della classe.
     */
    private void readCredentials() {
        System.out.print(PROMPT_USERNAME);
        this.username = scanner.nextLine().trim();
        System.out.print(PROMPT_PASSWORD);
        this.password = scanner.nextLine().trim();
    }

    /**
     * Esegue un'azione delegata al controller verificandone prima l'esistenza.
     * 
     * @param action L'azione Runnable da eseguire.
     */
    private void delegateToController(Runnable action) {
        if (loginController != null) {
            action.run();
        } else {
            System.out.println(MSG_NO_CONTROLLER);
        }
    }

    /**
     * Metodo helper per invocare il login sul controller.
     */
    private void delegateLogin() {
        loginController.handleLogin();
    }

    /**
     * Metodo helper per invocare la registrazione come Collezionista.
     */
    private void delegateCollectorRegistration() {
        loginController.handleRegistrationAsCollector();
    }

    /**
     * Metodo helper per invocare la registrazione come Store.
     */
    private void delegateStoreRegistration() {
        loginController.handleRegistrationAsStore();
    }

    @Override
    public void close() {
        // nessuna operazione per la CLI - lo scanner su System.in non deve essere
        // chiuso
    }

    @Override
    public void refresh() {
        // nessuna operazione per la CLI
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("Errore: " + errorMessage);
    }

    @Override
    public void setController(Object controller) {
        this.loginController = (LoginController) controller;
    }
}
