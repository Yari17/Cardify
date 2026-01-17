package controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import view.IStoreHPView;

public class StoreHPController {
    private static final Logger LOGGER = Logger.getLogger(StoreHPController.class.getName());

    private final String username;
    private final ApplicationController navigationController;
    private view.IStoreHPView view;

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
        if (view != null) {
            view.setController(this);
            view.showWelcomeMessage(username);
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

    /**
     * Metodo chiamato dalla view quando l'utente vuole vedere gli scambi conclusi.
     */
    public void onViewCompletedTradesRequested() {
        LOGGER.log(Level.INFO, "StoreHP: richiesta di visualizzazione scambi conclusi per {0}", username);
        // Load completed trades and show them inside the Store Home Page (no navigation)
        loadCompletedTrades();
    }

    /**
     * Carica gli scambi conclusi per lo store e richiede alla view di mostrarli.
     */
    public void loadCompletedTrades() {
        try {
            model.dao.ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            java.util.List<model.domain.TradeTransaction> list = tradeDao.getStoreCompletedTrades(username);
            java.util.List<model.bean.TradeTransactionBean> beans = new java.util.ArrayList<>();
            if (list != null) {
                for (model.domain.TradeTransaction t : list) {
                    if (t == null) continue;
                    model.bean.TradeTransactionBean b = new model.bean.TradeTransactionBean();
                    b.setTransactionId(t.getTransactionId());
                    b.setProposerId(t.getProposerId());
                    b.setReceiverId(t.getReceiverId());
                    b.setStoreId(t.getStoreId());
                    b.setTradeDate(t.getTradeDate());
                    b.setStatus(t.getTradeStatus() != null ? t.getTradeStatus().name() : null);
                    beans.add(b);
                }
            }
            // Delegate alla view
            // view may be null if not set yet
            // Use try/catch to avoid breaking navigation in case of view errors
            try {
                if (view != null) view.displayCompletedTrades(beans);
            } catch (Exception ex) {
                LOGGER.warning(() -> "Displaying completed trades failed: " + ex.getMessage());
            }
        } catch (Exception ex) {
            LOGGER.warning(() -> "loadCompletedTrades failed: " + ex.getMessage());
        }
    }
}
