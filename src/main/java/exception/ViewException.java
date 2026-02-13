package exception;

/**
 * Segnala un errore irreversibile durante il caricamento o l'interazione con
 * l'interfaccia grafica (View).
 * Spesso utilizzata per incapsulare IOException derivanti dal caricamento di
 * file FXML.
 */
public class ViewException extends RuntimeException {
    public ViewException(String message) {
        super(message);
    }

    public ViewException(String message, Throwable cause) {
        super(message, cause);
    }
}
