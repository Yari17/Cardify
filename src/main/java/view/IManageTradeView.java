package view;

import model.bean.ProposalBean;
import java.util.List;

/**
 * Interfaccia per la View di "Manage Trades".
 * Responsabilità della View:
 * - Mostrare le liste di proposte (pending / scheduled).
 * - Esporre un metodo per ricevere il controller applicativo (setManageController).
 *
 * La View NON deve contenere logica applicativa o di persistenza: quando
 * l'utente aziona un controllo (es. Accept/Decline/Trade), la View deve
 * delegare l'azione al controller applicativo precedentemente passato con
 * `setManageController`.
 */
public interface IManageTradeView extends IView {

    // Mostra le proposte in attesa e le proposte concluse/programmati
    void displayTrades(List<ProposalBean> pending, List<ProposalBean> scheduled);

    // Imposta l'username corrente per adattare la UI (es. evidenziare incoming proposals)
    void setUsername(String username);

    // API basata su callback: la view registra i callback che il controller
    // fornisce per le azioni utente (accept/decline/trade). Questo riduce
    // il coupling perché la view non deve conoscere il tipo concreto del controller.
    void registerOnAccept(java.util.function.Consumer<String> onAccept);
    void registerOnDecline(java.util.function.Consumer<String> onDecline);
    void registerOnCancel(java.util.function.Consumer<String> onCancel);
    void registerOnTradeClick(java.util.function.Consumer<String> onTradeClick);
    void registerOnTradeNowClick(java.util.function.Consumer<String> onTradeNowClick);

}
