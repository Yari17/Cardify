package view;

import model.bean.TradeTransactionBean;

import java.util.List;


public interface IStoreTradeView extends IView {

    
    void setController(controller.LiveTradeController controller);

    
    void displayScheduledTrades(List<TradeTransactionBean> scheduled);

    void displayCompletedTrades(List<TradeTransactionBean> trades);

    
    void displayTrade(TradeTransactionBean transaction);

    
    void displayInProgressTrades(List<TradeTransactionBean> inProgress);

    
    void showMessage(String message);


}
