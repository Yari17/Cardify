package controller;

import model.bean.TradeTransactionBean;
import model.dao.factory.DaoFactory;
import model.dao.json.JsonTradeDao;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveTradeControllerCompletedViewTest {

    static class CaptureCollectorView implements view.ICollectorTradeView {
        List<TradeTransactionBean> completed = new ArrayList<>();

        @Override public void displayTrade(TradeTransactionBean tradeTransaction) {}
        @Override public void setUsername(String username) {}
        @Override public void setController(controller.LiveTradeController controller) {}
        @Override public void displayIspection() {}
        @Override public void onIspectionComplete(String username) {}
        @Override public void onTradeComplete(String tradeId) {}
        @Override public void displayScheduledTrades(java.util.List<model.bean.TradeTransactionBean> scheduled) {}
        @Override public void displayCompletedTrades(java.util.List<model.bean.TradeTransactionBean> completedTrades) {
            if (completedTrades != null) completed.addAll(completedTrades);
        }
        @Override public void refresh() {}
        @Override public void showError(String errorMessage) {}
        @Override public void setStage(javafx.stage.Stage stage) {}
        @Override public void close() {}
        @Override public void display() {}
    }

    @Test
    void controllerShouldPopulateCompletedTradesFromJsonDao() {
        // Build controller with a DaoFactory that returns JsonTradeDao reading database/trades.json
        LiveTradeController controller = new LiveTradeController("user1", new ApplicationController() {
            @Override public model.dao.factory.DaoFactory getDaoFactory() {
                return new DaoFactory() {
                    @Override public model.dao.ITradeDao createTradeDao() { return new JsonTradeDao(); }
                    @Override public model.dao.IBinderDao createBinderDao() { return null; }
                    @Override public model.dao.IUserDao createUserDao() { return null; }
                    @Override public model.dao.IProposalDao createProposalDao() { return null; }
                };
            }
        });

        CaptureCollectorView view = new CaptureCollectorView();
        controller.setView(view);
        controller.loadCollectorCompletedTrades();

        boolean found = view.completed.stream().anyMatch(b -> b.getTransactionId() == 1214693586);
        assertTrue(found, "Expected completed trade 1214693586 to be present in view list");
    }
}
