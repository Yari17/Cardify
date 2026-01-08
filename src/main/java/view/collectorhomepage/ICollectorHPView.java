package view.collectorhomepage;

import controller.CollectorHPController;
import model.bean.CardBean;
import view.IView;

import java.util.List;

public interface ICollectorHPView extends IView {
    void setController(CollectorHPController controller);

    String getSearchQuery();

    void showWelcomeMessage(String username);

    void displayCards(List<CardBean> cards);
}
