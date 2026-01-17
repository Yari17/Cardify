package view;

import controller.StoreHPController;
import model.bean.TradeTransactionBean;

import java.util.List;

public interface IStoreHPView extends IView {
    void setController(StoreHPController controller);


    void showWelcomeMessage(String username);

    // Mostra la lista degli scambi conclusi (COMPLETED o CANCELLED) relativi a questo store
    void displayCompletedTrades(List<TradeTransactionBean> completed);
}
