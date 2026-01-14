package controller;

import model.bean.TradeBean;
import view.trade.ITradeView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ManageTradeController {
    private static final Logger LOGGER = Logger.getLogger(ManageTradeController.class.getName());

    private final String username;
    private final model.dao.IProposalDao proposalDao;

    public ManageTradeController(String username, ApplicationController navigationController) {
        this.username = username;
        // Only keep the dao reference we need for proposal operations
        this.proposalDao = navigationController != null ? navigationController.getDaoFactory().createProposalDao() : null;
    }

    /**
     * Load trades for the current user, classify them and dispatch to the view for rendering.
     */
    public void loadAndDisplayTrades(ITradeView view) {
        if (view == null) return;

        List<TradeBean> pending = new ArrayList<>();
        List<TradeBean> scheduled = new ArrayList<>();

        try {
            // Fetch proposals where user is involved
            List<model.domain.Proposal> sentPending = proposalDao != null ? proposalDao.getSentPendingProposal(username) : List.of();
            List<model.domain.Proposal> received = proposalDao != null ? proposalDao.getReceivedProposals(username) : List.of();
            List<model.domain.Proposal> scheduledProps = proposalDao != null ? proposalDao.getScheduledProposals(username) : List.of();

            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            // Combine sent and received pending proposals
            List<model.domain.Proposal> combinedPending = new ArrayList<>();
            combinedPending.addAll(sentPending);
            combinedPending.addAll(received);

            processPendingProposals(combinedPending, pending, now);

            // Scheduled proposals -> scheduled list
            for (model.domain.Proposal p : scheduledProps) {
                if (p == null) continue;
                scheduled.add(mapProposalToTradeBean(p));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading user trade transactions: {0}", e.getMessage());
            LOGGER.log(Level.FINE, "Stacktrace", e);
        }

        view.displayTrades(pending, scheduled);
    }

    private void processPendingProposals(List<model.domain.Proposal> proposals, List<TradeBean> outPending, java.time.LocalDateTime now) {
        if (proposals == null || proposals.isEmpty()) return;
        for (model.domain.Proposal p : proposals) {
            if (p == null) continue;
            try {
                boolean expired = isExpired(p, now);
                if (expired) {
                    p.setStatus(model.domain.enumerations.ProposalStatus.EXPIRED);
                    persistProposalStatusChange(p);
                }
                if (p.getStatus() == model.domain.enumerations.ProposalStatus.PENDING
                        || p.getStatus() == model.domain.enumerations.ProposalStatus.EXPIRED) {
                    outPending.add(mapProposalToTradeBean(p));
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed processing proposal {0}: {1}", new Object[]{p != null ? p.getProposalId() : "<null>", ex.getMessage()});
            }
        }
    }

    private boolean isExpired(model.domain.Proposal p, java.time.LocalDateTime now) {
        if (p == null || p.getLastUpdated() == null) return false;
        return p.getLastUpdated().plusDays(1).isBefore(now);
    }

    private void persistProposalStatusChange(model.domain.Proposal p) {
        if (p == null) return;
        if (proposalDao == null) return;
        try {
            proposalDao.update(p);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to persist proposal status change for {0}: {1}", new Object[]{p.getProposalId(), e.getMessage()});
        }
    }

    private TradeBean mapProposalToTradeBean(model.domain.Proposal p) {
        TradeBean tb = new TradeBean();
        tb.setTradeId(p.getProposalId());
        tb.setStoreId(p.getMeetingPlace());
        tb.setStatus(null); // TradeBean has different status semantics; leave null or reuse
        tb.setCreatedAt(p.getLastUpdated() != null ? p.getLastUpdated().atZone(java.time.ZoneId.systemDefault()).toInstant() : java.time.Instant.now());
        tb.setUpdatedAt(java.time.Instant.now());

        // proposer participant
        TradeBean.ParticipantBean proposer = new TradeBean.ParticipantBean();
        proposer.setUserId(p.getProposerId());
        proposer.setRole("proposer");
        if (p.getCardsOffered() != null) {
            List<TradeBean.ItemBean> items = new ArrayList<>();
            for (model.domain.Card c : p.getCardsOffered()) {
                TradeBean.ItemBean ib = new TradeBean.ItemBean();
                ib.setId(c.getId());
                ib.setName(c.getName());
                ib.setGameType(c.getGameType() != null ? c.getGameType().name() : null);
                ib.setQuantity(c.getQuantity());
                ib.setTradable(true);
                items.add(ib);
            }
            proposer.setItems(items);
        }

        // receiver participant
        TradeBean.ParticipantBean receiver = new TradeBean.ParticipantBean();
        receiver.setUserId(p.getReceiverId());
        receiver.setRole("receiver");
        if (p.getCardsRequested() != null) {
            List<TradeBean.ItemBean> items = new ArrayList<>();
            for (model.domain.Card c : p.getCardsRequested()) {
                TradeBean.ItemBean ib = new TradeBean.ItemBean();
                ib.setId(c.getId());
                ib.setName(c.getName());
                ib.setGameType(c.getGameType() != null ? c.getGameType().name() : null);
                ib.setQuantity(c.getQuantity());
                ib.setTradable(true);
                items.add(ib);
            }
            receiver.setItems(items);
        }

        List<TradeBean.ParticipantBean> parts = new ArrayList<>();
        parts.add(proposer);
        parts.add(receiver);
        tb.setParticipants(parts);

        return tb;
    }
}
