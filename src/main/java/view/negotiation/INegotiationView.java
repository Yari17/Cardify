package view.negotiation;

import controller.NegotiationController;
import model.bean.ProposalBean;
import view.IView;
import model.bean.CardBean;

import java.util.List;
import java.util.function.Consumer;
import javafx.stage.Stage;

public interface INegotiationView extends IView {
    void showInventory(List<CardBean> inventory);
    void showRequested(List<CardBean> requested);
    void showProposed(List<CardBean> proposed);

    void setOnCardProposed(Consumer<CardBean> onPropose);
    void setOnCardUnproposed(Consumer<CardBean> onUnpropose);
    void setOnConfirmRequested(Consumer<ProposalBean> onConfirm);

    void showConfirmationResult(boolean success, String message);

    void setController(NegotiationController controller);

    // Provide list of available stores to choose from
    void setAvailableStores(List<String> storeUsernames);

    // Optionally populate a meeting date hint (e.g., tomorrow's date)
    void setMeetingDateHint(String dateHint);

    // Optional: for FX views to get the stage from the factory. CLI implementations can ignore.
    default void setStage(Stage stage) { /* no-op by default */ }
}
