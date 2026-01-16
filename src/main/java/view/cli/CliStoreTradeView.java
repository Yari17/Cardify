package view.cli;

import view.IStoreTradeView;
import model.bean.TradeTransactionBean;
import controller.LiveTradeController;

import java.util.List;

public class CliStoreTradeView implements IStoreTradeView {
    private LiveTradeController controller;

    @Override
    public void setController(controller.LiveTradeController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        // semplice implementazione CLI: mostra lista e prompt
        if (controller != null) controller.loadScheduledTrades();
    }

    @Override
    public void close() {}

    @Override
    public void refresh() {
        if (controller != null) controller.loadScheduledTrades();
    }

    @Override
    public void showError(String errorMessage) {
        System.err.println("Error: " + errorMessage);
    }

    @Override
    public void displayScheduledTrades(List<TradeTransactionBean> scheduled) {
        System.out.println("Scheduled trades for store:");
        if (scheduled == null || scheduled.isEmpty()) System.out.println(" Nessuno");
        else for (TradeTransactionBean t : scheduled) System.out.println(" - " + t.getTransactionId() + " [" + (t.getStatus()!=null?t.getStatus():"?") + "]");
    }

    @Override
    public void displayTrade(TradeTransactionBean transaction) {
        System.out.println("Trade details: " + transaction.getTransactionId());
    }

    @Override
    public void showMessage(String message) { System.out.println(message); }
}
