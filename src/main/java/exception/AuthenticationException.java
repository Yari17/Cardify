package exception;

public class AuthenticationException extends DaoException {

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

