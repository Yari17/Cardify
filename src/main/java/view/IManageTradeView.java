package view;

import model.bean.ProposalBean;
import java.util.List;
import java.util.function.Consumer;

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


    void setUsername(String username);



    void registerOnAccept(Consumer<String> onAccept);
    void registerOnDecline(Consumer<String> onDecline);
    void registerOnCancel(Consumer<String> onCancel);
    void registerOnTradeClick(Consumer<String> onTradeClick);
    void registerOnTradeNowClick(Consumer<String> onTradeNowClick);

}
