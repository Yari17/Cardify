package view;

import model.bean.TradeTransactionBean;

import java.util.List;

/**
 * Interfaccia per la view dello Store che gestisce gli scambi programmati in negozio.
 */
public interface IStoreTradeView extends IView {

    // Imposta il controller applicativo che gestisce il flusso degli scambi
    void setController(controller.LiveTradeController controller);

    // Mostra la lista degli scambi programmati per lo store
    void displayScheduledTrades(List<TradeTransactionBean> scheduled);

    // Mostra i dettagli di uno scambio selezionato (in una dialog o area dedicata)
    void displayTrade(TradeTransactionBean transaction);

    // Mostra la lista degli scambi in corso (inspection phase o inspection passed)
    void displayInProgressTrades(List<TradeTransactionBean> inProgress);

    // Notifica testuale liberamente utilizzabile dalla controller (es. messaggi del flusso live)
    void showMessage(String message);


}
