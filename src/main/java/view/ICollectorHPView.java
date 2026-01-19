package view;

import controller.CollectorHPController;
import model.bean.CardBean;

import java.util.List;
import java.util.Map;

public interface ICollectorHPView extends IView {

    void setController(CollectorHPController controller);

    void showWelcomeMessage(String username);

    void showCardOverview(CardBean card);

    void displayCards(List<CardBean> cards);

    void displayAvailableSets(Map<String, String> setsMap);

    
    void showSuccess(String message);
    void showError(String message);

}
