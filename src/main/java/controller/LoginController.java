package controller;

import config.AppConfig;
import exception.UserAlreadyExistsException;
import model.dao.IUserDao;
import model.domain.User;
import model.domain.enumerations.ViewPage;
import view.ILoginView;

/**
 * Controller per la gestione delle operazioni di autenticazione e
 * registrazione.
 * Si occupa di validare l'input dell'utente e coordinare l'accesso al sistema
 * tramite il DAO degli utenti.
 */
public class LoginController {
    private static final String EMPTY_FIELD_MESSAGE = "Campo vuoto: inserisci username e password";

    private ILoginView view;
    /**
     * Riferimento al controller principale dell'applicazione per la navigazione.
     */
    private ApplicationController appController;
    /**
     * Istanza del DAO per l'accesso ai dati degli utenti, iniettata per favorire il
     * disaccoppiamento.
     */
    private final IUserDao userDao;

    /**
     * Costruttore principale per l'iniezione delle dipendenze.
     * 
     * @param appController Il controller radice per la navigazione tra le pagine.
     * @param userDao       Il DAO per la persistenza degli utenti.
     */
    public LoginController(ApplicationController appController, IUserDao userDao) {
        this.appController = appController;
        this.userDao = userDao;
    }

    /**
     * Costruttore predefinito (mantenuto per compatibilità, ma sconsigliato).
     * Non inizializza i riferimenti necessari per il corretto funzionamento.
     */
    public LoginController() {
        this.appController = null;
        this.userDao = null;
    }

    /**
     * Associa la vista al controller per permettere l'interazione con
     * l'interfaccia.
     * 
     * @param view L'istanza della vista login (CLI o JavaFX).
     */
    public void setView(ILoginView view) {
        this.view = view;
    }

    /**
     * Imposta il controller di applicazione per gestire la navigazione post-login.
     * 
     * @param appController L'istanza del controller principale.
     */
    public void setAppController(ApplicationController appController) {
        this.appController = appController;
    }

    /**
     * Gestisce il processo di autenticazione dell'utente.
     * Verifica la validità dei campi, interroga il database tramite il DAO e,
     * in caso di successo, imposta l'utente corrente nel controller principale
     * e reindirizza alla home appropriata.
     * 
     * Utile per centralizzare la logica di login indipendentemente dal tipo di
     * vista.
     */
    public void handleLogin() {
        if (view == null)
            return;

        String username = view.getUsername();
        String password = view.getPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            view.showError(EMPTY_FIELD_MESSAGE);
            return;
        }

        try {
            User user = userDao.authenticateAndGetUser(username, password);
            if (user == null) {
                view.showLoginFailure("Credenziali non valide");
                return;
            }

            // Memorizza l'utente loggato per le schermate successive
            if (appController != null) {
                appController.setCurrentUser(user);

                // Naviga alla homepage basata sul tipo di utente
                String type = user.getUserType();
                if (AppConfig.USER_TYPE_COLLECTOR.equals(type)) {
                    appController.navigateTO(ViewPage.COLLECTOR_HOMEPAGE);
                } else if (AppConfig.USER_TYPE_STORE.equals(type)) {
                    appController.navigateTO(ViewPage.STORE_HOMEPAGE);
                } else {
                    // Fallback se il tipo utente non è riconosciuto
                    view.showLoginFailure("Tipo utente non riconosciuto");
                    return;
                }
            }

            view.showLoginSuccess();
        } catch (RuntimeException e) {
            view.showLoginFailure("Errore login: " + e.getMessage());
        }
    }

    /**
     * Avvia la registrazione di un nuovo utente come Negozio.
     * Delega la logica effettiva al metodo privato
     * {@link #registerWithType(String)}.
     */
    public void handleRegistrationAsStore() {
        registerWithType(AppConfig.USER_TYPE_STORE);
    }

    /**
     * Avvia la registrazione di un nuovo utente come Collezionista.
     * Delega la logica effettiva al metodo privato
     * {@link #registerWithType(String)}.
     */
    public void handleRegistrationAsCollector() {
        registerWithType(AppConfig.USER_TYPE_COLLECTOR);
    }

    /**
     * Esegue la logica di registrazione comune a tutti i tipi di utente.
     * Valida i campi e interagisce con il DAO per persistere il nuovo account.
     * 
     * @param userType Il ruolo da assegnare all'utente (Collezionista o Negozio).
     */
    private void registerWithType(String userType) {
        if (view == null)
            return;
        String username = view.getUsername();
        String password = view.getPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            view.showError(EMPTY_FIELD_MESSAGE);
            return;
        }

        try {
            User newUser = new User(username, password, userType);
            userDao.register(newUser);
            view.showRegistrationSuccess();
        } catch (UserAlreadyExistsException _) {
            view.showRegistrationFailure("Utente già presente");
        } catch (RuntimeException e) {
            view.showRegistrationFailure("Errore registrazione: " + e.getMessage());
        }
    }

    /**
     * Restituisce il controller di applicazione principale.
     * 
     * @return L'istanza di ApplicationController.
     */
    public ApplicationController getAppController() {
        return this.appController;
    }
}
