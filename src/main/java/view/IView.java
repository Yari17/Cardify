package view;

/**
 * Interfaccia base per tutte le viste del sistema (CLI e JavaFX).
 * Definisce i metodi fondamentali per la gestione del ciclo di vita della UI.
 */
public interface IView {
    /** Mostra la vista all'utente. */
    void display();

    /** Chiude la vista e rilascia le risorse. */
    void close();

    /** Aggiorna i dati mostrati nella vista. */
    void refresh();

    /** Mostra un messaggio di errore all'utente. */
    void showError(String errorMessage);

    /** Associa il controller alla vista (Pattern MVC). */
    void setController(Object controller);
}
