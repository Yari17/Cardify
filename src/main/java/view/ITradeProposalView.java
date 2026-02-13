package view;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

import model.bean.CardBean;
import model.bean.UserBean;

/**
 * Interfaccia per le view di proposta di scambio.
 * 
 * Fornisce un contratto forte per la gestione delle proposte di scambio,
 * utilizzando Bean per mantenere il disaccoppiamento dal domain model
 * e garantire aderenza ai principi GRASP e MVC.
 */
public interface ITradeProposalView extends IView {

    /**
     * Visualizza la lista delle carte disponibili nella collezione dell'utente.
     * 
     * @param cards Lista di CardBean con le carte disponibili per l'offerta
     */
    void showAvailableItems(List<CardBean> cards);

    /**
     * Visualizza la lista delle carte attualmente offerte nella proposta.
     * 
     * @param cards Lista di CardBean con le carte offerte
     */
    void showOfferedItems(List<CardBean> cards);

    /**
     * Visualizza la carta target desiderata nella proposta di scambio.
     * 
     * @param card CardBean della carta richiesta
     */
    void showTargetItem(CardBean card);

    /**
     * Richiede all'utente di selezionare store, data e ora per l'incontro.
     * 
     * @param stores    Lista di store fisici disponibili
     * @param onConfirm Callback invocato quando l'utente conferma la selezione
     */
    void showMeetingDialog(List<UserBean> stores, BiConsumer<UserBean, LocalDateTime> onConfirm);

    /**
     * Mostra un messaggio di successo all'utente.
     * 
     * @param message Messaggio da visualizzare
     */
    void showSuccessMessage(String message);
}
