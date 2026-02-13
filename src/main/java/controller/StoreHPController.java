package controller;

import model.dao.factory.DaoFactory;
import model.domain.User;
import view.IStoreHPView;
import view.IView;

import java.util.List;

/**
 * Controller per la Homepage del Negozio.
 * Gestisce la visualizzazione delle transazioni (in corso, programmate e
 * storiche)
 * relative a uno specifico punto vendita.
 */
public class StoreHPController {
    /** Controller principale per la gestione della navigazione. */
    private final ApplicationController applicationController;
    /** L'utente gestore del negozio attualmente loggato. */
    private final User currentUser;
    /** Vista associata alla homepage del negozio. */
    private IStoreHPView view;
    /** DAO per il recupero delle sessioni di scambio associate al negozio. */
    private model.dao.ITradeSessionDao tradeSessionDao;

    /**
     * Costruttore del controller per il negozio.
     * 
     * @param applicationController Il controller radice.
     * @param user                  L'utente che rappresenta il negozio.
     */
    public StoreHPController(ApplicationController applicationController, User user) {
        this.applicationController = applicationController;
        this.currentUser = user;
        // Inizializza il DAO basandosi sulla configurazione globale di persistenza
        this.tradeSessionDao = DaoFactory.getFactory(config.AppConfig.getPersistenceType()).createTradeDao();
    }

    /**
     * Associa la vista al controller.
     * 
     * @param storeView L'istanza della vista negozio.
     */
    public void setView(IView storeView) {
        this.view = (IStoreHPView) storeView;
    }

    /**
     * Carica tutti i dati relativi al negozio dal database e aggiorna la vista.
     * Recupera separatamente gli scambi in corso, programmati e completati,
     * delegando la conversione in bean al mapper
     * {@link model.bean.mapper.TradeSessionMapper}.
     */
    public void loadStoreData() {
        if (view == null)
            return;

        view.setStoreName(currentUser.getUsername());

        if (tradeSessionDao == null)
            return;

        // Recupero transazioni filtrate per stato e per il negozio corrente
        List<model.domain.TradeSession> ongoingSessions = tradeSessionDao
                .getStoreInProgressTrades(currentUser);
        List<model.domain.TradeSession> scheduledSessions = tradeSessionDao
                .getStoreScheduledTrades(currentUser);
        List<model.domain.TradeSession> historySessions = tradeSessionDao.getStoreCompletedTrades(currentUser);

        // Aggiornamento della vista con i bean trasformati (Information Expert)
        view.showOngoingTrades(model.bean.mapper.TradeSessionMapper.toBeanList(ongoingSessions));
        view.showScheduledTrades(model.bean.mapper.TradeSessionMapper.toBeanList(scheduledSessions));
        view.showHistoryTrades(model.bean.mapper.TradeSessionMapper.toBeanList(historySessions));
    }

    /**
     * Gestisce l'apertura dei dettagli di una specifica sessione di scambio.
     * Memorizza temporaneamente l'ID della sessione nel controller principale
     * prima di navigare alla pagina di dettaglio.
     * 
     * @param sessionBean Il bean della sessione selezionata.
     */
    public void openTradeDetails(model.bean.TradeSessionBean sessionBean) {
        if (applicationController != null) {
            applicationController.setTemporaryData("TRADE_SESSION_ID", sessionBean.getTransactionId());
            applicationController.navigateTO(model.domain.enumerations.ViewPage.STORE_TRADE);
        }
    }

    /**
     * Esegue il logout dell'utente e reindirizza alla schermata iniziale.
     */
    public void logout() {
        applicationController.logout();
    }
}
