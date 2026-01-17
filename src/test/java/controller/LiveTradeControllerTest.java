package controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.dao.IBinderDao;
import model.dao.ITradeDao;
import model.api.ICardProvider;
import model.dao.factory.DaoFactory;
import model.domain.Card;
import model.domain.Binder;
import model.domain.TradeTransaction;
import model.domain.enumerations.TradeStatus;
import java.time.LocalDateTime;
import java.util.*;

class LiveTradeControllerTest {
    private StubApplicationController nav;
    private StubTradeDao tradeDao;
    private StubBinderDao binderDao;
    private StubCardProvider cardProvider;
    private LiveTradeController controllerUser1;
    private LiveTradeController controllerUser2;

    @BeforeEach
    void setUp() {
        tradeDao = new StubTradeDao();
        binderDao = new StubBinderDao();
        cardProvider = new StubCardProvider();
        nav = new StubApplicationController(tradeDao, binderDao, cardProvider);
        controllerUser1 = new LiveTradeController("user1", nav);
        controllerUser2 = new LiveTradeController("user2", nav);
    }

    @Test
    void confirmPresence_firstCollector_setsPartiallyArrived() {
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(1, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        tradeDao.save(tx);
        int code = controllerUser1.confirmPresence(1);
        assertTrue(code > 0);
        assertEquals(TradeStatus.PARTIALLY_ARRIVED, tradeDao.getTradeTransactionById(1).getTradeStatus());
    }

    @Test
    void confirmPresence_secondCollector_setsBothArrived() {
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(2, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        tradeDao.save(tx);
        int code1 = controllerUser1.confirmPresence(2);
        assertTrue(code1 > 0);
        int code2 = controllerUser2.confirmPresence(2);
        assertTrue(code2 > 0);
        assertEquals(TradeStatus.BOTH_ARRIVED, tradeDao.getTradeTransactionById(2).getTradeStatus());
    }

    @Test
    void verifySessionCode_movesToInspectionPhase_whenBothCodesMatch() {
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(3, TradeStatus.BOTH_ARRIVED, participants, details);
        tx.confirmPresence("user1");
        tx.confirmPresence("user2");
        tradeDao.save(tx);
        boolean ok1 = controllerUser1.verifySessionCode(3, tx.getProposerSessionCode());
        assertTrue(ok1);
        boolean ok2 = controllerUser1.verifySessionCode(3, tx.getReceiverSessionCode());
        assertTrue(ok2);
        assertEquals(TradeStatus.INSPECTION_PHASE, tradeDao.getTradeTransactionById(3).getTradeStatus());
    }

    @Test
    void fullTradeFlow_performCardExchange_updatesBinders() {
        var c = new Card("SET1-001", "Charizard", "url1", null);
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeX");
        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            List.of(c),
            List.of()
        );
        TradeTransaction tx = new TradeTransaction(0, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        tradeDao.save(tx);
        cardProvider.sets.put("SET1", "Set Uno");
        controllerUser1.performCardExchange(tx);
        assertTrue(binderDao.getUserBinders("user2").stream().anyMatch(b -> b.getSetId().equals("SET1")));
    }

    @Test
    void confirmPresence_withNonExistentTransaction_returnsErrorCode() {
        // Test: conferma presenza su transazione non esistente
        int code = controllerUser1.confirmPresence(999);
        assertEquals(-1, code, "Se la transazione non esiste, deve restituire -1");
    }

    @Test
    void verifySessionCode_withWrongCode_returnsFalse() {
        // Test: verifica session code errato
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(4, TradeStatus.BOTH_ARRIVED, participants, details);
        tx.confirmPresence("user1");
        tx.confirmPresence("user2");
        tradeDao.save(tx);
        boolean ok = controllerUser1.verifySessionCode(4, 123456); // codice errato
        assertTrue(!ok, "Codice errato deve restituire false");
    }

    @Test
    void performCardExchange_doesNotAddDuplicateBinders() {
        // Test: scambio con carta già presente nel binder
        var c = new Card("SET2-001", "Bulbasaur", "url2", null);
        binderDao.createBinder("user2", "SET2", "Set Due");
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeX");
        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            List.of(c),
            List.of()
        );
        TradeTransaction tx = new TradeTransaction(5, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        tradeDao.save(tx);
        cardProvider.sets.put("SET2", "Set Due");
        controllerUser1.performCardExchange(tx);
        long count = binderDao.getUserBinders("user2").stream().filter(b -> b.getSetId().equals("SET2")).count();
        assertEquals(1, count, "Non devono esserci duplicati di binder per lo stesso set");
    }

    @Test
    void performCardExchange_withNoCardsOffered_doesNothing() {
        // Test: scambio con carte offerte vuote
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeX");
        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            List.of(),
            List.of()
        );
        TradeTransaction tx = new TradeTransaction(6, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        tradeDao.save(tx);
        controllerUser1.performCardExchange(tx);
        assertTrue(binderDao.getUserBinders("user2").isEmpty(), "Se non ci sono carte offerte, il binder non deve cambiare");
    }

    @Test
    void confirmPresence_whenAlreadyBothArrived_doesNotChangeStatus() {
        // Test: conferma presenza su transazione già BOTH_ARRIVED
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(7, TradeStatus.BOTH_ARRIVED, participants, details);
        tradeDao.save(tx);
        int code = controllerUser1.confirmPresence(7);
        assertEquals(-1, code, "Se lo stato è già BOTH_ARRIVED, non deve cambiare");
        assertEquals(TradeStatus.BOTH_ARRIVED, tradeDao.getTradeTransactionById(7).getTradeStatus());
    }

    @Test
    void inspectionFail_setsStatusToCancelled() {
        // Test: ispezione fallita annulla lo scambio
        var participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        var details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(), List.of());
        TradeTransaction tx = new TradeTransaction(8, TradeStatus.INSPECTION_PHASE, participants, details);
        tradeDao.save(tx);
        controllerUser1.failInspection(8, "user1");
        assertEquals(TradeStatus.CANCELLED, tradeDao.getTradeTransactionById(8).getTradeStatus(), "Se l'ispezione fallisce, lo stato deve essere CANCELLED");
    }

    @Test
    void completeTrade_setsStatusToCompleted() {
        // Test: completamento dello scambio
        var c = new Card("SET3-001", "Squirtle", "url3", null);
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeX");
        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            List.of(c),
            List.of()
        );
        TradeTransaction tx = new TradeTransaction(9, TradeStatus.INSPECTION_PHASE, participants, details);
        tradeDao.save(tx);
        controllerUser1.completeTrade(9);
        assertEquals(TradeStatus.COMPLETED, tradeDao.getTradeTransactionById(9).getTradeStatus(), "Dopo il completamento, lo stato deve essere COMPLETED");
    }

    // Stub implementations
    static class StubTradeDao implements ITradeDao {
        private final Map<Integer, TradeTransaction> map = new HashMap<>();
        @Override public TradeTransaction getTradeTransactionById(int id) { return map.get(id); }
        @Override public void save(TradeTransaction tx) { map.put(tx.getTransactionId(), tx); }
        @Override public void updateTransactionStatus(int id, String status) {
            TradeTransaction tx = map.get(id);
            if (tx != null && status != null) tx.updateTradeStatus(model.domain.enumerations.TradeStatus.valueOf(status));
        }
        @Override public TradeTransaction getTradeTransactionBySessionCodes(int proposerCode, int receiverCode) { return null; }
        @Override public List<TradeTransaction> getUserTradeTransactions(String username) { return new ArrayList<>(map.values()); }
        @Override public List<TradeTransaction> getStoreTradeScheduledTransactions(String userId, String tradeId) { return new ArrayList<>(map.values()); }
        @Override public List<TradeTransaction> getUserTradeTransactions(String userId, String tradeId) { return new ArrayList<>(map.values()); }
        @Override public Optional<TradeTransaction> findByParticipantsAndDate(String proposerId, String receiverId, LocalDateTime tradeDate) { return Optional.empty(); }
        @Override public Optional<TradeTransaction> get(long id) { return Optional.empty(); }
        @Override public void update(TradeTransaction t, String[] params) {}
        @Override public void delete(TradeTransaction t) {}
    }
    static class StubBinderDao implements IBinderDao {
        private final Map<String, List<Binder>> binders = new HashMap<>();
        @Override public List<Binder> getUserBinders(String owner) { return binders.getOrDefault(owner, new ArrayList<>()); }
        @Override public List<Binder> getBindersExcludingOwner(String owner) { return new ArrayList<>(); }
        @Override public void createBinder(String owner, String setId, String setName) {
            binders.computeIfAbsent(owner, k -> new ArrayList<>()).add(new Binder(owner, setId, setName));
        }
        @Override public void deleteBinder(String binderId) {}
        @Override public Optional<Binder> get(long id) { return Optional.empty(); }
        @Override public void save(Binder t) {}
        @Override public void update(Binder t, String[] params) {}
        @Override public void delete(Binder t) {}
    }
    static class StubCardProvider implements ICardProvider {
        Map<String, String> sets = new HashMap<>();
        @Override public Map<String, String> getAllSets() { return sets; }
        @Override public List<Card> searchSet(String setId) { return new ArrayList<>(); }
        @Override public List<Card> searchCardsByName(String cardName) { return new ArrayList<>(); }
        @Override public <T extends Card> T getCardDetails(String cardId) { return null; }
    }
    static class StubDaoFactory extends DaoFactory {
        private final ITradeDao tradeDao; private final IBinderDao binderDao;
        public StubDaoFactory(ITradeDao t, IBinderDao b) { tradeDao = t; binderDao = b; }
        @Override public ITradeDao createTradeDao() { return tradeDao; }
        @Override public IBinderDao createBinderDao() { return binderDao; }
        @Override public model.dao.IUserDao createUserDao() { return null; }
        @Override public model.dao.IProposalDao createProposalDao() { return null; }
    }
    static class StubApplicationController extends ApplicationController {
        private final ITradeDao tradeDao; private final IBinderDao binderDao; private final ICardProvider cardProvider;
        public StubApplicationController(ITradeDao t, IBinderDao b, ICardProvider c) { tradeDao = t; binderDao = b; cardProvider = c; }
        @Override public DaoFactory getDaoFactory() { return new StubDaoFactory(tradeDao, binderDao); }
        @Override public ICardProvider getCardProvider() { return cardProvider; }
    }
}
