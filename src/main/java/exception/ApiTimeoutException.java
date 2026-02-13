package exception;

/**
 * Eccezione lanciata quando una richiesta verso un'API esterna (es. Pokemon TCG
 * API)
 * supera il limite di tempo massimo consentito.
 */
public class ApiTimeoutException extends RuntimeException {
    public ApiTimeoutException(String message) {
        super(message);
    }

    public ApiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
