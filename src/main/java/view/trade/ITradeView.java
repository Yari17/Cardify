package view.trade;

import view.IView;

/**
 * Interface for Trade view.
 */
public interface ITradeView extends IView {

    /**
     * Sets the username to display in the profile section.
     *
     * @param username the username to display
     */
    void setUsername(String username);

    /**
     * Displays the lists of trades, separated by status (Pending vs
     * Scheduled/History).
     *
     * @param pendingTrades   list of trades with PENDING status
     * @param scheduledTrades list of trades with ACCEPTED/SCHEDULED status
     */
    void displayTrades(java.util.List<model.bean.TradeBean> pendingTrades,
            java.util.List<model.bean.TradeBean> scheduledTrades);
}
