package view;

import model.bean.TradeTransactionBean;

import java.util.List;

public interface ICollectorTradeView extends IView {

    void displayTrade(TradeTransactionBean tradeTransaction);

    void setUsername(String username);

    void setController(controller.LiveTradeController controller);

    void displayIspection();

    void onIspectionComplete(String username);

    void onTradeComplete(String tradeId);

    void displayScheduledTrades(List<TradeTransactionBean> scheduled);

    void displayCompletedTrades(java.util.List<model.bean.TradeTransactionBean> completedTrades);
}
