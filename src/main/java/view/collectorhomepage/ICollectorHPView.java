package view.collectorhomepage;

import controller.CollectorHPController;
import model.bean.CardBean;
import view.IView;

import java.util.List;
import java.util.Map;

public interface ICollectorHPView extends IView {

    enum SearchType {
        BY_NAME,
        BY_SET
    }

    void setController(CollectorHPController controller);

    void showWelcomeMessage(String username);

    void showCardOverview(CardBean card);

    void displayCards(List<CardBean> cards);

    void displayAvailableSets(Map<String, String> setsMap);

}
