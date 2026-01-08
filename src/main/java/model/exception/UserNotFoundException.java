package model.exception;

public class UserNotFoundException extends DaoException {
    public UserNotFoundException(String username) {
        super("User not found: " + username);
    }
}
