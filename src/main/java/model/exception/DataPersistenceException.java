package model.exception;

public class DataPersistenceException extends DaoException {
    public DataPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}

