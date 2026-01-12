package controller;

import model.bean.UserBean;
import view.trade.ITradeView;

import java.util.logging.Logger;

public class TradeController {
    private static final Logger LOGGER = Logger.getLogger(TradeController.class.getName());

    private final String username;
    private final ApplicationController navigationController;
    private ITradeView view;

    public TradeController(String username, ApplicationController navigationController) {
        this.username = username;
        this.navigationController = navigationController;
    }

    public void setView(ITradeView view) {
        this.view = view;
        if (view != null) {
            view.setUsername(username);
        }
    }

    public void loadTrades() {
        LOGGER.log(java.util.logging.Level.INFO, "Loading trades (MOCK data) for user: {0}", username);

        // Mock Pending Trades
        java.util.List<model.bean.TradeBean> pendingTrades = new java.util.ArrayList<>();

        model.bean.TradeBean t1 = new model.bean.TradeBean();
        t1.setId("TRD-101");
        t1.setSenderUsername("AshKetchum");
        t1.setReceiverUsername(username);
        t1.setStatus(model.domain.TradeStatus.PENDING);
        t1.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
        // Simple mock card
        model.bean.CardBean c1 = new model.bean.CardBean("base1-4", "Charizard", null,
                model.domain.CardGameType.POKEMON);
        t1.setOfferedCards(java.util.Collections.singletonList(c1));
        pendingTrades.add(t1);

        model.bean.TradeBean t2 = new model.bean.TradeBean();
        t2.setId("TRD-102");
        t2.setSenderUsername("Misty");
        t2.setReceiverUsername(username);
        t2.setStatus(model.domain.TradeStatus.PENDING);
        t2.setCreatedAt(java.time.LocalDateTime.now().minusHours(5));
        model.bean.CardBean c2 = new model.bean.CardBean("base1-15", "Venusaur", null,
                model.domain.CardGameType.POKEMON);
        t2.setOfferedCards(java.util.Collections.singletonList(c2));
        pendingTrades.add(t2);

        // Mock Scheduled/Accepted Trades
        java.util.List<model.bean.TradeBean> scheduledTrades = new java.util.ArrayList<>();

        model.bean.TradeBean t3 = new model.bean.TradeBean();
        t3.setId("TRD-099");
        t3.setSenderUsername(username);
        t3.setReceiverUsername("Brock");
        t3.setStatus(model.domain.TradeStatus.ACCEPTED);
        t3.setScheduledDate(java.time.LocalDateTime.now().plusDays(2).withHour(15).withMinute(0));
        t3.setStoreLocation("PokeCenter Viridian City");
        model.bean.CardBean c3 = new model.bean.CardBean("base1-2", "Blastoise", null,
                model.domain.CardGameType.POKEMON);
        t3.setRequestedCard(c3);
        scheduledTrades.add(t3);

        if (view != null) {
            view.displayTrades(pendingTrades, scheduledTrades);
        }
    }

    public String getUsername() {
        return username;
    }

    public void navigateToHome() {
        LOGGER.log(java.util.logging.Level.INFO, "Navigating to home page for user: {0}", username);
        if (view != null) {
            view.close();
        }
        navigationController.navigateToCollectorHomePage(new UserBean(username, config.AppConfig.USER_TYPE_COLLECTOR));
    }

    public void navigateToCollection() {
        LOGGER.log(java.util.logging.Level.INFO, "Navigating to collection page for user: {0}", username);
        if (view != null) {
            view.close();
        }
        navigationController.navigateToCollection(username);
    }

    public void onLogoutRequested() {
        LOGGER.log(java.util.logging.Level.INFO, "User {0} logging out", username);
        if (view != null) {
            view.close();
        }
        navigationController.logout();
    }
}
