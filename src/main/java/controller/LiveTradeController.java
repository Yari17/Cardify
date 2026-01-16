package controller;


import model.bean.UserBean;
import model.domain.Card;
import view.trade.ILiveTradeView;

import java.util.List;
import java.util.logging.Logger;

//controller Trade - kept minimal; presentation/management moved to ManageTradeController and ManageTradeView
public class LiveTradeController {
    private static final Logger LOGGER = Logger.getLogger(LiveTradeController.class.getName());

    private final String username;
    private final ApplicationController navigationController;
    private ILiveTradeView view;

    public LiveTradeController(String username, ApplicationController navigationController) {
        this.username = username;
        this.navigationController = navigationController;
    }

    public void setView(ILiveTradeView view) {
        this.view = view;
        if (view != null) {
            view.setUsername(username);
        }
    }

    public void loadTrades() {
        // Intentionally empty: ManageTradeController is responsible for loading/managing proposals.
        LOGGER.fine("LiveTradeController.loadTrades() called - no-op by design (delegated to ManageTradeController)");
    }

    // Load scheduled trades for the current user and display them in the live trade view
    public void loadScheduledTrades() {
        try {
            model.dao.ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            java.util.List<model.domain.TradeTransaction> list = tradeDao.getUserTradeTransactions(username);
            java.util.List<model.bean.TradeTransactionBean> beans = new java.util.ArrayList<>();
            for (model.domain.TradeTransaction t : list) {
                model.bean.TradeTransactionBean b = new model.bean.TradeTransactionBean();
                b.setTransactionId(t.getTransactionId());
                // TradeTransaction domain currently doesn't hold a proposalId field; leave it null or map if available
                b.setProposerId(t.getProposerId());
                b.setReceiverId(t.getReceiverId());
                b.setStoreId(t.getStoreId());
                b.setTradeDate(t.getTradeDate());
                b.setStatus(t.getTradeStatus() != null ? t.getTradeStatus().name() : null);

                java.util.List<model.bean.CardBean> offered = new java.util.ArrayList<>();
                if (t.getOfferedCards() != null) {
                    for (model.domain.Card c : t.getOfferedCards()) {
                        if (c == null) continue;
                        model.bean.CardBean cb = new model.bean.CardBean();
                        cb.setId(c.getId()); cb.setName(c.getName()); cb.setImageUrl(c.getImageUrl()); cb.setQuantity(c.getQuantity());
                        offered.add(cb);
                    }
                }
                b.setOffered(offered);

                java.util.List<model.bean.CardBean> requested = new java.util.ArrayList<>();
                if (t.getRequestedCards() != null) {
                    for (model.domain.Card c : t.getRequestedCards()) {
                        if (c == null) continue;
                        model.bean.CardBean cb = new model.bean.CardBean();
                        cb.setId(c.getId()); cb.setName(c.getName()); cb.setImageUrl(c.getImageUrl()); cb.setQuantity(c.getQuantity());
                        requested.add(cb);
                    }
                }
                b.setRequested(requested);
                beans.add(b);
            }
            if (view != null) view.displayScheduledTrades(beans);
        } catch (Exception ex) {
            LOGGER.fine(() -> "loadScheduledTrades failed: " + ex.getMessage());
        }
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
        try {
            navigationController.logout();
        } catch (exception.NavigationException ne) {
            LOGGER.warning(() -> "Logout failed: " + ne.getMessage());
        }
    }

    public int confirmPresence(String proposalId) {
        try {
            var proposalOpt = navigationController.getDaoFactory().createProposalDao().getById(proposalId);
            if (proposalOpt.isEmpty()) {
                LOGGER.warning(() -> "Proposal not found for confirmPresence: " + proposalId);
                return -1;
            }
            model.domain.Proposal p = proposalOpt.get();

            // build domain TradeTransaction
            int txId = (int)(System.currentTimeMillis() % Integer.MAX_VALUE);
            java.time.LocalDateTime creation = java.time.LocalDateTime.now();
            java.time.LocalDateTime tradeDate;
            if (p.getMeetingDate() != null && !p.getMeetingDate().isEmpty()) {
                tradeDate = java.time.LocalDate.parse(p.getMeetingDate()).atStartOfDay();
            } else {
                tradeDate = creation;
            }

            List<Card> offered = new java.util.ArrayList<>();
            if (p.getCardsOffered() != null) {
                for (model.domain.Card c : p.getCardsOffered()) {
                    model.domain.Card dc = new model.domain.Card(c.getId(), c.getName(), c.getImageUrl(), c.getGameType());
                    dc.setQuantity(c.getQuantity());
                    offered.add(dc);
                }
            }
            List<model.domain.Card> requested = new java.util.ArrayList<>();
            if (p.getCardsRequested() != null) {
                for (model.domain.Card c : p.getCardsRequested()) {
                    model.domain.Card dc = new model.domain.Card(c.getId(), c.getName(), c.getImageUrl(), c.getGameType());
                    dc.setQuantity(c.getQuantity());
                    requested.add(dc);
                }
            }

            model.domain.TradeTransaction tx = new model.domain.TradeTransaction(txId, model.domain.enumerations.TradeStatus.WAITING_FOR_ARRIVAL,
                    p.getProposerId(), p.getReceiverId(), p.getMeetingPlace(), creation, tradeDate, offered, requested);

            model.dao.ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            tradeDao.save(tx);

            return tx.confirmPresence(username);
        } catch (Exception ex) {
            LOGGER.warning(() -> "confirmPresence failed: " + ex.getMessage());
            return -1;
        }
    }

    // Navigate to the live trade view for the given proposal (delegates to ApplicationController)
    public void startTrade(String proposalId) {
        try {
            navigationController.navigateToTrade(username, proposalId);
        } catch (Exception ex) {
            LOGGER.warning(() -> "startTrade navigation failed: " + ex.getMessage());
        }
    }

    // Navigate to the manage trades view
    public void navigateToManage() {
        try {
            navigationController.navigateToManageTrade(username);
        } catch (exception.NavigationException ne) {
            LOGGER.warning(() -> "navigateToManage failed: " + ne.getMessage());
        }
    }

}
