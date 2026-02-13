package exception;

/**
 * Segnala un conflitto durante la fase di registrazione di un nuovo utente.
 * Viene lanciata se l'identificativo (username) scelto è già presente nel
 * sistema di persistenza.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
