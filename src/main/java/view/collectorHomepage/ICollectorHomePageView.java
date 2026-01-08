package view.collectorhomepage;

import controller.CollectorHomePageController;
import model.domain.card.Card;
import view.IView;

import java.util.List;

public interface ICollectorHomePageView extends IView {
    void setController(CollectorHomePageController controller);
    String getSearchQuery();

    void showCards(List<Card> cards);

    void showCardDetails(Card card);

    void showWelcomeMessage(String username);
}
