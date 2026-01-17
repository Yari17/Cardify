package view;

import controller.NegotiationController;
import model.bean.ProposalBean;
import model.bean.CardBean;

import java.util.List;
import java.util.function.Consumer;

public interface INegotiationView extends IView {
    void showInventory(List<CardBean> inventory);
    void showRequested(List<CardBean> requested);
    void showProposed(List<CardBean> proposed);


    void registerOnCardProposed(Consumer<CardBean> onPropose);
    void registerOnCardUnproposed(Consumer<CardBean> onUnpropose);
    void registerOnConfirmRequested(Consumer<ProposalBean> onConfirm);
    void showConfirmationResult(boolean success, String message);
    void setController(NegotiationController controller);

    // Provide list of available stores to choose from
    void showAvailableStores(List<String> storeUsernames);

    // Optionally populate a meeting date hint (e.g., tomorrow's date)
    void setMeetingDateHint(String dateHint);

}
