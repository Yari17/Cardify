package controller;


import model.bean.CardBean;
import model.bean.TradeTransactionBean;
import model.bean.UserBean;
import model.dao.IBinderDao;
import model.dao.ITradeDao;
import model.domain.Card;
import model.domain.TradeTransaction;
import view.ICollectorTradeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.logging.Logger;


public class LiveTradeController {
    private static final Logger LOGGER = Logger.getLogger(LiveTradeController.class.getName());
    private static final String TRADE_PREFIX = "Trade ";

    private final String username;
    private final ApplicationController navigationController;
    private view.ICollectorTradeView view;
    private view.IStoreTradeView storeView;

    public LiveTradeController(String username, ApplicationController navigationController) {
        this.username = username;
        this.navigationController = navigationController;
    }

    public void setView(ICollectorTradeView view) {
        this.view = view;
        if (view != null) {
            view.setUsername(username);
        }
    }

    
    public void setStoreView(view.IStoreTradeView storeView) {
        this.storeView = storeView;
        if (this.storeView != null) {
            this.storeView.setController(this);
        }
    }

    
    public boolean verifySessionCode(int transactionId, int sessionCode) {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) return false;
            boolean accepted = tx.acceptSessionCode(sessionCode);
            if (!accepted) return false;
            tradeDao.updateTransactionStatus(transactionId, tx.getTradeStatus() != null ? tx.getTradeStatus().name() : null);
            tradeDao.save(tx);
            notifyTradeStatusViews(tx, transactionId);
            return true;
        } catch (Exception ex) {
            LOGGER.warning(() -> "verifySessionCode failed: " + ex.getMessage());
            return false;
        }
    }

    private void notifyTradeStatusViews(TradeTransaction tx, int transactionId) {
        try {
            refreshTradeStatus(transactionId);
            if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.INSPECTION_PHASE) {
                if (storeView != null) {
                    storeView.showMessage("Both collectors arrived. Inspection phase started for trade " + transactionId);
                    
                    
                    
                }
                if (view != null) view.displayIspection();
            } else if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.PARTIALLY_ARRIVED && storeView != null) {
                storeView.showMessage("One collector arrived for trade " + transactionId + " — awaiting second arrival");
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "Post-verify notification failed: " + ex.getMessage());
        }
    }

    
    public TradeTransactionBean refreshTradeStatus(int transactionId) {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) return null;
            TradeTransactionBean b = new TradeTransactionBean();
            b.setTransactionId(tx.getTransactionId());
            b.setProposerId(tx.getProposerId());
            b.setReceiverId(tx.getReceiverId());
            b.setStoreId(tx.getStoreId());
            b.setTradeDate(tx.getTradeDate());
            b.setStatus(tx.getTradeStatus() != null ? tx.getTradeStatus().name() : null);
            
            java.util.List<CardBean> offered = new java.util.ArrayList<>();
            if (tx.getOfferedCards() != null) for (Card c : tx.getOfferedCards()) {
                CardBean cb = new model.bean.CardBean();
                cb.setId(c.getId());
                cb.setName(c.getName());
                cb.setImageUrl(c.getImageUrl());
                cb.setQuantity(c.getQuantity());
                offered.add(cb);
            }
            b.setOffered(offered);
            java.util.List<CardBean> requested = new java.util.ArrayList<>();
            if (tx.getRequestedCards() != null) for (model.domain.Card c : tx.getRequestedCards()) {
                CardBean cb = new CardBean();
                cb.setId(c.getId());
                cb.setName(c.getName());
                cb.setImageUrl(c.getImageUrl());
                cb.setQuantity(c.getQuantity());
                requested.add(cb);
            }
            b.setRequested(requested);
            
            b.setProposerInspectionOk(tx.getProposerInspectionOk());
            b.setReceiverInspectionOk(tx.getReceiverInspectionOk());
            
            b.setProposerArrived(tx.isProposerArrived());
            b.setReceiverArrived(tx.isReceiverArrived());
            b.setProposerSessionCode(tx.getProposerSessionCode());
            b.setReceiverSessionCode(tx.getReceiverSessionCode());
            return b;
        } catch (Exception ex) {
            LOGGER.warning(() -> "refreshTradeStatus failed: " + ex.getMessage());
            return null;
        }
    }

    public void loadTrades() {
        
        LOGGER.fine("LiveTradeController.loadTrades() called - no-op by design (delegated to ManageTradeController)");
    }

    
    public void loadScheduledTrades() {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            List<TradeTransaction> list;
            if (storeView != null) {
                list = tradeDao.getStoreTradeScheduledTransactions(username, null);
            } else {
                list = tradeDao.getUserTradeTransactions(username);
            }
            List<TradeTransactionBean> beans = new ArrayList<>();
            for (TradeTransaction t : list) {
                beans.add(toBean(t));
            }
            if (storeView != null) {
                LOGGER.info(() -> "loadScheduledTrades: dispatching " + beans.size() + " trades to store view");
                beans.stream().findFirst().ifPresent(first -> LOGGER.fine(() -> "First trade bean: id=" + first.getTransactionId() + " proposer=" + first.getProposerId() + " receiver=" + first.getReceiverId()));
                storeView.displayScheduledTrades(beans);
            } else if (view != null) {
                LOGGER.info(() -> "loadScheduledTrades: dispatching " + beans.size() + " trades to collector view");
                view.displayScheduledTrades(beans);
                
                safeLoadCollectorCompletedTrades();
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "loadScheduledTrades failed: " + ex.getMessage());
        }
    }

    
    private void safeLoadCollectorCompletedTrades() {
        try {
            LOGGER.info(() -> "loadScheduledTrades: now loading completed trades for user=" + username);
            loadCollectorCompletedTrades();
        } catch (Exception ex) {
            LOGGER.fine(() -> "loadScheduledTrades: failed to load completed trades: " + ex.getMessage());
        }
    }

    
    private TradeTransactionBean toBean(TradeTransaction t) {
        TradeTransactionBean b = new TradeTransactionBean();
        b.setTransactionId(t.getTransactionId());
        b.setProposerId(t.getProposerId());
        b.setReceiverId(t.getReceiverId());
        b.setStoreId(t.getStoreId());
        b.setTradeDate(t.getTradeDate());
        b.setStatus(t.getTradeStatus() != null ? t.getTradeStatus().name() : null);
        List<CardBean> offered = new ArrayList<>();
        if (t.getOfferedCards() != null) {
            for (Card c : toCardList(t.getOfferedCards())) {
                if (c == null) continue;
                CardBean cb = new CardBean();
                cb.setId(c.getId());
                cb.setName(c.getName());
                cb.setImageUrl(c.getImageUrl());
                cb.setQuantity(c.getQuantity());
                offered.add(cb);
            }
        }
        b.setOffered(offered);
        List<CardBean> requested = new ArrayList<>();
        if (t.getRequestedCards() != null) {
            for (Card c : toCardList(t.getRequestedCards())) {
                if (c == null) continue;
                CardBean cb = new CardBean();
                cb.setId(c.getId());
                cb.setName(c.getName());
                cb.setImageUrl(c.getImageUrl());
                cb.setQuantity(c.getQuantity());
                requested.add(cb);
            }
        }
        b.setRequested(requested);
        b.setProposerArrived(t.isProposerArrived());
        b.setReceiverArrived(t.isReceiverArrived());
        b.setProposerSessionCode(t.getProposerSessionCode());
        b.setReceiverSessionCode(t.getReceiverSessionCode());
        b.setProposerInspectionOk(t.getProposerInspectionOk());
        b.setReceiverInspectionOk(t.getReceiverInspectionOk());
        return b;
    }

    
    public void navigateBackToStoreHome() {
        try {
            navigationController.navigateToStoreHomePage(new UserBean(username, config.AppConfig.USER_TYPE_STORE));
        } catch (Exception ex) {
            LOGGER.warning(() -> "navigateBackToStoreHome failed: " + ex.getMessage());
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
        } catch (exception.NavigationException _) {
            LOGGER.warning("Logout failed");
        }
    }

    public int confirmPresence(int transactionId) {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) {
                LOGGER.warning(() -> "confirmPresence: TradeTransaction not found for id " + transactionId);
                return -1;
            }
            
            if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.BOTH_ARRIVED
                || tx.getTradeStatus() == model.domain.enumerations.TradeStatus.COMPLETED
                || tx.getTradeStatus() == model.domain.enumerations.TradeStatus.CANCELLED) {
                return -1;
            }
            int code = tx.confirmPresence(username);
            tradeDao.save(tx);

            notifyViewsAfterConfirm(tx, username);

            return code;
        } catch (Exception ex) {
            LOGGER.warning(() -> "confirmPresence failed: " + ex.getMessage());
            return -1;
        }
    }

    private void notifyViewsAfterConfirm(TradeTransaction tx, String username) {
        try {
            TradeTransactionBean bean = refreshTradeStatus(tx.getTransactionId());
            if (storeView != null) {
                if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.BOTH_ARRIVED) {
                    storeView.showMessage("Siete entrambi in negozio, aspettate che il personale ispezioni le vostre carte");
                } else {
                    storeView.showMessage("Collector arrived: " + username + " for trade " + tx.getTransactionId());
                }
                storeView.displayTrade(bean);
            }
            
        } catch (Exception ex) {
            LOGGER.fine(() -> "Post-confirm notification failed: " + ex.getMessage());
        }
    }

    
    public void startTrade(String proposalId) {
        try {
            
            LOGGER.fine(() -> "startTrade requested for proposalId=" + proposalId + " user=" + username);
            navigationController.navigateToTrade(username);
        } catch (Exception ex) {
            LOGGER.warning(() -> "startTrade navigation failed: " + ex.getMessage());
        }
    }

    
    public void navigateToManage() {
        try {
            navigationController.navigateToManageTrade(username);
        } catch (exception.NavigationException _) {
            LOGGER.warning("navigateToManage failed");
        }
    }

    
    public boolean recordInspectionResult(int transactionId, String collectorId, boolean ok) {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) return false;
            tx.markInspectionResult(collectorId, ok);
            tradeDao.save(tx);
            TradeTransactionBean bean = refreshTradeStatus(transactionId);
            if (storeView != null) {
                storeView.displayTrade(bean);
                if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.COMPLETED) {
                    storeView.showMessage(TRADE_PREFIX + transactionId + " completed successfully.");
                } else if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.CANCELLED) {
                    storeView.showMessage(TRADE_PREFIX + transactionId + " cancelled during inspection.");
                }
            }
            if (view != null) {
                if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.COMPLETED) {
                    view.onTradeComplete(String.valueOf(transactionId));
                } else if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.CANCELLED) {
                    view.showError(TRADE_PREFIX + transactionId + " cancelled during inspection");
                }
            }
            if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.COMPLETED) {
                performCardExchange(tx);
            }
            return true;
        } catch (Exception ex) {
            LOGGER.warning(() -> "recordInspectionResult failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean failInspection(int transactionId, String collectorId) {
        return recordInspectionResult(transactionId, collectorId, false);
    }

    
    public boolean markInspectionPassed(int transactionId) {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) {
                LOGGER.warning(() -> "markInspectionPassed: TradeTransaction not found for id " + transactionId);
                return false;
            }
            tx.updateTradeStatus(model.domain.enumerations.TradeStatus.INSPECTION_PASSED);
            tradeDao.save(tx);
            
            TradeTransactionBean bean = refreshTradeStatus(transactionId);
            if (storeView != null) {
                storeView.displayTrade(bean);
                storeView.showMessage(TRADE_PREFIX + transactionId + " inspection passed.");
            }
            if (view != null) {
                
                view.onIspectionComplete(null);
            }
            return true;
        } catch (Exception ex) {
            LOGGER.warning(() -> "markInspectionPassed failed: " + ex.getMessage());
            return false;
        }
    }

    
    private List<Card> toCardList(Object obj) {
        if (obj instanceof List<?> raw) {
            List<Card> result = new ArrayList<>();
            for (Object o : raw) {
                if (o instanceof Card card) result.add(card);
            }
            return result;
        }
        return new ArrayList<>();
    }

    
    public void performCardExchange(TradeTransaction tx) {
        try {
            IBinderDao binderDao = navigationController.getBinderDao();
            model.api.ICardProvider cardProvider = navigationController.getCardProvider();
            CardExchangeManager manager = new CardExchangeManager(binderDao, cardProvider);
            manager.executeExchange(tx);
        } catch (Exception ex) {
            LOGGER.fine(() -> "performCardExchange failed delegated execution: " + ex.getMessage());
        }
    }

    
    public boolean concludeTrade(int transactionId) {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) {
                LOGGER.warning(() -> "concludeTrade: TradeTransaction non trovata per id " + transactionId);
                return false;
            }
            tx.updateTradeStatus(model.domain.enumerations.TradeStatus.COMPLETED);
            tradeDao.save(tx);
            performCardExchange(tx);
            
            if (storeView != null) {
                storeView.showMessage(TRADE_PREFIX + transactionId + " concluso con successo.");
                
                navigationController.navigateToStoreTrades(tx.getStoreId());
            }
            if (view != null) {
                view.onTradeComplete(String.valueOf(transactionId));
            }
            return true;
        } catch (Exception ex) {
            LOGGER.warning(() -> "concludeTrade failed: " + ex.getMessage());
            return false;
        }
    }

    
    public TradeTransactionBean fetchTradeBySessionCodes(int proposerCode, int receiverCode) {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionBySessionCodes(proposerCode, receiverCode);
            if (tx == null) {
                LOGGER.warning(() -> "fetchTradeBySessionCodes: nessuna transazione trovata per i codici " + proposerCode + ", " + receiverCode);
                return null;
            }
            return toBean(tx);
        } catch (Exception ex) {
            LOGGER.warning(() -> "fetchTradeBySessionCodes failed: " + ex.getMessage());
            return null;
        }
    }

    
    public void loadStoreScheduledTrades() {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            List<TradeTransaction> list = tradeDao.getStoreTradeScheduledTransactions(username, null);
            List<TradeTransactionBean> beans = new ArrayList<>();
            for (TradeTransaction t : list) {
                beans.add(toBean(t));
            }
            if (storeView != null) {
                storeView.displayScheduledTrades(beans);
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "loadStoreScheduledTrades failed: " + ex.getMessage());
        }
    }

    
    public void loadStoreInProgressTrades() {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            List<TradeTransaction> list = tradeDao.getStoreTradeInProgressTransactions(username);
            List<TradeTransactionBean> beans = new ArrayList<>();
            for (TradeTransaction t : list) {
                beans.add(toBean(t));
            }
            if (storeView != null) {
                storeView.displayInProgressTrades(beans); 
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "loadStoreInProgressTrades failed: " + ex.getMessage());
        }
    }

    
    public void loadCollectorCompletedTrades() {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            List<TradeTransaction> all = tradeDao.getUserCompletedTrades(username);
            
            logCompletedTradesDiagnostic(all);
            List<TradeTransactionBean> completed = new ArrayList<>();
            if (all != null) {
                for (TradeTransaction t : all) {
                    completed.add(toBean(t));
                }
            }
            if (view != null) {
                view.displayCompletedTrades(completed);
            }
         } catch (Exception ex) {
             LOGGER.warning(() -> "loadCollectorCompletedTrades failed: " + ex.getMessage());
         }
     }

    
    private void logCompletedTradesDiagnostic(List<TradeTransaction> all) {
        try {
            if (all == null || all.isEmpty()) {
                LOGGER.info(() -> "LiveTradeController.loadCollectorCompletedTrades: DAO returned 0 completed trades for user=" + username);
            } else {
                String ids = all.stream().filter(Objects::nonNull).map(t -> String.valueOf(t.getTransactionId())).collect(Collectors.joining(","));
                LOGGER.info(() -> "LiveTradeController.loadCollectorCompletedTrades: DAO returned " + all.size() + " completed trades for user=" + username + " ids=" + ids);
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "LiveTradeController.loadCollectorCompletedTrades logging failed: " + ex.getMessage());
        }
    }

    
    public void loadStoreCompletedTrades() {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            List<TradeTransaction> all = tradeDao.getStoreCompletedTrades(username);
            if (all == null) all = new ArrayList<>();
            List<TradeTransactionBean> completed = new ArrayList<>();
            for (TradeTransaction t : all) completed.add(toBean(t));
            if (storeView != null) storeView.displayCompletedTrades(completed);
        } catch (Exception ex) {
            LOGGER.fine(() -> "loadStoreCompletedTrades failed: " + ex.getMessage());
        }
    }

    
    public boolean cancelTrade(int transactionId) {
        try {
            ITradeDao tradeDao = navigationController.getTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) {
                LOGGER.warning(() -> "cancelTrade: TradeTransaction not found for id " + transactionId);
                return false;
            }
            tx.updateTradeStatus(model.domain.enumerations.TradeStatus.CANCELLED);
            tradeDao.save(tx);
            
            TradeTransactionBean bean = refreshTradeStatus(transactionId);
            if (storeView != null) {
                storeView.displayTrade(bean);
                storeView.showMessage(TRADE_PREFIX + transactionId + " cancelled during inspection.");
            }
            if (view != null) {
                view.showError(TRADE_PREFIX + transactionId + " cancelled during inspection");
            }
            return true;
        } catch (Exception ex) {
            LOGGER.warning(() -> "cancelTrade failed: " + ex.getMessage());
            return false;
        }
    }
 }
