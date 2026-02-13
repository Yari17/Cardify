package controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.bean.UserBean;
import model.bean.mapper.UserMapper;
import model.domain.User;
import model.domain.enumerations.ViewPage;
import model.notification.Subject;
import model.notification.events.ProposalReceivedEvent;
import view.ITradeProposalView;
import view.IView;

/**
 * Controller responsabile del flusso di creazione di una proposta di scambio.
 * 
 * Gestisce l'interazione tra il collezionista proponente e la carta selezionata
 * (target).
 * Il controller mantiene uno stato temporaneo delle carte offerte e coordina la
 * finalizzazione
 * della proposta includendo i dettagli logistici (luogo e data dell'incontro).
 */
public class TradeProposalController extends Subject {
    /** Controller principale per la navigazione. */
    private ApplicationController applicationController;
    /** Vista associata alla creazione della proposta. */
    private ITradeProposalView view;
    /** Utente proponente in sessione. */
    private User sessionUser;
    /** DAO per il recupero dei dati degli utenti/negozi. */
    private model.dao.IUserDao userDao;
    /** DAO per il salvataggio della proposta finale. */
    private model.dao.IProposalDao proposalDao;
    /** DAO per il recupero della collezione dell'utente. */
    private model.dao.IBinderDao binderDao;

    /** Mappa dello stato della proposta: ID carta -> Quantità offerta. */
    private final java.util.Map<String, Integer> offeredCardsMap = new java.util.HashMap<>();

    /**
     * Mappa di riferimento per le quantità originali in collezione: ID carta ->
     * Quantità totale.
     */
    private final java.util.Map<String, Integer> originalCollectionQuantities = new java.util.HashMap<>();

    /**
     * Cache dei bean delle carte per riferimento rapido durante la creazione: ID
     * carta -> CardBean.
     */
    private final java.util.Map<String, model.bean.CardBean> proposalCandidates = new java.util.HashMap<>();

    /** La carta "target" che l'utente desidera ricevere in cambio. */
    private model.bean.CardBean targetCard;

    /**
     * Costruttore del controller per la creazione di proposte.
     * 
     * @param applicationController Riferimento al controller principale per la
     *                              navigazione.
     * @param sessionUser           Utente che sta creando la proposta.
     * @param userDao               DAO per la gestione dei negozi.
     * @param proposalDao           DAO per la persistenza della proposta finale.
     * @param binderDao             DAO per il recupero delle carte dell'utente.
     */
    public TradeProposalController(ApplicationController applicationController, User sessionUser,
            model.dao.IUserDao userDao, model.dao.IProposalDao proposalDao, model.dao.IBinderDao binderDao) {
        this.applicationController = applicationController;
        this.sessionUser = sessionUser;
        this.userDao = userDao;
        this.proposalDao = proposalDao;
        this.binderDao = binderDao;
    }

    /**
     * Inizializza lo stato della proposta impostando la carta desiderata.
     * 
     * Il metodo delega la pulizia delle mappe di stato al metodo helper
     * {@link #resetProposalState()}.
     * 
     * @param targetCard La carta che l'utente desidera ricevere.
     */
    public void initProposal(model.bean.CardBean targetCard) {
        this.targetCard = targetCard;
        resetProposalState();
        updateView();
    }

    /**
     * Resetta lo stato interno della proposta (carte offerte, quantità e cache dei
     * candidati).
     * 
     * Utile per iniziare una nuova sessione di proposta pulita.
     */
    public void resetProposalState() {
        offeredCardsMap.clear();
        originalCollectionQuantities.clear();
        proposalCandidates.clear();
    }

    /**
     * Registra la quantità originale disponibile per una carta nella collezione
     * dell'utente.
     * 
     * @param cardId   ID univoco della carta.
     * @param quantity Quantità totale posseduta.
     */
    public void setOriginalQuantity(String cardId, int quantity) {
        originalCollectionQuantities.put(cardId, quantity);
    }

    /**
     * Restituisce la quantità originale disponibile in collezione per una carta.
     * 
     * @param cardId ID della carta.
     * @return La quantità totale posseduta o 0 se non trovata.
     */
    public int getOriginalQuantity(String cardId) {
        return originalCollectionQuantities.getOrDefault(cardId, 0);
    }

    /**
     * Aggiorna o imposta la quantità di una carta offerta nello scambio.
     * 
     * @param cardId        ID della carta da offrire.
     * @param quantityToSet Quantità da includere nell'offerta.
     */
    public void addOfferedCard(String cardId, int quantityToSet) {
        offeredCardsMap.put(cardId, quantityToSet);
        updateView();
    }

    /**
     * Rimuove una carta dall'offerta corrente della proposta.
     * 
     * @param cardId ID della carta da rimuovere.
     */
    public void removeOfferedCard(String cardId) {
        offeredCardsMap.remove(cardId);
        updateView();
    }

    /**
     * Restituisce la quantità attualmente offerta per una specifica carta.
     * 
     * @param cardId ID della carta.
     * @return La quantità offerta o 0 se la carta non è in offerta.
     */
    public int getOfferedQuantity(String cardId) {
        return offeredCardsMap.getOrDefault(cardId, 0);
    }

    /**
     * Restituisce una copia della mappa delle carte correntemente offerte.
     * 
     * @return Nuova HashMap contenente le coppie ID-Quantità dell'offerta.
     */
    public java.util.Map<String, Integer> getOfferedCardsMap() {
        return new java.util.HashMap<>(offeredCardsMap);
    }

    /**
     * Memorizza temporaneamente i bean delle carte per facilitare la creazione
     * della proposta.
     * 
     * @param cards Lista di bean delle carte candidate all'offerta.
     */
    public void cacheProposalCandidates(List<model.bean.CardBean> cards) {
        proposalCandidates.clear();
        for (model.bean.CardBean c : cards) {
            proposalCandidates.put(c.getId(), c);
        }
    }

    /** Recupera un bean di una carta dalla cache tramite il suo ID. */
    public model.bean.CardBean getProposalCandidate(String cardId) {
        return proposalCandidates.get(cardId);
    }

    /**
     * Recupera l'intera collezione dell'utente corrente interrogando i binder
     * associati.
     * 
     * Delega la mappatura degli oggetti di dominio in bean al mapper
     * `CardMapper.toBean()`.
     * 
     * @return Una lista di {@link model.bean.CardBean} dell'utente.
     */
    public List<model.bean.CardBean> getUserCollection() {
        if (binderDao == null || sessionUser == null) {
            return new ArrayList<>();
        }

        List<model.domain.Binder> userBinders = binderDao.getUserBinders(sessionUser.getUsername());

        List<model.bean.CardBean> myCards = new ArrayList<>();

        for (model.domain.Binder binder : userBinders) {
            for (model.domain.CollectionItem item : binder.getOwnedCards()) {
                model.bean.CardBean cardBean = model.bean.mapper.CardMapper.toBean(item);
                cardBean.setOwner(binder.getOwner());

                // Calcolo remainingQuantity per disaccoppiare View dalla business logic
                int offeredQty = getOfferedQuantity(cardBean.getId());
                cardBean.setRemainingQuantity(cardBean.getQuantity() - offeredQty);

                myCards.add(cardBean);
            }
        }
        cacheProposalCandidates(myCards);
        return myCards;
    }

    /**
     * Recupera l'elenco dei negozi fisici registrati nel sistema.
     * 
     * @return Lista di utenti che rappresentano i negozi.
     */
    public List<User> getStores() {
        if (userDao != null) {
            return userDao.getStores();
        }
        return new ArrayList<>();
    }

    /**
     * Persiste la proposta definita nel database e attiva la notifica tramite il
     * pattern Observer.
     * 
     * Converte il bean in oggetto di dominio prima del salvataggio.
     * 
     * @param proposalBean Il bean contenente tutti i dati della proposta.
     */
    public void createProposal(model.bean.ProposalBean proposalBean) {
        if (proposalBean != null && proposalDao != null) {
            try {
                model.domain.Proposal proposal = model.bean.mapper.ProposalMapper.toDomain(proposalBean);
                proposalDao.save(proposal);

                // Notifica gli osservatori della nuova proposta
                notifyObservers(new ProposalReceivedEvent(
                        proposal.getReceiver().getUsername(),
                        proposal.getProposer().getUsername()));

            } catch (Exception e) {
                throw new exception.DaoException("Impossibile creare la proposta", e);
            }
        }
    }

    /**
     * Avvia la fase finale della proposta richiedendo all'utente i dettagli
     * dell'incontro.
     * 
     * Recupera i negozi tramite {@link #getStores()} e richiede alla vista di
     * mostrare
     * il dialogo per la selezione di luogo e data.
     */
    public void finalizeProposal() {
        if (view != null) {
            List<User> stores = getStores();
            List<UserBean> storeBeans = new ArrayList<>();
            for (User store : stores) {
                storeBeans.add(UserMapper.toBean(store));
            }
            view.showMeetingDialog(storeBeans, this::submitProposal);
        }
    }

    /**
     * Costruisce il bean finale della proposta e lo invia per il salvataggio.
     * 
     * Il metodo itera sulle carte offerte, delegando la clonazione di ogni bean a
     * {@link #copyCardBean(model.bean.CardBean)} per evitare di alterare gli
     * oggetti in cache,
     * e infine chiama {@link #createProposal(model.bean.ProposalBean)}.
     * 
     * @param store Il negozio selezionato per l'incontro.
     * @param time  La data e l'ora pianificate per l'incontro.
     */
    private void submitProposal(UserBean store, LocalDateTime time) {
        model.bean.ProposalBean bean = new model.bean.ProposalBean();
        bean.setProposalId(java.util.UUID.randomUUID().toString());
        bean.setFromUser(sessionUser.getUsername());

        if (targetCard != null) {
            bean.setToUser(targetCard.getOwner());
            List<model.bean.CardBean> requested = new ArrayList<>();
            targetCard.setQuantity(1); // Per ora assumiamo quantità 1
            requested.add(targetCard);
            bean.setRequested(requested);
        }

        List<model.bean.CardBean> offered = new ArrayList<>();
        java.util.Map<String, Integer> currentOffers = getOfferedCardsMap();
        for (java.util.Map.Entry<String, Integer> entry : currentOffers.entrySet()) {
            model.bean.CardBean original = getProposalCandidate(entry.getKey());
            if (original != null) {
                // Clona il bean originale prima di impostare la quantità offerta
                model.bean.CardBean copy = copyCardBean(original);
                copy.setQuantity(entry.getValue());
                offered.add(copy);
            }
        }
        bean.setOffered(offered);
        bean.setStatus("PENDING");

        if (store != null) {
            bean.setMeetingPlace(store.getUsername());
        }
        if (time != null) {
            // Usa formato italiano per compatibilità con ProposalMapper
            java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy");
            java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            bean.setMeetingDate(time.toLocalDate().format(dateFormatter));
            bean.setMeetingTime(time.toLocalTime().format(timeFormatter));
        }

        createProposal(bean);
        if (view != null) {
            view.showSuccessMessage("Proposta inviata con successo.");
        }
        goBack();
    }

    /**
     * Esegue una copia profonda di un bean di una carta.
     * 
     * @param original Il bean originale da duplicare.
     * @return Una nuova istanza di CardBean con gli stessi valori.
     */
    private model.bean.CardBean copyCardBean(model.bean.CardBean original) {
        if (original == null) {
            return null;
        }
        model.bean.CardBean copy = new model.bean.CardBean();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setImageUrl(original.getImageUrl());
        copy.setGameType(original.getGameType());
        copy.setQuantity(original.getQuantity());
        copy.setTradable(original.isTradable());
        copy.setStatus(original.getStatus());
        copy.setOwner(original.getOwner());
        copy.setSetId(original.getSetId());
        copy.setDetails(original.getDetails());
        return copy;
    }

    /** Imposta la vista associata a questo controller. */
    public void setView(IView view) {
        this.view = (ITradeProposalView) view;
        updateView();
    }

    /**
     * Aggiorna la vista con lo stato corrente della proposta (PUSH model).
     */
    private void updateView() {
        if (view != null) {
            view.showTargetItem(targetCard);
            view.showAvailableItems(getUserCollection());

            // Costruisce la lista di bean offerti per la vista
            List<model.bean.CardBean> offeredList = new ArrayList<>();
            for (java.util.Map.Entry<String, Integer> entry : offeredCardsMap.entrySet()) {
                model.bean.CardBean candidate = getProposalCandidate(entry.getKey());
                if (candidate != null) {
                    model.bean.CardBean copy = copyCardBean(candidate);
                    copy.setQuantity(entry.getValue());
                    offeredList.add(copy);
                }
            }
            view.showOfferedItems(offeredList);
        }
    }

    /**
     * Forza un aggiornamento della vista (utile per il refresh su richiesta della
     * UI).
     */
    public void refresh() {
        updateView();
    }

    /** Torna alla homepage del collezionista. */
    public void goToHomepage() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTOR_HOMEPAGE);
    }

    /** Metodo helper per tornare indietro, reindirizza alla homepage. */
    public void goBack() {
        goToHomepage();
    }

    /** Restituisce l'utente attualmente in sessione. */
    public User getSessionUser() {
        return sessionUser;
    }

    /** Restituisce la carta target oggetto della proposta. */
    public model.bean.CardBean getTargetCard() {
        return targetCard;
    }
}
