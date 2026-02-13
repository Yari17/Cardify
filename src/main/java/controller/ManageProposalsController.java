package controller;

import model.domain.Proposal;
import model.domain.User;
import model.domain.enumerations.ViewPage;
import model.notification.Subject;
import model.notification.events.ProposalAcceptedEvent;
import model.notification.events.ProposalRejectedEvent;
import view.IManageProposalsView;
import view.IView;
import model.domain.enumerations.ProposalStatus;
import model.dao.IProposalDao;
import model.dao.ITradeSessionDao;
import model.bean.ProposalBean;
import model.bean.mapper.ProposalMapper;

import java.util.List;

/**
 * Controller dedicato alla gestione delle proposte di scambio tra
 * collezionisti.
 * 
 * Gestisce il ciclo di vita di una proposta (accettazione, rifiuto,
 * visualizzazione)
 * e coordina la creazione delle sessioni di scambio effettive. Estende
 * {@link Subject}
 * per notificare gli osservatori (come il servizio di notifica) riguardo ai
 * cambiamenti di stato.
 */
public class ManageProposalsController extends Subject {
    /** Prefisso per i messaggi di log relativi al caricamento delle proposte. */
    private static final String LOG_LOADED_PREFIX = "Caricate ";

    /** Controller di applicazione per la gestione della navigazione. */
    ApplicationController applicationController;
    /** Vista associata per la gestione delle proposte. */
    IManageProposalsView view;
    /** Utente attualmente autenticato. */
    User sessionUser;
    /** DAO per l'accesso e la persistenza delle proposte. */
    IProposalDao proposalDao;
    /** DAO per la persistenza delle sessioni di scambio generate. */
    ITradeSessionDao tradeSessionDao;

    /**
     * Costruttore principale per il controller delle proposte.
     * 
     * @param applicationController Il controller principale dell'applicazione per
     *                              la navigazione.
     * @param sessionUser           L'utente attualmente in sessione.
     * @param proposalDao           Il DAO per l'accesso ai dati delle proposte.
     * @param tradeSessionDao       Il DAO per la persistenza delle sessioni di
     *                              scambio.
     */
    public ManageProposalsController(ApplicationController applicationController, User sessionUser,
            IProposalDao proposalDao, ITradeSessionDao tradeSessionDao) {
        this.applicationController = applicationController;
        this.sessionUser = sessionUser;
        this.proposalDao = proposalDao;
        this.tradeSessionDao = tradeSessionDao;
    }

    /**
     * Accetta una proposta di scambio specifica.
     * 
     * Il metodo aggiorna lo stato della proposta, delega la creazione della
     * sessione di scambio
     * effettiva al metodo helper {@link #createTradeSession(Proposal)} per
     * mantenere la logica separata,
     * e infine notifica gli osservatori dell'avvenuta accettazione.
     * 
     * @param proposalId L'ID della proposta da accettare.
     */
    public void acceptProposal(String proposalId) {
        if (proposalDao == null || proposalId == null)
            return;

        java.util.Optional<Proposal> pOpt = proposalDao.getById(proposalId);
        if (pOpt.isPresent()) {
            Proposal proposal = pOpt.get();
            if (proposal.getStatus() == ProposalStatus.PENDING) {
                proposal.accept();
                proposalDao.update(proposal);

                // Crea e salva la sessione di scambio delegando la logica complessa
                createTradeSession(proposal);

                // Notifica tramite Observer pattern
                notifyObservers(new ProposalAcceptedEvent(proposal.getProposer(),
                        proposal.getReceiver()));
                // Ricarica i dati e aggiorna la view (PUSH)
                loadProposals();
                view.showProposalAcceptedDialog(this::goToTrade);
            }
        }
    }

    /**
     * Rifiuta una proposta di scambio specifica.
     * 
     * Aggiorna lo stato della proposta sul database e notifica gli osservatori
     * affinché il proponente riceva l'avviso del rifiuto.
     * 
     * @param proposalId L'ID della proposta da rifiutare.
     */
    public void rejectProposal(String proposalId) {
        if (proposalDao == null || proposalId == null)
            return;

        java.util.Optional<Proposal> pOpt = proposalDao.getById(proposalId);
        if (pOpt.isPresent()) {
            Proposal proposal = pOpt.get();
            if (proposal.getStatus() == ProposalStatus.PENDING) {
                proposal.reject();
                proposalDao.update(proposal);
                // Notifica tramite Observer pattern
                notifyObservers(new ProposalRejectedEvent(proposal.getProposer(), proposal.getReceiver()));
                // Ricarica i dati e aggiorna la view (PUSH)
                loadProposals();
            }
        }
    }

    /**
     * Recupera l'elenco delle proposte ricevute dall'utente corrente che sono
     * ancora in attesa.
     * 
     * @return Una lista di {@link ProposalBean} rappresentanti le proposte
     *         pendenti.
     */
    public List<ProposalBean> getReceivedPendingProposals() {
        if (proposalDao == null || sessionUser == null)
            return List.of();
        List<ProposalBean> list = proposalDao.getReceivedPendingProposals(sessionUser).stream()
                .map(this::toBean)
                .toList();
        java.util.logging.Logger.getLogger(getClass().getName())
                .info(() -> LOG_LOADED_PREFIX + list.size() + " proposte ricevute in attesa.");
        return list;
    }

    /**
     * Recupera l'elenco delle proposte inviate dall'utente corrente che sono ancora
     * in attesa.
     * 
     * @return Una lista di {@link ProposalBean} rappresentanti le proposte inviate.
     */
    public List<ProposalBean> getSentProposals() {
        if (proposalDao == null || sessionUser == null)
            return List.of();
        List<ProposalBean> list = proposalDao.getSentPendingProposal(sessionUser).stream()
                .map(this::toBean)
                .toList();
        java.util.logging.Logger.getLogger(getClass().getName())
                .info(() -> LOG_LOADED_PREFIX + list.size() + " proposte inviate.");
        return list;
    }

    /**
     * Recupera l'elenco del passato storico delle proposte completate (accettate o
     * rifiutate).
     * 
     * @return Una lista di {@link ProposalBean} delle proposte storiche.
     */
    public List<ProposalBean> getCompletedProposals() {
        if (proposalDao == null || sessionUser == null)
            return List.of();
        List<ProposalBean> list = proposalDao.getCompletedProposals(sessionUser).stream()
                .map(this::toBean)
                .toList();
        java.util.logging.Logger.getLogger(getClass().getName())
                .info(() -> LOG_LOADED_PREFIX + list.size() + " proposte completate.");
        return list;
    }

    /**
     * Converte un oggetto di dominio Proposal in un Bean per la visualizzazione.
     * Popola il flag isSentByMe per evitare logica comparativa nelle View.
     * 
     * @param proposal L'oggetto di dominio da mappare.
     * @return Il bean corrispondente con flag isSentByMe impostato.
     */
    private ProposalBean toBean(Proposal proposal) {
        ProposalBean bean = ProposalMapper.toBean(proposal);
        // Popolamento flag per eliminare business logic nelle View
        if (sessionUser != null) {
            bean.setIsSentByMe(proposal.getProposer().getUsername().equals(sessionUser.getUsername()));
        }
        return bean;
    }

    /**
     * Collega la vista al controller.
     * 
     * @param manageProposalsView La vista che implementa l'interfaccia
     *                            IManageProposalsView.
     */
    public void setView(IView manageProposalsView) {
        this.view = (IManageProposalsView) manageProposalsView;
        loadProposals();
    }

    /**
     * Recupera tutte le categorie di proposte e aggiorna la vista (PUSH model).
     */
    public void loadProposals() {
        if (view != null) {
            view.showReceivedPendingProposals(getReceivedPendingProposals());
            view.showSentPendingProposals(getSentProposals());
            view.showCompletedProposals(getCompletedProposals());
        }
    }

    /**
     * Restituisce l'istanza dell'ApplicationController associata.
     */
    public ApplicationController getApplicationController() {
        return this.applicationController;
    }

    /**
     * Restituisce l'utente corrente in sessione.
     */
    public User getSessionUser() {
        return this.sessionUser;
    }

    /**
     * Naviga verso la homepage del collezionista.
     */
    public void goToHomepage() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTOR_HOMEPAGE);
    }

    /**
     * Naviga verso la visualizzazione della collezione dell'utente.
     */
    public void goToCollection() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTION);
    }

    /**
     * Naviga verso la sezione degli scambi del collezionista.
     */
    public void goToTrade() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTOR_TRADE);
    }

    /**
     * Ricarica la pagina di gestione delle proposte.
     */
    public void manageProposals() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.MANAGE_PROPOSAL);
    }

    /**
     * Esegue il logout dell'utente corrente e riporta alla schermata di login.
     */
    public void logout() {
        if (applicationController != null) {
            applicationController.setCurrentUser(null);
            applicationController.navigateTO(ViewPage.LOGIN);
        }
    }

    /**
     * Crea una nuova sessione di scambio (TradeSession) a partire da una proposta
     * accettata.
     * 
     * Questo metodo helper si occupa di appiattire le liste di carte proposte e
     * richieste,
     * gestire i partecipanti e configurare i dettagli logistici dell'incontro prima
     * della persistenza.
     * 
     * @param proposal La proposta accettata da cui generare la sessione.
     */
    private void createTradeSession(Proposal proposal) {
        if (tradeSessionDao == null)
            return;

        model.domain.TradeSession.TradeParticipants participants = new model.domain.TradeSession.TradeParticipants(
                proposal.getProposer().getUsername(),
                proposal.getReceiver().getUsername(),
                proposal.getMeetingStore() != null ? proposal.getMeetingStore().getUsername() : null);

        List<model.domain.Card> offeredCards = proposal.getProposedItems().stream()
                .map(item -> {
                    List<model.domain.Card> flattened = new java.util.ArrayList<>();
                    for (int i = 0; i < item.getQuantity(); i++)
                        flattened.add(item.getCard());
                    return flattened;
                })
                .flatMap(List::stream)
                .toList();

        List<model.domain.Card> askedCards = new java.util.ArrayList<>();
        if (proposal.getAskedItems() != null) {
            askedCards = proposal.getAskedItems().stream()
                    .map(item -> {
                        List<model.domain.Card> flattened = new java.util.ArrayList<>();
                        for (int i = 0; i < item.getQuantity(); i++)
                            flattened.add(item.getCard());
                        return flattened;
                    })
                    .flatMap(List::stream)
                    .toList();
        }

        model.domain.TradeSession.TradeDetails details = new model.domain.TradeSession.TradeDetails(
                java.time.LocalDateTime.now(),
                proposal.getScheduledAt(),
                offeredCards,
                askedCards);

        model.domain.TradeSession session = new model.domain.TradeSession(
                -1, // L'ID verrà generato automaticamente dal database
                model.domain.enumerations.TradeStatus.WAITING_FOR_ARRIVAL, // Stato iniziale
                participants,
                details);

        tradeSessionDao.saveTradeSession(session);

        // Notifica il negozio della nuova sessione programmata
        if (session.getStoreId() != null) {
            notifyObservers(new model.notification.events.TradeSessionCreatedEvent(session));
        }
    }
}
