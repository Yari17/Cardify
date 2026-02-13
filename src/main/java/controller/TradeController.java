package controller;

import model.domain.User;
import model.domain.enumerations.ViewPage;
import model.notification.Subject;
import model.notification.events.UserInStoreEvent;
import view.ICollectorTradeView;
import view.IStoreTradeView;
import view.IView;
import model.domain.enumerations.TradeStatus;
import model.domain.TradeSession;

import model.dao.ITradeSessionDao;
import model.dao.IBinderDao;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import model.domain.Card;
import model.domain.Binder;
import model.bean.TradeSessionBean;
import model.bean.mapper.TradeSessionMapper;

/**
 * Controller centrale per la gestione delle sessioni di scambio fisico (Trade
 * Sessions).
 * Coordina l'intero processo di scambio nel mondo reale presso un negozio
 * fisico:
 * dall'arrivo dei partecipanti (validazione codici sessione), alla fase di
 * ispezione
 * delle carte, fino al trasferimento effettivo della proprietà nel sistema.
 * Estende {@link Subject} per notificare l'arrivo dell'utente in negozio
 * tramite {@link UserInStoreEvent}.
 */
public class TradeController extends Subject {
    ApplicationController applicationController;

    private ICollectorTradeView collectorView;
    private IStoreTradeView storeView;

    private final User sessionUser;

    private final ITradeSessionDao tradeSessionDao;
    private final IBinderDao binderDao;

    /**
     * Costruttore del controller per gli scambi fisici.
     * 
     * @param applicationController Il controller principale per la gestione della
     *                              navigazione.
     * @param sessionUser           L'utente (collezionista o negozio) attualmente
     *                              loggato.
     * @param tradeSessionDao       DAO per l'accesso e l'aggiornamento delle
     *                              sessioni di scambio.
     * @param binderDao             DAO per la gestione dei binder e il
     *                              trasferimento delle carte tra utenti.
     */
    public TradeController(ApplicationController applicationController, User sessionUser,
            ITradeSessionDao tradeSessionDao, IBinderDao binderDao) {
        this.applicationController = applicationController;
        this.sessionUser = sessionUser;
        this.tradeSessionDao = tradeSessionDao;
        this.binderDao = binderDao;
    }

    /**
     * Carica le sessioni di scambio dell'utente corrente e aggiorna la vista del
     * collezionista.
     * Il metodo separa le sessioni attive da quelle completate o annullate, utilizza un mapper
     * per convertire le entità di dominio in Bean e le passa alla View
     * {@link #isTradeActive(TradeStatus)}
     * e {@link #isTradeCompleted(TradeStatus)}.
     */
    public void loadTrades() {
        List<TradeSession> sessions = tradeSessionDao.getUserTradeSessions(sessionUser);
        List<TradeSessionBean> active = new ArrayList<>();
        List<TradeSessionBean> completed = new ArrayList<>();

        for (TradeSession session : sessions) {
            if (isTradeActive(session.getTradeStatus())) {
                active.add(TradeSessionMapper.toBean(session));
            } else if (isTradeCompleted(session.getTradeStatus())) {
                completed.add(TradeSessionMapper.toBean(session));
            }
        }

        collectorView.showTradeLists(active, completed);
    }

    /**
     * Restituisce l'utente attualmente loggato nella sessione.
     * 
     * @return L'oggetto {@link User} che rappresenta l'utente corrente.
     */
    public User getSessionUser() {
        return this.sessionUser;
    }

    /**
     * Apre i dettagli di una specifica sessione di scambio.
     * Carica l'entità dal database tramite l'ID e delega la logica di
     * visualizzazione
     * differenziata (codice segreto, partner) a
     * {@link #loadAndShowTradeDetails(TradeSession)}.
     * 
     * @param tradeId L'identificatore univoco della sessione di scambio.
     */
    public void openTradeDetails(int tradeId) {
        if ((collectorView == null && storeView == null) || tradeSessionDao == null)
            return;

        TradeSession currentTradeSession = tradeSessionDao.getTradeSessionById(tradeId);

        if (currentTradeSession != null) {
            loadAndShowTradeDetails(currentTradeSession);
        }
    }

    /**
     * Configura i parametri di visualizzazione e mostra i dettagli dello scambio
     * nella vista attiva.
     * Se l'utente corrente è un collezionista partecipante, il metodo mostra il suo
     * codice di sessione temporaneo.
     * 
     * @param session L'entità di dominio TradeSession da mostrare.
     */
    private void loadAndShowTradeDetails(TradeSession session) {
        String userCode = null;
        String partnerName = null;

        if (sessionUser != null) {
            if (sessionUser.getUsername().equals(session.getProposerId())) {
                if (session.getProposerSessionCode() > 0) {
                    userCode = String.valueOf(session.getProposerSessionCode());
                }
                partnerName = session.getReceiverId();
            } else if (sessionUser.getUsername().equals(session.getReceiverId())) {
                if (session.getReceiverSessionCode() > 0) {
                    userCode = String.valueOf(session.getReceiverSessionCode());
                }
                partnerName = session.getProposerId();
            }
        }

        // Converti a bean per separazione view/domain
        TradeSessionBean sessionBean = TradeSessionMapper.toBean(session);

        if (collectorView != null) {
            collectorView.showTradeDetails(sessionBean, userCode, partnerName);
        } else if (storeView != null) {
            storeView.showTradeDetails(sessionBean, userCode, partnerName);
        }
    }

    /**
     * Genera e recupera il codice di partecipazione per l'utente loggato.
     * Chiama {@link TradeSession#confirmPresence(String)} per generare un codice
     * OTP (One-Time Password)
     * e persiste lo stato tramite il DAO senza però modificare lo stato globale
     * della sessione.
     * 
     * @param session La sessione di scambio in cui l'utente desidera confermare la
     *                presenza.
     * @return Il codice generato (>0) o -1 in caso di errore.
     */
    public int retrieveSessionCode(TradeSession session) {
        if (session == null || tradeSessionDao == null || sessionUser == null)
            return -1;

        int newCode = session.confirmPresence(sessionUser.getUsername());
        if (newCode > 0) {
            tradeSessionDao.updateTradeSessionStatus(session);
            if (collectorView != null) {
                collectorView.registerConfirmPresence();
                collectorView.showSessionCode();
            }
        }
        return newCode;
    }

    /**
     * Genera e recupera il codice di partecipazione per l'utente loggato tramite ID
     * sessione.
     * Versione semplificata per view che non hanno accesso a oggetti domain.
     * Carica la sessione dal DAO e delega a
     * {@link #retrieveSessionCode(TradeSession)}.
     * 
     * @param sessionId ID della sessione di scambio
     * @return Il codice generato (>0) o -1 in caso di errore
     */
    public int retrieveSessionCodeById(int sessionId) {
        if (tradeSessionDao == null)
            return -1;

        TradeSession session = tradeSessionDao.getTradeSessionById(sessionId);
        return retrieveSessionCode(session);
    }

    /**
     * Recupera il codice sessione dell'utente loggato per la sessione specificata.
     * Determina automaticamente se l'utente è proposer o receiver e restituisce
     * il codice corrispondente.
     * 
     * @param session Sessione da cui recuperare il codice
     * @return Codice sessione utente (>0) o -1 se non generato o errore
     */
    public int getUserSessionCode(TradeSession session) {
        if (session == null || sessionUser == null)
            return -1;

        String username = sessionUser.getUsername();

        if (username.equals(session.getProposerId())) {
            return session.getProposerSessionCode();
        } else if (username.equals(session.getReceiverId())) {
            return session.getReceiverSessionCode();
        }

        return -1;
    }

    /**
     * Recupera il codice sessione dell'utente loggato tramite ID sessione.
     * Versione semplificata per view che non hanno accesso a oggetti domain.
     * Carica la sessione dal DAO e delega a
     * {@link #getUserSessionCode(TradeSession)}.
     * 
     * @param sessionId ID della sessione
     * @return Codice sessione utente (>0) o -1 se non generato o errore
     */
    public int getUserSessionCode(int sessionId) {
        if (tradeSessionDao == null)
            return -1;

        TradeSession session = tradeSessionDao.getTradeSessionById(sessionId);
        return getUserSessionCode(session);
    }

    /**
     * Esegue il completamento effettivo dello scambio nel sistema.
     * La procedura segue questi passi rigorosi:
     * 1. Delega il trasferimento fisico (logico) delle carte tra utenti a
     * {@link #processCardTransfer(String, String, String, List)} per ogni binder
     * coinvolto.
     * 2. Aggiorna lo stato globale della sessione a COMPLETED.
     * 3. Persiste lo stato finale della transazione tramite il DAO.
     * 
     * @param session La sessione di scambio da finalizzare.
     */
    public void performTrade(TradeSession session) {
        if (session == null || binderDao == null || tradeSessionDao == null)
            return;

        // Raggruppa le carte per Set ID per identificare i binder coinvolti
        Set<String> involvedSets = new HashSet<>();
        session.getOfferedCards().forEach(c -> involvedSets.add(c.getCardSetID()));
        session.getRequestedCards().forEach(c -> involvedSets.add(c.getCardSetID()));

        for (String setId : involvedSets) {
            // Trasferimento: Proponente -> Ricevente (Carte Offerte)
            processCardTransfer(session.getProposerId(), session.getReceiverId(), setId, session.getOfferedCards());

            // Trasferimento: Ricevente -> Proponente (Carte Richieste)
            processCardTransfer(session.getReceiverId(), session.getProposerId(), setId, session.getRequestedCards());
        }

        session.updateTradeStatus(TradeStatus.COMPLETED);
        tradeSessionDao.updateTradeSessionStatus(session);

        if (storeView != null) {
            storeView.onFinalizeTrade();
        }
    }

    /**
     * Gestisce lo spostamento atomico di una lista di carte tra due utenti
     * all'interno di un binder specifico.
     * 
     * @param fromUser Nome utente del cedente.
     * @param toUser   Nome utente del destinatario.
     * @param setId    Identificatore del set (binder) coinvolto.
     * @param cards    Elenco globale delle carte da cui filtrare quelle
     *                 appartenenti al set.
     */
    private void processCardTransfer(String fromUser, String toUser, String setId,
            List<Card> cards) {
        List<Card> cardsToTransfer = cards.stream()
                .filter(c -> c.getCardSetID().equals(setId))
                .toList();

        if (cardsToTransfer.isEmpty())
            return;

        Binder fromBinder = binderDao.getBinderByOwnerAndSet(fromUser, setId);
        String setName = fromBinder != null ? fromBinder.getSetName() : "Set Sconosciuto";
        Binder toBinder = binderDao.findOrCreateBinder(toUser, setId, setName);

        if (fromBinder != null) {
            for (Card c : cardsToTransfer) {
                fromBinder.removeCard(c);
            }
            binderDao.save(fromBinder);
        }

        if (toBinder != null) {
            for (Card c : cardsToTransfer) {
                toBinder.addCard(c);
            }
            binderDao.save(toBinder);
        }
    }

    /**
     * Associa la vista corretta (Collezionista o Negozio) al controller.
     * Riconosce automaticamente il tipo di vista tramite controllo dei tipi.
     * 
     * @param view L'istanza della vista da associare.
     */
    public void setView(IView view) {
        if (view instanceof ICollectorTradeView collectorTradeView) {
            this.collectorView = collectorTradeView;
        } else if (view instanceof IStoreTradeView storeTradeView) {
            this.storeView = storeTradeView;
        }
    }

    /**
     * Valida un codice numerico fornito da un collezionista presso il negozio
     * fisico.
     * Se il codice è valido, aggiorna lo stato della sessione. Se è il primo
     * partecipante ad arrivare,
     * attiva una notifica tramite {@link UserInStoreEvent} notificando gli
     * osservatori del pattern Observer.
     * 
     * @param code      Il codice numerico inserito dal negozio.
     * @param sessionId L'ID della sessione di scambio.
     */
    public void verifySessionCode(int code, int sessionId) {
        if (tradeSessionDao == null || storeView == null)
            return;

        TradeSession session = tradeSessionDao.getTradeSessionById(sessionId);
        if (session == null) {
            storeView.showError("Sessione di scambio non trovata.");
            return;
        }

        boolean wasWaiting = session.getTradeStatus() == TradeStatus.WAITING_FOR_ARRIVAL;

        if (session.acceptSessionCode(code)) {
            tradeSessionDao.updateTradeSessionStatus(session);

            if (wasWaiting && session.getTradeStatus() == TradeStatus.PARTIALLY_ARRIVED) {
                User arrivedUser;
                if (code == session.getProposerSessionCode()) { // Arrivato il proponente
                    arrivedUser = new User(session.getProposerId(), "", "");
                } else { // Arrivato il ricevente
                    arrivedUser = new User(session.getReceiverId(), "", "");
                }
                notifyObservers(new UserInStoreEvent(session, arrivedUser));
            }

            if (storeView != null) {
                storeView.registerCodeValidation(code);
            }
            refreshStoreView(session);
        } else {
            storeView.showError("Codice di sessione non valido.");
        }
    }

    /**
     * Avvia ufficialmente la fase di ispezione fisica delle carte.
     * Disponibile solo quando entrambi i partecipanti sono arrivati in negozio.
     * 
     * @param sessionId ID della sessione di scambio.
     */
    public void startInspection(int sessionId) {
        if (tradeSessionDao == null)
            return;
        TradeSession session = tradeSessionDao.getTradeSessionById(sessionId);
        if (session != null && session.getTradeStatus() == TradeStatus.BOTH_ARRIVED) {
            session.updateTradeStatus(TradeStatus.INSPECTION_PHASE);
            tradeSessionDao.updateTradeSessionStatus(session);
            refreshStoreView(session);
        }
    }

    /**
     * Conferma che l'ispezione delle carte ha avuto esito positivo.
     * Procede alla finalizzazione dello scambio chiamando il metodo helper
     * {@link #performTrade(TradeSession)}.
     * 
     * @param sessionId ID della sessione di scambio.
     */
    public void passInspection(int sessionId) {
        if (tradeSessionDao == null)
            return;
        TradeSession session = tradeSessionDao.getTradeSessionById(sessionId);
        if (session != null && session.getTradeStatus() == TradeStatus.INSPECTION_PHASE) {
            session.updateTradeStatus(TradeStatus.INSPECTION_PASSED);
            tradeSessionDao.updateTradeSessionStatus(session);
            if (storeView != null) {
                storeView.registerInspectionSuccess();
            }
            performTrade(session);
            refreshStoreView(session);
        }
    }

    /**
     * Annulla lo scambio a causa di un'ispezione fallita delegando a
     * {@link #cancelTrade(int)}.
     */
    public void failInspection(int sessionId) {
        if (storeView != null) {
            storeView.registerInspectionFail();
        }
        cancelTrade(sessionId);
    }

    /** Annulla permanentemente una sessione di scambio. */
    public void cancelTrade(int sessionId) {
        if (tradeSessionDao == null)
            return;
        TradeSession session = tradeSessionDao.getTradeSessionById(sessionId);
        if (session != null) {
            session.updateTradeStatus(TradeStatus.CANCELLED);
            tradeSessionDao.updateTradeSessionStatus(session);
            refreshStoreView(session);
        }
    }

    /**
     * Determina se uno scambio è attualmente in fase attiva (non concluso).
     *
     * @param status Lo stato dello scambio da verificare.
     * @return {@code true} se lo scambio è attivo, {@code false} altrimenti.
     */
    private boolean isTradeActive(TradeStatus status) {
        return status == TradeStatus.WAITING_FOR_ARRIVAL ||
                status == TradeStatus.PARTIALLY_ARRIVED ||
                status == TradeStatus.BOTH_ARRIVED ||
                status == TradeStatus.INSPECTION_PHASE ||
                status == TradeStatus.INSPECTION_PASSED;
    }

    /**
     * Determina se uno scambio ha concluso il suo ciclo di vita (completato o
     * terminato).
     *
     * @param status Lo stato dello scambio da verificare.
     * @return {@code true} se lo scambio è terminato, {@code false} altrimenti.
     */
    private boolean isTradeCompleted(TradeStatus status) {
        return status == TradeStatus.COMPLETED ||
                status == TradeStatus.CANCELLED ||
                status == TradeStatus.EXPIRED;
    }

    /**
     * Aggiorna lo stato della vista del negozio con i dati della sessione corrente.
     * Converte l'entità di dominio in bean per garantire il disaccoppiamento del
     * layer view.
     * 
     * @param session L'entità di dominio da mostrare nel negozio.
     */
    private void refreshStoreView(TradeSession session) {
        if (storeView != null && session != null) {
            TradeSessionBean sessionBean = TradeSessionMapper.toBean(session);
            storeView.showTradeDetails(sessionBean, null, null);
        }
    }

    /**
     * Comanda al controller principale di navigare alla homepage del collezionista.
     * Utile per resettare la vista dopo un'operazione o alla pressione del tasto
     * home.
     */
    public void goToCollectorHomepage() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTOR_HOMEPAGE);
    }
    /**
     * Comanda la navigazione alla homepage del negozio.
     * Utile per tornare indietro dalle schermate di dettaglio o di gestione.
     */
    public void goToStoreHomepage() {
        if (applicationController != null) {
            applicationController.navigateTO(ViewPage.STORE_HOMEPAGE);
        }
    }
    /**
     * Comanda al controller principale di navigare alla visualizzazione della
     * collezione.
     */
    public void goToCollection() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.COLLECTION);
    }

    /**
     * Comanda al controller principale di navigare alla gestione delle proposte.
     */
    public void manageProposals() {
        if (applicationController != null)
            applicationController.navigateTO(ViewPage.MANAGE_PROPOSAL);
    }

    /**
     * Gestisce l'uscita dell'utente dal sistema.
     * Delega la logica di pulizia della sessione a
     * {@link ApplicationController#logout()}.
     */
    public void logout() {
        if (applicationController != null) {
            applicationController.logout();
        }
    }
}
