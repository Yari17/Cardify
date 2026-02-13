package view;

/**
 * Interfaccia per la vista di autenticazione e registrazione.
 */
public interface ILoginView extends IView {
    /** Recupera lo username inserito dall'utente. */
    String getUsername();

    /** Recupera la password inserita dall'utente. */
    String getPassword();

    /** Notifica il successo del login. */
    void showLoginSuccess();

    /** Notifica il fallimento del login con un messaggio specifico. */
    void showLoginFailure(String errorMessage);

    /** Notifica il successo della registrazione. */
    void showRegistrationSuccess();

    /** Notifica il fallimento della registrazione con un messaggio specifico. */
    void showRegistrationFailure(String errorMessage);
}
