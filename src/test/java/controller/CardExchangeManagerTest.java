package controller;

import exception.ConnectionException;
import model.bean.CardBean;
import model.dao.IBinderDao;
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
}
