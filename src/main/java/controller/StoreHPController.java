package controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import view.IStoreHPView;

public class StoreHPController {
    private static final Logger LOGGER = Logger.getLogger(StoreHPController.class.getName());

    private final String username;
    private final ApplicationController navigationController;
    private view.IStoreHPView view;

    public StoreHPController(String username, ApplicationController navigationController) {
        this.username = username;
        this.navigationController = navigationController;
    }

    public String getUsername() {
        return username;
    }

    
    public void setView(IStoreHPView view) {
        this.view = view;
        
        if (view != null) {
            view.setController(this);
            view.showWelcomeMessage(username);
        }
    }

    
    public void onLogoutRequested() {
        LOGGER.log(Level.INFO, "Store user {0} logging out", username);
        navigationController.logout();
    }

    
    public void onExitRequested() {
        System.exit(0);
    }

    
    public void onManageTradesRequested() {
        LOGGER.log(Level.INFO, "StoreHP: richiesta di gestione scambi per {0}", username);
        
        navigationController.navigateToStoreTrades(username);
    }

    
    public void onViewCompletedTradesRequested() {
        LOGGER.log(Level.INFO, "StoreHP: richiesta di visualizzazione scambi conclusi per {0}", username);
        
        loadCompletedTrades();
    }

    
    public void loadCompletedTrades() {
        try {
            model.dao.ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            java.util.List<model.domain.TradeTransaction> list = tradeDao.getStoreCompletedTrades(username);
            java.util.List<model.bean.TradeTransactionBean> beans = new java.util.ArrayList<>();
            if (list != null) {
                for (model.domain.TradeTransaction t : list) {
                    if (t == null) continue;
                    model.bean.TradeTransactionBean b = new model.bean.TradeTransactionBean();
                    b.setTransactionId(t.getTransactionId());
                    b.setProposerId(t.getProposerId());
                    b.setReceiverId(t.getReceiverId());
                    b.setStoreId(t.getStoreId());
                    b.setTradeDate(t.getTradeDate());
                    b.setStatus(t.getTradeStatus() != null ? t.getTradeStatus().name() : null);
                    beans.add(b);
                }
            }
            
            
            
            safeDisplayCompletedTrades(beans);
        } catch (Exception ex) {
            LOGGER.warning(() -> "loadCompletedTrades failed: " + ex.getMessage());
        }
    }

    private void safeDisplayCompletedTrades(java.util.List<model.bean.TradeTransactionBean> beans) {
        try {
            if (view != null) view.displayCompletedTrades(beans);
        } catch (Exception ex) {
            LOGGER.warning(() -> "Displaying completed trades failed: " + ex.getMessage());
        }
    }
}
