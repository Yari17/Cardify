package model.bean;

import java.util.Objects;

/**
 * Bean per il trasporto dei dati di un utente tra i vari livelli
 * dell'applicazione.
 * Contiene le credenziali e il profilo dell'utente per la gestione della
 * sessione.
 */
public class UserBean {
    /** Nome utente unico. */
    private String username;
    /** Password dell'utente. */
    private String password;
    /** Tipo di utente (es. COLLECTOR, STORE). */
    private String userType;

    /**
     * Costruttore vuoto per compatibilità JavaBean.
     */
    public UserBean() {
    }

    /**
     * Costruttore completo per inizializzare rapidamente il bean.
     * 
     * @param username Nome utente.
     * @param password Password.
     * @param userType Tipo profilo.
     */
    public UserBean(String username, String password, String userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    // Getter e Setter
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserBean userBean = (UserBean) o;
        return Objects.equals(username, userBean.username) &&
                Objects.equals(password, userBean.password) &&
                Objects.equals(userType, userBean.userType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, userType);
    }
}
