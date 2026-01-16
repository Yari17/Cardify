package controller;

import model.dao.IBinderDao;
import model.api.ICardProvider;
import model.dao.factory.DaoFactory;
import model.domain.Card;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    // Stub implementations
    static class StubTradeDao implements ITradeDao {
        private final Map<Integer, TradeTransaction> map = new HashMap<>();
        @Override public TradeTransaction getTradeTransactionById(int id) { return map.get(id); }
        @Override public void save(TradeTransaction tx) { map.put(tx.getTransactionId(), tx); }
        @Override public void updateTransactionStatus(int id, String status) {
            TradeTransaction tx = map.get(id);
            if (tx != null && status != null) tx.updateTradeStatus(TradeStatus.valueOf(status));
        }
        // ...implement only needed methods...
        @Override public TradeTransaction getTradeTransactionBySessionCodes(int proposerCode, int receiverCode) { return null; }
        @Override public List<TradeTransaction> getUserTradeTransactions(String username) { return new ArrayList<>(map.values()); }
        @Override public List<TradeTransaction> getStoreTradeScheduledTransactions(String storeId, String filter) { return new ArrayList<>(map.values()); }
    }
    static class StubBinderDao implements IBinderDao {
        private final Map<String, List<Binder>> binders = new HashMap<>();
        @Override public List<Binder> getUserBinders(String username) { return binders.getOrDefault(username, new ArrayList<>()); }
        @Override public void createBinder(String username, String setId, String setName) {
            binders.computeIfAbsent(username, k -> new ArrayList<>()).add(new Binder(username, setId, setName));
        }
        @Override public void save(Binder binder) {}
    }
    static class StubCardProvider implements ICardProvider {
        Map<String, String> sets = new HashMap<>();
        @Override public Map<String, String> getAllSets() { return sets; }
        // ...implement only needed methods...
    }
    static class StubDaoFactory extends DaoFactory {
        private final ITradeDao tradeDao; private final IBinderDao binderDao;
        public StubDaoFactory(ITradeDao t, IBinderDao b) { tradeDao = t; binderDao = b; }
        @Override public ITradeDao createTradeDao() { return tradeDao; }
        @Override public IBinderDao createBinderDao() { return binderDao; }
    }
    static class StubApplicationController extends ApplicationController {
        private final ITradeDao tradeDao; private final IBinderDao binderDao; private final ICardProvider cardProvider;
        public StubApplicationController(ITradeDao t, IBinderDao b, ICardProvider c) { tradeDao = t; binderDao = b; cardProvider = c; }
        @Override public DaoFactory getDaoFactory() { return new StubDaoFactory(tradeDao, binderDao); }
        @Override public ICardProvider getCardProvider() { return cardProvider; }
    }
}
