package view;

import model.bean.TradeSessionBean;

import java.util.List;

public interface ICollectorTradeView extends IView {
    void showTradeLists(List<TradeSessionBean> activeTrades, List<TradeSessionBean> completedTrades);

    void showTradeDetails(TradeSessionBean sessionBean, String userCode, String partnerName);

    /**
     * Registra conferma presenza utente per la sessione corrente.
     * Genera codice sessione.
     */
    void registerConfirmPresence();

    /**
     * Mostra il codice sessione corrente all'utente.
     */
    void showSessionCode();
}
