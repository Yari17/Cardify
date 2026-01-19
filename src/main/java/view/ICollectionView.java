package view;

import controller.CollectionController;
import model.domain.Binder;

import java.util.List;
import java.util.Map;


public interface ICollectionView extends IView {

    
    void setController(CollectionController controller);

    
    void setWelcomeMessage(String username);

    
    void displayCollection(Map<String, Binder> bindersBySet, Map<String, List<model.domain.Card>> setCardsMap);

    
    void updateCardInSet(String setId, String cardId);

    
    void setSaveButtonVisible(boolean visible);

    
    void showSuccess(String message);

    
    void showError(String message);
}
