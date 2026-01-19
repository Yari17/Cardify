package controller;

import model.bean.CardBean;
import model.bean.ProposalBean;
import model.dao.ITradeDao;
import model.domain.Card;
import model.domain.Proposal;
import model.domain.TradeTransaction;
import model.domain.enumerations.ProposalStatus;
import view.cli.CliManageTradeView;
import view.javafx.FXManageTradeView;
import view.IManageTradeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        
        this.proposalDao = navigationController != null ? navigationController.getDaoFactory().createProposalDao()
                : null;
        this.navigationController = navigationController;
    }


    public void loadAndDisplayTrades(IManageTradeView view) {
        if (view == null)
            return;

        List<ProposalBean> pending = new ArrayList<>();
        List<ProposalBean> concluded = new ArrayList<>();

        try {
            
            List<Proposal> pendingProposals = proposalDao != null ? proposalDao.getPendingProposals(username) : List.of();
            List<Proposal> completedProposals = proposalDao != null ? proposalDao.getCompletedProposals(username) : List.of();

            
            for (Proposal p : pendingProposals) {
                if (p == null) continue;
                pending.add(toBean(p));
            }
            for (Proposal p : completedProposals) {
                if (p == null) continue;
                concluded.add(toBean(p));
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error loading user trade transactions: {0}", ex.getMessage());
            LOGGER.log(Level.FINE, "Stacktrace", ex);
        }

        
        view.displayTrades(pending, concluded);
    }

    
    public boolean acceptProposal(String proposalId) {
        return updateProposalStatus(proposalId, ProposalStatus.ACCEPTED);
    }

    
    public boolean declineProposal(String proposalId) {
        return updateProposalStatus(proposalId, ProposalStatus.REJECTED);
    }

    
    private boolean updateProposalStatus(String proposalId, ProposalStatus newStatus) {
        if (proposalDao == null || proposalId == null || newStatus == null)
            return false;
        try {
            var opt = proposalDao.getById(proposalId);
            if (opt.isEmpty())
                return false;
            Proposal p = opt.get();
            
            switch (newStatus) {
                case ACCEPTED -> p.accept();
                case REJECTED -> p.decline();
                default -> p.setStatus(newStatus);
            }
            proposalDao.update(p);

            
            if (newStatus == ProposalStatus.ACCEPTED) {
                persistTradeTransactionIfNeeded(p, proposalId);
            }

            LOGGER.log(Level.INFO, "Proposal {0}: {1}", new Object[] { newStatus.name().toLowerCase(), proposalId });
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to update proposal {0} to {1}: {2}",
                    new Object[] { proposalId, newStatus, ex.getMessage() });
            return false;
        }
    }

    
    public model.domain.Proposal getProposalById(String proposalId) {
        if (proposalDao == null || proposalId == null)
            return null;
        return proposalDao.getById(proposalId).orElse(null);
    }

    
    
    public boolean initiateTrade(String proposalId) {
        if (navigationController == null || proposalId == null)
            return false;
        try {
            
            Optional<Proposal> opt = proposalDao != null ? proposalDao.getById(proposalId) : Optional.empty();
            if (opt.isEmpty()) return false;
            Proposal p = opt.get();
            java.time.LocalDateTime meeting = p.getMeetingLocalDateTime().orElse(null);
            model.dao.ITradeDao tradeDao = navigationController.getDaoFactory().createTradeDao();
            var txOpt = tradeDao.findByParticipantsAndDate(p.getProposerId(), p.getReceiverId(), meeting);
            if (txOpt.isPresent()) {
                
                navigationController.navigateToTrade(username);
            } else {
                LOGGER.warning(() -> "No TradeTransaction found for proposal: " + proposalId);
                return false;
            }
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to start trade flow for {0}: {1}",
                    new Object[] { proposalId, ex.getMessage() });
            return false;
        }
    }


    private ProposalBean toBean(Proposal p) {
        ProposalBean b = new ProposalBean();
        b.setProposalId(p.getProposalId());
        b.setFromUser(p.getProposerId());
        b.setToUser(p.getReceiverId());
        b.setMeetingPlace(p.getMeetingPlace());
        b.setMeetingDate(p.getMeetingDate());
        b.setMeetingTime(p.getMeetingTime());
        b.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        
        b.setOffered(mapCardsToBeans(p.getCardsOffered()));
        b.setRequested(mapCardsToBeans(p.getCardsRequested()));
        return b;
    }

    
    private List<CardBean> mapCardsToBeans(List<Card> cards) {
        List<CardBean> result = new ArrayList<>();
        if (cards == null || cards.isEmpty())
            return result;
        for (Card c : cards) {
            if (c == null)
                continue;
            result.add(cardToBean(c));
        }
        return result;
    }

    
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
            
            try {
                this.view.registerOnAccept(this::acceptProposal);
                this.view.registerOnDecline(this::declineProposal);
                this.view.registerOnCancel(this::declineProposal);
                this.view.registerOnTradeClick(this::initiateTrade);
                this.view.registerOnTradeNowClick(this::initiateTrade);
            } catch (AbstractMethodError | Exception ex) {
                
                
                LOGGER.fine(() -> "Callback registration via interface failed, falling back to setManageController: " + ex.getMessage());
                try {
                    if (this.view instanceof FXManageTradeView fx)
                        fx.setManageController(this);
                    else if (this.view instanceof CliManageTradeView cli)
                        cli.setManageController(this);
                } catch (Exception innerEx) {
                    LOGGER.fine(() -> "View does not support setManageController: " + innerEx.getMessage());
                }
            }
            try {
                this.view.setUsername(this.username);
            } catch (Exception ex) {
                LOGGER.fine(() -> "View does not support setUsername: " + ex.getMessage());
            }
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
        if (view != null)
            view.close();
        try {
            navigationController.navigateToLiveTrades(username);
        } catch (exception.NavigationException ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to navigate to Live Trades: {0}", ex.getMessage());
            if (view != null)
                view.showError("Impossibile aprire la sezione Trade");
        }
    }

    
    
    private void persistTradeTransactionIfNeeded(Proposal p, String proposalId) {
        try {
            ITradeDao tradeDao = navigationController != null
                    ? navigationController.getDaoFactory().createTradeDao()
                    : null;
            if (tradeDao != null) {
               TradeTransaction tx = p.toTradeTransaction();
                if (tx == null) {
                    LOGGER.fine(() -> "toTradeTransaction returned null for proposal " + proposalId);
                    return;
                }
                tradeDao.save(tx);
                LOGGER.log(Level.INFO, "Created trade transaction {0} for proposal {1}",
                        new Object[] { tx.getTransactionId(), proposalId });
             }
         } catch (Exception ex) {
             LOGGER.log(Level.WARNING, "Failed to persist TradeTransaction for proposal {0}: {1}",
                     new Object[] { proposalId, ex.getMessage() });
         }
     }

}
