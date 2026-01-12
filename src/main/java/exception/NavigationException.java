package exception;

/**
 * Exception thrown when navigation between views fails.
 */
public class NavigationException extends RuntimeException {

    public NavigationException(String message) {
        super(message);
    }

    public NavigationException(String message, Throwable cause) {
        super(message, cause);
    }
}

