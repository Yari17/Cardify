package view;

import controller.StoreHPController;
import model.bean.TradeTransactionBean;

import java.util.List;

public interface IStoreHPView extends IView {
    void setController(StoreHPController controller);


    void showWelcomeMessage(String username);

    
    void displayCompletedTrades(List<TradeTransactionBean> completed);
}
