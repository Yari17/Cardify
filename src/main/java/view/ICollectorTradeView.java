package view;

import model.bean.TradeTransactionBean;

import java.util.List;

/**
 * Interfaccia per la View di Live Trades (scambi programmati) destinata al collezionista.
 * La view del collezionista non deve esporre metodi specifici della modalità store.
 */
public interface ICollectorTradeView extends IView {

    // Mostra i dettagli di uno scambio (usato per aprire dialog sullo scambio)
    void displayTrade(TradeTransactionBean tradeTransaction);

    // Imposta username per la personalizzazione della view
    void setUsername(String username);

    // Fornisce il controller associato alla view; la view deve delegare le azioni al controller
    void setController(controller.LiveTradeController controller);

    // Eventi di presentazione: mostra l'interfaccia per ispezione (es. store manager)
    void displayIspection();

    // Invocato dal controller quando l'ispezione è completata per mostrare feedback
    void onIspectionComplete(String username);

    // Invocato dal controller quando lo scambio è completato per mostrare feedback
    void onTradeComplete(String tradeId);

    // Mostra la lista di scambi schedulati per questo utente
    void displayScheduledTrades(List<TradeTransactionBean> scheduled);

    /**
     * Mostra la lista degli scambi conclusi (COMPLETED o CANCELLED) nella view del collezionista.
     */
    void displayCompletedTrades(java.util.List<model.bean.TradeTransactionBean> completedTrades);
}
