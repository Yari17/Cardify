package controller;

import model.bean.CardBean;
import model.bean.ProposalBean;
import model.domain.Card;
import model.domain.Proposal;
import model.domain.TradeTransaction;
import model.domain.enumerations.ProposalStatus;
import model.domain.enumerations.TradeStatus;
import view.managetrade.IManageTradeView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ManageTradeController {
    private static final Logger LOGGER = Logger.getLogger(ManageTradeController.class.getName());

    private final String username;
    private final model.dao.IProposalDao proposalDao;
    private final ApplicationController navigationController;
    private IManageTradeView view;

    public ManageTradeController(String username, ApplicationController navigationController) {
        this.username = username;
        // Only keep the dao reference we need for proposal operations
        this.proposalDao = navigationController != null ? navigationController.getDaoFactory().createProposalDao() : null;
        this.navigationController = navigationController;
    }

    /**
     * Load proposals for the current user, classify them and dispatch to the view for rendering.
     */
    public void loadAndDisplayTrades(IManageTradeView view) {
        if (view == null) return;

        List<ProposalBean> pending = new ArrayList<>();
        List<ProposalBean> concluded = new ArrayList<>();

        try {
            // Fetch proposals where user is involved
            List<Proposal> sentPending = proposalDao != null ? proposalDao.getSentPendingProposal(username) : List.of();
            List<Proposal> received = proposalDao != null ? proposalDao.getReceivedProposals(username) : List.of();

            LocalDateTime now = LocalDateTime.now();

            // Combine sent and received proposals
            List<model.domain.Proposal> combined = new ArrayList<>();
            combined.addAll(sentPending);
            combined.addAll(received);

            // Process combined proposals: separate pending vs concluded (rejected/expired)
            for (Proposal p : combined) {
                if (p == null) continue;
                processProposal(p, pending, concluded, now);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading user trade transactions: {0}", e.getMessage());
            LOGGER.log(Level.FINE, "Stacktrace", e);
        }

        // Wire controller and display pending + concluded
        view.setManageController(this);
        view.displayTrades(pending, concluded);
    }

    // Accept a proposal (mark as ACCEPTED and persist)
    public boolean acceptProposal(String proposalId) {
        return updateProposalStatus(proposalId, ProposalStatus.ACCEPTED);
    }

    // Decline a proposal (mark as REJECTED and persist)
    public boolean declineProposal(String proposalId) {
        return updateProposalStatus(proposalId, ProposalStatus.REJECTED);
    }

    // Helper to set a proposal status and persist (used by accept/decline)
    private boolean updateProposalStatus(String proposalId, ProposalStatus newStatus) {
        if (proposalDao == null || proposalId == null || newStatus == null) return false;
        try {
            var opt = proposalDao.getById(proposalId);
            if (opt.isEmpty()) return false;
            Proposal p = opt.get();
            p.setStatus(newStatus);
            proposalDao.update(p);

            // If the proposal became ACCEPTED, create and persist a TradeTransaction
            if (newStatus == ProposalStatus.ACCEPTED) {
                persistTradeTransactionIfNeeded(p, proposalId);
            }

            LOGGER.log(Level.INFO, "Proposal {0}: {1}", new Object[]{newStatus.name().toLowerCase(), proposalId});
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to update proposal {0} to {1}: {2}", new Object[]{proposalId, newStatus, e.getMessage()});
            return false;
        }
    }

    // Map a domain.Proposal to a domain.TradeTransaction
    private model.domain.TradeTransaction mapProposalToTradeTransaction(Proposal p) {
        if (p == null) return null;
        int txId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        java.time.LocalDateTime creation = java.time.LocalDateTime.now();
        java.time.LocalDateTime tradeDate = creation;
        try {
            if (p.getMeetingDate() != null && !p.getMeetingDate().isEmpty()) {
                tradeDate = java.time.LocalDate.parse(p.getMeetingDate()).atStartOfDay();
            }
        } catch (Exception e) {
            LOGGER.fine(() -> "Could not parse meeting date for proposal " + p.getProposalId() + ": " + e.getMessage());
        }

        List<Card> offered = new ArrayList<>();
        if (p.getCardsOffered() != null) offered.addAll(p.getCardsOffered());
        List<Card> requested = new ArrayList<>();
        if (p.getCardsRequested() != null) requested.addAll(p.getCardsRequested());

        return new TradeTransaction(txId,
                TradeStatus.WAITING_FOR_ARRIVAL,
                p.getProposerId(), p.getReceiverId(), p.getMeetingPlace(), creation, tradeDate, offered, requested);
    }

    // Return proposal domain object for detailed view
    public model.domain.Proposal getProposalById(String proposalId) {
        if (proposalDao == null || proposalId == null) return null;
        return proposalDao.getById(proposalId).orElse(null);
    }

    // Initiate the live trade (called when parties are meeting at store on meeting day)
    public boolean initiateTrade(String proposalId) {
        if (navigationController == null || proposalId == null) return false;
        try {
            // Delegate to ApplicationController's navigation to trade so LiveTradeController handles the use-case
            navigationController.navigateToTrade(username, proposalId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to start trade flow for {0}: {1}", new Object[]{proposalId, e.getMessage()});
            return false;
        }
    }


    private boolean isExpired(Proposal p, LocalDateTime now) {
        if (p == null || p.getLastUpdated() == null) return false;
        return p.getLastUpdated().plusDays(1).isBefore(now);
    }

    private void persistProposalStatusChange(Proposal p) {
        if (p == null) return;
        if (proposalDao == null) return;
        try {
            proposalDao.update(p);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to persist proposal status change for {0}: {1}", new Object[]{p.getProposalId(), e.getMessage()});
        }
    }

    private ProposalBean toBean(Proposal p) {
        ProposalBean b = new ProposalBean();
        b.setProposalId(p.getProposalId());
        b.setFromUser(p.getProposerId());
        b.setToUser(p.getReceiverId());
        b.setMeetingPlace(p.getMeetingPlace());
        b.setMeetingDate(p.getMeetingDate());
        b.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        b.setLastUpdated(p.getLastUpdated());
        b.setOffered(mapCardsToBeans(p.getCardsOffered()));
        b.setRequested(mapCardsToBeans(p.getCardsRequested()));
        return b;
    }

    // Convert a list of domain.Card to a list of CardBean safely
    private List<CardBean> mapCardsToBeans(List<Card> cards) {
        List<CardBean> result = new ArrayList<>();
        if (cards == null || cards.isEmpty()) return result;
        for (Card c : cards) {
            if (c == null) continue;
            result.add(cardToBean(c));
        }
        return result;
    }

    // Convert single domain.Card to CardBean
    private CardBean cardToBean(Card c) {
        CardBean cb = new CardBean();
        cb.setId(c.getId());
        cb.setName(c.getName());
        cb.setImageUrl(c.getImageUrl());
        cb.setGameType(c.getGameType() != null ? c.getGameType().name() : null);
        cb.setQuantity(c.getQuantity());
        return cb;
    }

    public void setView(IManageTradeView view) {
        this.view = view;
        if (this.view != null) {
            this.view.setManageController(this);
            this.view.setUsername(this.username);
        }
    }

    public void navigateToHome() {
        LOGGER.info(() -> "Navigating to home page for user: " + username);
        if (view != null) {
            view.close();
        }
        navigationController
                .navigateToCollectorHomePage(new model.bean.UserBean(username, config.AppConfig.USER_TYPE_COLLECTOR));
    }

    public void navigateToCollection() {
        LOGGER.log(java.util.logging.Level.INFO, "Navigating to collection page for user: {0}", username);
        if (view != null) {
            view.close();
        }
        navigationController.navigateToCollection(username);
    }
    public void onLogoutRequested() {
        LOGGER.info(() -> "User " + username + " logging out");
        if (view != null) {
            view.close();
        }
        navigationController.logout();
    }

    public void navigateToLiveTrades() {
        LOGGER.log(java.util.logging.Level.INFO, "Navigating to Live Trades for user: {0}", username);
        if (view != null) view.close();
        try {
            navigationController.navigateToLiveTrades(username);
        } catch (exception.NavigationException e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to navigate to Live Trades: {0}", e.getMessage());
            if (view != null) view.showError("Impossibile aprire la sezione Trade");
        }
    }

    // Helper to process a single proposal into pending or concluded lists; keeps loadUserCollection concise
    private void processProposal(Proposal p, List<ProposalBean> pending, List<ProposalBean> concluded, LocalDateTime now) {
        try {
            boolean expired = isExpired(p, now);
            if (expired) {
                p.setStatus(ProposalStatus.EXPIRED);
                persistProposalStatusChange(p);
            }

            if (p.getStatus() == ProposalStatus.PENDING) {
                pending.add(toBean(p));
            } else if (p.getStatus() == ProposalStatus.REJECTED || p.getStatus() == ProposalStatus.EXPIRED) {
                concluded.add(toBean(p));
            }
            // ACCEPTED proposals are handled as scheduled trades and shown elsewhere
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed processing proposal {0}: {1}", new Object[]{p.getProposalId(), ex.getMessage()});
        }
    }

    // Extracted persistence logic for trade transactions when a proposal is accepted
    private void persistTradeTransactionIfNeeded(Proposal p, String proposalId) {
        try {
            model.dao.ITradeDao tradeDao = navigationController != null ? navigationController.getDaoFactory().createTradeDao() : null;
            if (tradeDao != null) {
                model.domain.TradeTransaction tx = mapProposalToTradeTransaction(p);
                if (tx == null) {
                    LOGGER.fine(() -> "mapProposalToTradeTransaction returned null for proposal " + proposalId);
                    return;
                }
                tradeDao.save(tx);
                LOGGER.log(Level.INFO, "Created trade transaction {0} for proposal {1}", new Object[]{tx.getTransactionId(), proposalId});
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to persist TradeTransaction for proposal {0}: {1}", new Object[]{proposalId, ex.getMessage()});
        }
    }
}
