package controller;

import model.bean.CardBean;
import model.bean.ProposalBean;
import model.dao.IUserDao;
import view.INegotiationView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NegotiationController {
    private static final Logger LOGGER = Logger.getLogger(NegotiationController.class.getName());

    private final String proposerUsername;
    private final String targetOwnerUsername;
    private INegotiationView view;
    private final controller.ApplicationController navigationController;
    private final model.dao.IProposalDao proposalDao;

    public NegotiationController(String proposerUsername, String targetOwnerUsername, ApplicationController navigationController) {
        this.proposerUsername = proposerUsername;
        this.targetOwnerUsername = targetOwnerUsername;
        this.navigationController = navigationController;
        this.proposalDao = navigationController != null ? navigationController.getProposalDao() : null;
    }

    public void setView(INegotiationView view) {
        this.view = view;
    }

    public void start(List<CardBean> inventory, List<CardBean> requested) {
        if (view != null) {
            view.showInventory(inventory);
            view.showRequested(requested);
            view.showProposed(java.util.Collections.emptyList());

            
            view.registerOnCardProposed(this::onCardProposed);
            view.registerOnCardUnproposed(this::onCardUnproposed);
            view.registerOnConfirmRequested(this::onConfirmRequested);

            
            try {
                IUserDao userDao = navigationController.getUserDao();
                List<String> stores = new ArrayList<>();
                for (String username : userDao.findAllUsernames()) {
                    model.domain.User u = userDao.findByName(username).orElse(null);
                    if (u != null && config.AppConfig.USER_TYPE_STORE.equals(u.getUserType())) {
                        stores.add(u.getName());
                    }
                }
                view.showAvailableStores(stores);
                
                view.setMeetingDateHint(LocalDate.now().plusDays(1).toString());
            } catch (Exception ex) {
                
                LOGGER.fine(() -> "Failed to load stores for negotiation view: " + ex.getMessage());
            }
        }
    }

    public String getProposerUsername() { return proposerUsername; }
    public String getTargetOwnerUsername() { return targetOwnerUsername; }

    private void onCardProposed(CardBean card) {
        LOGGER.info(() -> "Card proposed: " + card.getId());
    }

    private void onCardUnproposed(CardBean card) {
        LOGGER.info(() -> "Card unproposed: " + card.getId());
    }

    private void onConfirmRequested(ProposalBean proposalBean) {
        LOGGER.info(() -> "Confirm requested: " + proposalBean);
        try {
            
            if (proposalBean == null || proposalBean.getOffered() == null || proposalBean.getOffered().isEmpty()) {
                if (view != null) view.showConfirmationResult(false, "Devi offrire almeno una carta prima di inviare la proposta.");
                return;
            }
            int totalOffered = 0;
            for (model.bean.CardBean cb : proposalBean.getOffered()) totalOffered += cb.getQuantity();
            if (totalOffered <= 0) {
                if (view != null) view.showConfirmationResult(false, "La quantità delle carte offerte deve essere almeno 1.");
                return;
            }
             if (proposalDao != null) {
                 model.domain.Proposal p = mapToDomainProposal(proposalBean);
                 proposalDao.save(p);
                 if (view != null) view.showConfirmationResult(true, "Proposta inviata");
                 
                 safeNavigateToCollectorHome();
                 return;
             }
         } catch (Exception ex) {
             LOGGER.warning("Failed to persist proposal: " + ex.getMessage());
         }
         if (view != null) view.showConfirmationResult(false, "Impossibile salvare la proposta");
     }

    
    private void safeNavigateToCollectorHome() {
        try {
            navigationController.navigateToCollectorHomePage(new model.bean.UserBean(proposerUsername, config.AppConfig.USER_TYPE_COLLECTOR));
        } catch (exception.NavigationException _) {
            LOGGER.fine(() -> "Failed to navigate to collector home after sending proposal: ");
        }
    }

    private model.domain.Proposal mapToDomainProposal(ProposalBean bean) {
        model.domain.Proposal p = new model.domain.Proposal();
        
        p.setProposerId(bean.getFromUser() != null ? bean.getFromUser() : proposerUsername);
        p.setReceiverId(bean.getToUser() != null ? bean.getToUser() : targetOwnerUsername);

        if (bean.getOffered() != null) {
            List<model.domain.Card> offered = new ArrayList<>();
            for (model.bean.CardBean cb : bean.getOffered()) {
                model.domain.Card domainCard = new model.domain.Card(cb.getId(), cb.getName(), cb.getImageUrl(), cb.getGameType());
                domainCard.setQuantity(cb.getQuantity());
                offered.add(domainCard);
            }
            p.setCardsOffered(offered);
        }
        if (bean.getRequested() != null) {
            List<model.domain.Card> requested = new ArrayList<>();
            for (model.bean.CardBean cb : bean.getRequested()) {
                model.domain.Card domainCard = new model.domain.Card(cb.getId(), cb.getName(), cb.getImageUrl(), cb.getGameType());
                domainCard.setQuantity(cb.getQuantity());
                requested.add(domainCard);
            }
            p.setCardsRequested(requested);
        }
        
        p.setMeetingPlace(bean.getMeetingPlace());
        p.setMeetingDate(bean.getMeetingDate());
        p.setMeetingTime(bean.getMeetingTime());
        p.setStatus(model.domain.enumerations.ProposalStatus.PENDING);
        p.setLastUpdated(java.time.LocalDateTime.now());
        return p;
    }

    public void setController(INegotiationView view) {
        setView(view);
    }
}
