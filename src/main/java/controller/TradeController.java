package controller;


import model.bean.UserBean;
import view.trade.ITradeView;

import java.util.logging.Logger;

//controller Trade
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
