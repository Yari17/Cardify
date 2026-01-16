package view;

import model.bean.TradeTransactionBean;

/**
 * Interfaccia per la View di Live Trades (scambi programmati).
 * Responsabilità della View:
 * - Visualizzare i dettagli di uno scambio o la lista degli scambi schedulati.
 * - Fornire un metodo per ricevere il controller (`setController`) e delegare
 *   le azioni (confirm presence, start trade, etc.) al controller applicativo.
 */
public interface ILiveTradeView extends IView {

    // Mostra i dettagli di uno scambio (usato per aprire dialog sullo scambio)
    void displayTrade(TradeTransactionBean tradeTransaction);

    // Imposta username per la personalizzazione della view
    void setUsername(String username);

    // Fornisce il controller associato alla view; la view deve delegare le azioni al controller
    void setController(controller.LiveTradeController controller);

    // Eventi di presentazione: mostra l'interfaccia per ispezione (es. store manager)
    void displayIspection();

    // Mostra la lista di scambi schedulati per questo utente
    void displayScheduledTrades(java.util.List<model.bean.TradeTransactionBean> scheduled);

}
