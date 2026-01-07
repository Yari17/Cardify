package model.dao.exception;

public class UserAlreadyExistsException extends DaoException {
    public UserAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }
}

