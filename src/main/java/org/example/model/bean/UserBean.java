package org.example.model.bean;

import java.util.Objects;

/**
 * Bean representing user data.
 * Simple data object following JavaBean conventions.
 * Contains validation logic following the Information Expert principle (GRASP).
 */
public class UserBean {
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    public static final String USER_TYPE_COLLECTOR = "Collezionista";
    public static final String USER_TYPE_STORE = "Store";

    private String username;
    private String password;
    private String userType;

    public UserBean() {
    }

    public UserBean(String username, String password) {
        this.username = username;
        this.password = password;
        this.userType = USER_TYPE_COLLECTOR; // Default
    }

    public UserBean(String username, String password, String userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * Validates the user data.
     * Checks if username, password and userType meet the syntactic requirements.
     *
     * @return true if data is valid, false otherwise
     */
    public boolean isValid() {
        return isUsernameValid() && isPasswordValid() && isUserTypeValid();
    }

    /**
     * Validates username syntax.
     * Username must not be null, not empty and have minimum length.
     *
     * @return true if username is valid
     */
    public boolean isUsernameValid() {
        return username != null
            && !username.trim().isEmpty()
            && username.length() >= MIN_USERNAME_LENGTH;
    }

    /**
     * Validates password syntax.
     * Password must not be null, not empty and have minimum length.
     *
     * @return true if password is valid
     */
    public boolean isPasswordValid() {
        return password != null
            && !password.trim().isEmpty()
            && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Validates userType.
     * UserType must be either COLLECTOR or STORE.
     *
     * @return true if userType is valid
     */
    public boolean isUserTypeValid() {
        return userType != null
            && (USER_TYPE_COLLECTOR.equals(userType) || USER_TYPE_STORE.equals(userType));
    }

    /**
     * Returns a detailed validation error message.
     *
     * @return error message describing what's wrong, or null if valid
     */
    public String getValidationError() {
        if (username == null || username.trim().isEmpty()) {
            return "Il campo username non può essere vuoto";
        }

        if (username.length() < MIN_USERNAME_LENGTH) {
            return "L'username deve contenere almeno " + MIN_USERNAME_LENGTH + " caratteri";
        }

        if (password == null || password.trim().isEmpty()) {
            return "Il campo password non può essere vuoto";
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "La password deve contenere almeno " + MIN_PASSWORD_LENGTH + " caratteri";
        }

        if (!isUserTypeValid()) {
            return "Devi selezionare un tipo di utente (Collezionista o Store)";
        }

        return null; // No errors
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBean userBean = (UserBean) o;
        return Objects.equals(username, userBean.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public String toString() {
        return "UserBean{username='" + username + "', userType='" + userType + "'}";
        // Password omessa intenzionalmente per sicurezza
    }
}
