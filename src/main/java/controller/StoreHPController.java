package controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import view.IStoreHPView;

public class StoreHPController {
    private static final Logger LOGGER = Logger.getLogger(StoreHPController.class.getName());

    private final String username;
    private final ApplicationController navigationController;
    private IStoreHPView view;

    public StoreHPController(String username, ApplicationController navigationController) {
        this.username = username;
        this.navigationController = navigationController;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Associa la view grafica a questo controller.
     * Metodo chiamato dal ApplicationController quando si crea la view.
     */
    public void setView(IStoreHPView view) {
        this.view = view;
        // Imposta anche il controller sulla view (la view sa come invocare il controller)
        if (this.view != null) {
            this.view.setController(this);
            this.view.showWelcomeMessage(username);
        }
    }

    /**
     * Richiesta di logout da parte dell'utente store.
     * Delegata al ApplicationController per la navigazione.
     */
    public void onLogoutRequested() {
        LOGGER.log(Level.INFO, "Store user {0} logging out", username);
        navigationController.logout();
    }

    /**
     * Richiesta di uscita dall'applicazione.
     */
    public void onExitRequested() {
        System.exit(0);
    }

    /**
     * Metodo chiamato dalla view quando l'utente preme "Gestisci scambi".
     * Qui il controller grafico non accede a DAO o logica di business: delega
     * la navigazione al ApplicationController che si occupa di mostrare la
     * view dei Live Trades/Manage Trades.
     */
    public void onManageTradesRequested() {
        LOGGER.log(Level.INFO, "StoreHP: richiesta di gestione scambi per {0}", username);
        // Delego la navigazione al controller dell'applicazione
        navigationController.navigateToStoreTrades(username);
    }
}
