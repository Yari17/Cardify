package controller;

import exception.ConnectionException;
import model.bean.CardBean;
import model.dao.IBinderDao;
import model.dao.demo.DemoBinderDao;
import model.domain.Binder;
import model.domain.Card;
import model.domain.TradeTransaction;
import model.domain.enumerations.CardGameType;
import model.domain.enumerations.TradeStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;



class CardExchangeManagerTest {

    static class FakeBinderDao implements IBinderDao {
        
        private final Map<String, List<Binder>> store = new HashMap<>();

        @Override
        public Optional<Binder> get(long id) { return Optional.empty(); }

        @Override
        public void save(Binder binder) {
            if (binder == null) return;
            store.computeIfAbsent(binder.getOwner(), k -> new ArrayList<>());
            List<Binder> lst = store.get(binder.getOwner());
            
            for (int i = 0; i < lst.size(); i++) {
                if (lst.get(i).getSetId().equals(binder.getSetId())) {
                    lst.set(i, binder);
                    return;
                }
            }
            lst.add(binder);
        }

        @Override
        public void update(Binder binder, String[] params) { /* not used */ }

        @Override
        public void delete(Binder binder) { /* not used */ }

        @Override
        public List<Binder> getUserBinders(String owner) {
            return new ArrayList<>(store.getOrDefault(owner, new ArrayList<>()));
        }

        @Override
        public List<Binder> getBindersExcludingOwner(String owner) { return new ArrayList<>(); }

        @Override
        public void createBinder(String owner, String setId, String setName) {
            Binder b = new Binder(owner, setId, setName);
            store.computeIfAbsent(owner, k -> new ArrayList<>()).add(b);
        }

        @Override
        public void deleteBinder(String binderId) { /* not used */ }
    }

    static class FakeCardProvider implements model.api.ICardProvider {
        private final Map<String, String> sets;

        FakeCardProvider(Map<String, String> sets) { this.sets = sets; }

        @Override
        public List<Card> searchSet(String setId) throws ConnectionException {
            return List.of();
        }

        @Override
        public List<Card> searchCardsByName(String cardName) throws ConnectionException {
            return List.of();
        }

        @Override
        public <T extends Card> T getCardDetails(String cardId) throws ConnectionException {
            return null;
        }

        @Override
        public Map<String, String> getAllSets() { return sets; }
    }

    // Verifica che il ricevente riceva la carta offerta e la quantità venga sommata, mentre il proponente perda la carta
    @Test
    void executeExchange_mergesReceiverCardAnd_removesFromProposer() {
        FakeBinderDao binderDao = new FakeBinderDao();
        Map<String,String> sets = new HashMap<>();
        sets.put("base5", "Team Rocket");
        FakeCardProvider cardProvider = new FakeCardProvider(sets);

        
        Binder receiverBinder = new Binder("user2", "base5", "Team Rocket");
        CardBean existing = new CardBean();
        existing.setId("base5-9");
        existing.setQuantity(1);
        receiverBinder.addCard(existing);
        binderDao.save(receiverBinder);

        
        Binder proposerBinder = new Binder("user1", "base5", "Team Rocket");
        CardBean proposerCard = new CardBean();
        proposerCard.setId("base5-9");
        proposerCard.setQuantity(1);
        proposerBinder.addCard(proposerCard);
        binderDao.save(proposerBinder);

        
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeA");
        List<Card> offered = new ArrayList<>();
        Card offeredCard = new Card("base5-9", "Dark Hypno", "", CardGameType.POKEMON);
        offeredCard.setQuantity(1);
        offered.add(offeredCard);

        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now(), offered, Collections.emptyList());
        TradeTransaction tx = new TradeTransaction(1, TradeStatus.INSPECTION_PASSED, participants, details);

        
        CardExchangeManager mgr = new CardExchangeManager(binderDao, cardProvider);
        mgr.executeExchange(tx);

        
        List<CardBean> receiverCards = binderDao.getUserBinders("user2").get(0).getCards();
        assertEquals(1, receiverCards.size(), "Receiver binder should have one entry for the card");
        assertEquals("base5-9", receiverCards.get(0).getId());
        assertEquals(2, receiverCards.get(0).getQuantity(), "Receiver should have quantity 2 after receiving one more copy");

        
        List<CardBean> proposerCards = binderDao.getUserBinders("user1").get(0).getCards();
        assertTrue(proposerCards.stream().noneMatch(cb -> "base5-9".equals(cb.getId())), "Proposer binder should no longer contain the offered card");
    }


    // Verifica che se si offrono più copie, il ricevente ottenga la quantità corretta e che la carta richiesta venga rimossa dal ricevente
    @Test
    void executeExchange_receiverGetsCorrectQuantity_whenOfferedMultiple() {
        DemoBinderDao binderDao = new DemoBinderDao();

        // Prepare proposer binder with 2 Flareon (base2-3)
        Binder proposerBinder = new Binder("collectortest1", "base2", "Base 2");
        CardBean proposerCard = new CardBean("base2-3", "Flareon", "https://example", CardGameType.POKEMON);
        proposerCard.setQuantity(2);
        proposerBinder.addCard(proposerCard);
        binderDao.save(proposerBinder);

        // Prepare receiver binder with 4 Vaporeon (sm115-18) and no Flareon
        Binder receiverBinderV = new Binder("collectortest2", "sm115", "SM115");
        CardBean receiverV = new CardBean("sm115-18", "Vaporeon", "https://example", CardGameType.POKEMON);
        receiverV.setQuantity(4);
        receiverBinderV.addCard(receiverV);
        binderDao.save(receiverBinderV);

        // Also ensure receiver has a binder for the offered card set (base2) absent -> will be created by manager
        // Create offered/requested Card domain objects
        Card offered = new Card("base2-3", "Flareon", "https://example", CardGameType.POKEMON);
        offered.setQuantity(2);
        Card requested = new Card("sm115-18", "Vaporeon", "https://example", CardGameType.POKEMON);
        requested.setQuantity(1);

        // Build TradeTransaction
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants("collectortest1", "collectortest2", "Store1");
        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of(offered), List.of(requested));
        TradeTransaction tx = new TradeTransaction(1, TradeStatus.COMPLETED, participants, details);

        // Minimal ICardProvider stub that returns set names
        model.api.ICardProvider cardProvider = new model.api.ICardProvider() {
            @Override
            public List<Card> searchSet(String setId) { throw new UnsupportedOperationException(); }
            @Override
            public List<Card> searchCardsByName(String cardName) { throw new UnsupportedOperationException(); }
            @Override
            public <T extends Card> T getCardDetails(String cardId) { throw new UnsupportedOperationException(); }
            @Override
            public Map<String, String> getAllSets() {
                return Map.of("base2", "Base 2", "sm115", "SM115");
            }
        };

        CardExchangeManager manager = new CardExchangeManager(binderDao, cardProvider);

        // Act
        manager.executeExchange(tx);

        // Assert: receiver should now have a binder for base2 with Flareon quantity 2
        List<Binder> receiverBinders = binderDao.getUserBinders("collectortest2");
        Binder base2Binder = receiverBinders.stream().filter(b -> "base2".equals(b.getSetId())).findFirst().orElse(null);
        assertNotNull(base2Binder, "Receiver should have a binder for set base2 after exchange");
        CardBean receivedFlareon = base2Binder.getCards().stream().filter(c -> "base2-3".equals(c.getId())).findFirst().orElse(null);
        assertNotNull(receivedFlareon, "Receiver should have received Flareon card");
        assertEquals(2, receivedFlareon.getQuantity(), "Receiver should have quantity 2 of Flareon after exchange");

        // Also assert receiver kept existing Vaporeon quantity
        Binder smBinder = receiverBinders.stream().filter(b -> "sm115".equals(b.getSetId())).findFirst().orElse(null);
        assertNotNull(smBinder);
        CardBean vap = smBinder.getCards().stream().filter(c -> "sm115-18".equals(c.getId())).findFirst().orElse(null);
        assertNotNull(vap);
        assertEquals(3, vap.getQuantity(), "Receiver's Vaporeon quantity should be decremented by 1 (requested)");
    }
}
