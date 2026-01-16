package view.trade;

import model.bean.TradeTransactionBean;
import view.IView;

/**
 * Interface for Trade view.
 */
public interface ILiveTradeView extends IView {

    void displayTrade(TradeTransactionBean tradeTransaction);
    void setUsername(String username);
    void onConfirmPresence(String collectorId); //deve essere fatto da tutti e due i collezionisti
    void displayIspection();// serve per far ispezionare allo store manager le carte coinvolte nello scambio
    void onIspectionComplete(String username);
    void onTradeComplete(String tradeId);

    // Display the list of scheduled/pending live trades for this user
    void displayScheduledTrades(java.util.List<model.bean.TradeTransactionBean> scheduled);

}
