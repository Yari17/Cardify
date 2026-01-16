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

    // Registrazione dei callback: la View notifica il Controller tramite questi listener.
    void registerOnCardProposed(Consumer<CardBean> onPropose);
    void registerOnCardUnproposed(Consumer<CardBean> onUnpropose);
    void registerOnConfirmRequested(Consumer<ProposalBean> onConfirm);

    void showConfirmationResult(boolean success, String message);

    void setController(NegotiationController controller);

    // Provide list of available stores to choose from
    void setAvailableStores(List<String> storeUsernames);

    // Optionally populate a meeting date hint (e.g., tomorrow's date)
    void setMeetingDateHint(String dateHint);

    // Optionally provide a meeting time hint (e.g., default time)
    default void setMeetingTimeHint(String timeHint) { /* no-op by default */ }

    // GETTERS: permettono al Controller di leggere gli input correnti dalla View
    // (seguono la regola MVC: 'show' per la presentazione, 'get' per leggere i dati inseriti dall'utente)
    List<CardBean> getProposedCards();
    List<CardBean> getRequestedCards();
    String getSelectedStore();
    String getMeetingDateInput();
    String getMeetingTimeInput();
}
