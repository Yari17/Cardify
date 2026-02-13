package org.example;

import config.AppConfig;
import controller.ApplicationController;
import model.domain.enumerations.PersistenceType;
import model.domain.enumerations.ViewType;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Classe principale dell'applicazione Cardify.
 * Gestisce il punto di ingresso del sistema, la configurazione iniziale della
 * persistenza
 * e del tipo di interfaccia utente (CLI o JavaFX).
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Punto di ingresso principale dell'applicazione.
     * Coordina l'acquisizione delle preferenze dell'utente, l'inizializzazione del
     * toolkit grafico
     * se necessario e l'avvio del controller principale dell'applicazione.
     * 
     * Il flusso delega la configurazione iniziale ai metodi helper
     * {@code askPersistence} e
     * {@code askUiType} per mantenere la separazione delle responsabilità.
     *
     * @param args Argomenti passati da riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            askPersistence(reader);
            askUiType(reader);

            // Inizializza il toolkit JavaFX se l'utente ha selezionato l'interfaccia
            // grafica.
            // Questa operazione è necessaria prima di caricare qualsiasi risorsa FXML.
            if (AppConfig.getUiType() == ViewType.JAVAFX) {
                initializeJavaFX();
            }

            ApplicationController applicationController = new ApplicationController();
            applicationController.start();

        } catch (Exception ex) {
            LOGGER.severe("Errore durante l'avvio dell'applicazione: " + ex.getMessage());
        }
    }

    /**
     * Gestisce l'interazione con l'utente per selezionare la modalità di
     * persistenza dei dati.
     * Delega la logica di input generica al metodo helper {@code askSelection}.
     *
     * @param reader Il {@link BufferedReader} utilizzato per leggere l'input da
     *               console.
     */
    private static void askPersistence(BufferedReader reader) {
        askSelection(reader, "Seleziona il tipo di persistenza dei dati: \n0: demo\n1: standard",
                "Numero massimo di tentativi superato per la selezione della persistenza.",
                input -> {
                    switch (input) {
                        case "0" -> {
                            AppConfig.setPersistenceType(PersistenceType.DEMO);
                            System.out.println("Persistenza impostata: demo");
                        }
                        case "1" -> {
                            AppConfig.setPersistenceType(PersistenceType.JDBC);
                            System.out.println("Persistenza impostata: standard (JDBC)");
                        }
                        default -> throw new IllegalArgumentException();
                    }
                });
    }

    /**
     * Gestisce l'interazione con l'utente per selezionare il tipo di interfaccia
     * utente.
     * Delega la logica di input generica al metodo helper {@code askSelection}.
     *
     * @param reader Il {@link BufferedReader} utilizzato per leggere l'input da
     *               console.
     */
    private static void askUiType(BufferedReader reader) {
        askSelection(reader, "Seleziona il tipo di interfaccia utente: \n0: CLI\n1: JavaFX",
                "Numero massimo di tentativi superato per la selezione dell'interfaccia utente.",
                input -> {
                    switch (input) {
                        case "0" -> {
                            AppConfig.setUiType(ViewType.CLI);
                            System.out.println("Interfaccia impostata: CLI");
                        }
                        case "1" -> {
                            AppConfig.setUiType(ViewType.JAVAFX);
                            System.out.println("Interfaccia impostata: JavaFX");
                        }
                        default -> throw new IllegalArgumentException();
                    }
                });
    }

    /**
     * Metodo helper generico per gestire la selezione dell'utente con un limite di
     * tentativi.
     * Incapsula la logica di loop, lettura input e gestione degli errori per
     * evitare duplicazioni
     * nei metodi che richiedono scelte multiple.
     *
     * @param reader   Il {@link BufferedReader} per l'input.
     * @param prompt   Il messaggio da visualizzare all'utente.
     * @param errorMsg Il messaggio di errore da lanciare in caso di fallimento
     *                 prolungato.
     * @param action   La logica da eseguire sul valore inserito (tipicamente un
     *                 Consumer che aggiorna AppConfig).
     * @throws IllegalStateException Se viene superato il numero massimo di
     *                               tentativi (3).
     */
    private static void askSelection(BufferedReader reader, String prompt, String errorMsg,
            java.util.function.Consumer<String> action) {
        System.out.println(prompt);
        int attempts = 0;
        while (attempts < 3) {
            try {
                String input = reader.readLine();
                if (input != null && !input.isBlank()) {
                    input = input.trim().split("\\s+")[0];
                    if (!input.isEmpty() && attemptAction(action, input)) {
                        return;
                    }
                }
                attempts++;
            } catch (IOException e) {
                LOGGER.severe("Errore durante la lettura dell'input: " + e.getMessage());
                attempts++;
            }
        }
        throw new IllegalStateException(errorMsg);
    }

    /**
     * Tenta di eseguire un'azione basata sull'input dell'utente.
     * Gestisce il feedback in caso di input non conforme ai valori previsti (0 o
     * 1).
     *
     * @param action L'azione da eseguire.
     * @param input  La stringa di input da validare/elaborare.
     * @return {@code true} se l'azione è stata completata con successo,
     *         {@code false} altrimenti.
     */
    private static boolean attemptAction(java.util.function.Consumer<String> action, String input) {
        try {
            action.accept(input);
            return true;
        } catch (IllegalArgumentException _) {
            System.out.println("Input non valido. Inserisci 0 o 1.");
            return false;
        }
    }

    /**
     * Avvia il toolkit JavaFX in fase di bootstrap.
     * Questo metodo garantisce che i servizi della piattaforma siano pronti prima
     * dell'interazione
     * con i componenti grafici. Se il toolkit è già attivo, l'eccezione viene
     * gestita silenziosamente.
     */
    private static void initializeJavaFX() {
        try {
            Platform.startup(() -> {
                // Toolkit inizializzato con successo
            });
        } catch (IllegalStateException _) {
            // Toolkit già inizializzato, l'errore può essere ignorato in questo contesto di
            // avvio
        }
    }
}
