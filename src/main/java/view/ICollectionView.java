package view;

import model.bean.BinderBean;
import model.bean.CardBean;

import java.util.List;
import java.util.Map;

public interface ICollectionView extends IView {
    void displayUserBinders(List<BinderBean> binders);

    void onCreateBinder();

    void onAddCard(CardBean card);

    void onRemoveCard(CardBean card);

    void setSaveButtonVisible(boolean isVisible);

    void showAvailableSets(Map<String, String> availableSets);
}
