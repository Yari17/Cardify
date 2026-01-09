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

    /**
     * Restituisce la query di ricerca dell'utente.
     * Può essere un nome di carta o un ID di set a seconda del tipo di ricerca.
     * @return la query di ricerca (nome carta o ID set)
     */
    String getSearchQuery();

    /**
     * Restituisce il tipo di ricerca corrente.
     * @return BY_NAME per ricerca per nome, BY_SET per ricerca per set
     */
    SearchType getSearchType();
}
