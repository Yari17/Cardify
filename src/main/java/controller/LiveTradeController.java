package controller;


import model.api.ApiFactory;
import model.bean.CardBean;
import model.bean.TradeTransactionBean;
import model.bean.UserBean;
import model.dao.ITradeDao;
import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.Card;
import model.domain.TradeTransaction;
import view.ICollectorTradeView;

import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

//controller Trade - kept minimal; presentation/management moved to ManageTradeController and ManageTradeView
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

    /**
     * Associa la view specifica per lo store al controller applicativo.
     */
    public void setStoreView(view.IStoreTradeView storeView) {
        this.storeView = storeView;
        if (this.storeView != null) {
            this.storeView.setController(this);
        }
    }

    /**
     * Verifica il session code inserito dallo store per una trade proposal specifica.
     * Se il codice è valido aggiorna lo stato della trade transaction e ritorna true,
     * altrimenti false. La responsabilità di persistere lo stato è del DAO.
     */
    public boolean verifySessionCode(int transactionId, int sessionCode) {
        try {
            ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
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
            TradeTransactionBean updatedBean = refreshTradeStatus(transactionId);
            if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.INSPECTION_PHASE) {
                if (storeView != null) {
                    storeView.showMessage("Both collectors arrived. Inspection phase started for trade " + transactionId);
                    storeView.displayTrade(updatedBean);
                }
                if (view != null) view.displayIspection();
            } else if (tx.getTradeStatus() == model.domain.enumerations.TradeStatus.PARTIALLY_ARRIVED && storeView != null) {
                storeView.showMessage("One collector arrived for trade " + transactionId + " — awaiting second arrival");
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "Post-verify notification failed: " + ex.getMessage());
        }
    }

    /**
     * Richiede al DAO l'ultima versione della trade transaction e la ritorna come bean.
     */
    public TradeTransactionBean refreshTradeStatus(int transactionId) {
        try {
            ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) return null;
            TradeTransactionBean b = new TradeTransactionBean();
            b.setTransactionId(tx.getTransactionId());
            b.setProposerId(tx.getProposerId());
            b.setReceiverId(tx.getReceiverId());
            b.setStoreId(tx.getStoreId());
            b.setTradeDate(tx.getTradeDate());
            b.setStatus(tx.getTradeStatus() != null ? tx.getTradeStatus().name() : null);
            // map cards
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
            // inspection status mapping
            b.setProposerInspectionOk(tx.getProposerInspectionOk());
            b.setReceiverInspectionOk(tx.getReceiverInspectionOk());
            // arrival flags and session codes
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
        // Intentionally empty: ManageTradeController is responsible for loading/managing proposals.
        LOGGER.fine("LiveTradeController.loadTrades() called - no-op by design (delegated to ManageTradeController)");
    }

    // Load scheduled trades for the current user and display them in the live trade view
    public void loadScheduledTrades() {
        try {
            ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            List<TradeTransaction> list;
            if (storeView != null) {
                list = tradeDao.getStoreTradeScheduledTransactions(username, null);
            } else {
                list = tradeDao.getUserTradeTransactions(username);
            }
            List<TradeTransactionBean> beans = new ArrayList<>();
            for (TradeTransaction t : list) {
                beans.add(mapToBean(t));
            }
            if (storeView != null) {
                LOGGER.info(() -> "loadScheduledTrades: dispatching " + beans.size() + " trades to store view");
                if (!beans.isEmpty()) {
                    TradeTransactionBean first = beans.get(0);
                    LOGGER.fine(() -> "First trade bean: id=" + first.getTransactionId() + " proposer=" + first.getProposerId() + " receiver=" + first.getReceiverId());
                }
                storeView.displayScheduledTrades(beans);
            } else if (view != null) {
                LOGGER.info(() -> "loadScheduledTrades: dispatching " + beans.size() + " trades to collector view");
                view.displayScheduledTrades(beans);
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "loadScheduledTrades failed: " + ex.getMessage());
        }
    }

    /**
     * Mappa una TradeTransaction in un TradeTransactionBean per la UI.
     */
    private TradeTransactionBean mapToBean(TradeTransaction t) {
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

    /**
     * Naviga indietro alla homepage dello store. Metodo invocato dalla view (Back button).
     */
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
            ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) {
                LOGGER.warning(() -> "confirmPresence: TradeTransaction not found for id " + transactionId);
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
            // collector view may update itself via refresh; we avoid calling controller->view circularly
        } catch (Exception ex) {
            LOGGER.fine(() -> "Post-confirm notification failed: " + ex.getMessage());
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
        } catch (exception.NavigationException _) {
            LOGGER.warning("navigateToManage failed");
        }
    }

    /**
     * Registra l'esito dell'ispezione svolta dallo store per un collector su una trade specifica.
     * Se l'ispezione portata dallo store è negativa per uno dei collector, la trade viene annullata.
     * Se entrambe sono positive, la trade viene marcata come COMPLETED.
     */
    public boolean recordInspectionResult(int transactionId, String collectorId, boolean ok) {
        try {
            ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
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

    /**
     * Effettua lo scambio delle carte tra i binders dei collezionisti.
     * Per ogni carta trasferita, controlla se il ricevente ha già un binder per il set della carta.
     * Se non esiste, lo crea e aggiunge la carta. Se esiste, aggiunge la carta al binder.
     * Il controllo viene fatto per entrambi i collezionisti.
     */
    public void performCardExchange(TradeTransaction tx) {
        IBinderDao binderDao = navigationController.getDaoFactory().createBinderDao();
        // Usa il provider polimorfico
        model.api.ICardProvider cardProvider = navigationController.getCardProvider();
        // Scambio carte offerte dal proposer al receiver
        for (Card card : tx.getOfferedCards()) {
            String receiver = tx.getReceiverId();
            String setId = card.getId().split("-")[0]; // Assumendo che l'id contenga il set
            List<Binder> receiverBinders = binderDao.getUserBinders(receiver);
            Binder binder = receiverBinders.stream()
                    .filter(b -> b.getSetId().equals(setId))
                    .findFirst()
                    .orElse(null);
            if (binder == null) {
                String setName = cardProvider.getAllSets().get(setId);
                binderDao.createBinder(receiver, setId, setName);
                receiverBinders = binderDao.getUserBinders(receiver);
                binder = receiverBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            }
            if (binder != null) {
                binder.addCard(card.toBean());
                binderDao.save(binder);
            }
        }
        // Scambio carte offerte dal receiver al proposer
        for (Card card : tx.getRequestedCards()) {
            String proposer = tx.getProposerId();
            String setId = card.getId().split("-")[0];
            List<Binder> proposerBinders = binderDao.getUserBinders(proposer);
            Binder binder = proposerBinders.stream()
                    .filter(b -> b.getSetId().equals(setId))
                    .findFirst()
                    .orElse(null);
            if (binder == null) {
                String setName = cardProvider.getAllSets().get(setId);
                binderDao.createBinder(proposer, setId, setName);
                proposerBinders = binderDao.getUserBinders(proposer);
                binder = proposerBinders.stream().filter(b -> b.getSetId().equals(setId)).findFirst().orElse(null);
            }
            if (binder != null) {
                binder.addCard(card.toBean());
                binderDao.save(binder);
            }
        }
    }

    /**
     * Conclude manualmente lo scambio: imposta lo stato a COMPLETED e trasferisce le carte tra i raccoglitori.
     * Può essere chiamato dalla view dello store per completare lo scambio dopo l'ispezione.
     */
    public boolean concludeTrade(int transactionId) {
        try {
            ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionById(transactionId);
            if (tx == null) {
                LOGGER.warning(() -> "concludeTrade: TradeTransaction non trovata per id " + transactionId);
                return false;
            }
            tx.updateTradeStatus(model.domain.enumerations.TradeStatus.COMPLETED);
            tradeDao.save(tx);
            performCardExchange(tx);
            TradeTransactionBean bean = refreshTradeStatus(transactionId);
            if (storeView != null) {
                storeView.displayTrade(bean);
                storeView.showMessage(TRADE_PREFIX + transactionId + " concluso con successo.");
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

    /**
     * Cerca una transazione di scambio tramite la coppia di session code dei collezionisti.
     * Restituisce il TradeTransactionBean se trovato, altrimenti null.
     */
    public TradeTransactionBean fetchTradeBySessionCodes(int proposerCode, int receiverCode) {
        try {
            ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            TradeTransaction tx = tradeDao.getTradeTransactionBySessionCodes(proposerCode, receiverCode);
            if (tx == null) {
                LOGGER.warning(() -> "fetchTradeBySessionCodes: nessuna transazione trovata per i codici " + proposerCode + ", " + receiverCode);
                return null;
            }
            return mapToBean(tx);
        } catch (Exception ex) {
            LOGGER.warning(() -> "fetchTradeBySessionCodes failed: " + ex.getMessage());
            return null;
        }
    }
}
