package exception;

/**
 * Exception representing a failure to reach external network resources (e.g. API) due
 * to lack of network connectivity or other connection problems.
 *
 * Use this exception from CardProvider implementations to signal the caller that
 * data couldn't be fetched because of network issues.
 */
public class ConnectionException extends RuntimeException {
    public ConnectionException(String message, Throwable cause) { super(message, cause); }
}
