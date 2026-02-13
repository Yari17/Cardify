package controller;

import model.dao.demo.DemoBinderDao;
import model.dao.demo.DemoTradeSessionDao;
import model.domain.*;
import model.domain.enumerations.CardGameType;
import model.domain.enumerations.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per TradeController usando JUnit.
 * Usa Demo DAO reali
 */
class TradeControllerTest {

    private TradeController controller;
    private DemoBinderDao demoBinderDao;
    private DemoTradeSessionDao demoTradeSessionDao;
    private User testUser;

    // Carte test
    private Card card1;
    private Card card2;
    private Card card3;

    @BeforeEach
    void setUp() {
        demoBinderDao = new DemoBinderDao();
        demoTradeSessionDao = new DemoTradeSessionDao();
        testUser = new User("userA", "password", "Collezionista");

        controller = new TradeController(null, testUser, demoTradeSessionDao, demoBinderDao);

        // Setup carte test
        card1 = new Card("Card 1", "card1", "set1", "img1.png", CardGameType.POKEMON);
        card2 = new Card("Card 2", "card2", "set1", "img2.png", CardGameType.POKEMON);
        card3 = new Card("Card 3", "card3", "set2", "img3.png", CardGameType.POKEMON);
    }

    @Test
    void testCreateBinderWhenReceivingCardFromNewSet() {
        String fromUser = "userA";
        String toUser = "userB";

        // Crea binder cedente con card3
        List<CollectionItem> items = new ArrayList<>();
        items.add(new CollectionItem(card3, 1));
        Binder fromBinder = new Binder(fromUser, "set2", "Set 2", items);
        demoBinderDao.createBinder(fromUser, fromBinder);

        // UserB non ha binder per set2 (verificato implicitamente)

        // Crea sessione con card3 da scambiare
        List<Card> offeredCards = List.of(card3);
        TradeSession session = new TradeSession(
                1,
                TradeStatus.INSPECTION_PASSED,
                new TradeSession.TradeParticipants(fromUser, toUser, "store1"),
                new TradeSession.TradeDetails(LocalDateTime.now(), LocalDateTime.now(), offeredCards, List.of()));

        // Esegue scambio
        controller.performTrade(session);

        // Verifica che binder destinatario sia stato creato automaticamente
        Binder toBinder = demoBinderDao.getBinderByOwnerAndSet(toUser, "set2");
        assertNotNull(toBinder, "Binder destinatario dovrebbe essere stato auto-creato");

        // Verifica che contenga la carta ricevuta
        assertEquals(1, toBinder.getOwnedCards().size());
        assertEquals("card3", toBinder.getOwnedCards().get(0).getCard().getCardID());
    }

    @Test
    void testCorrectQuantityOfCardsTransferred() {
        String userA = "userA";
        String userB = "userB";

        // UserA ha 5 copie di card1
        List<CollectionItem> itemsA = new ArrayList<>();
        itemsA.add(new CollectionItem(card1, 5));
        Binder binderA = new Binder(userA, "set1", "Set 1", itemsA);
        demoBinderDao.createBinder(userA, binderA);

        // UserB ha 4 copie di card2
        List<CollectionItem> itemsB = new ArrayList<>();
        itemsB.add(new CollectionItem(card2, 4));
        Binder binderB = new Binder(userB, "set1", "Set 1", itemsB);
        demoBinderDao.createBinder(userB, binderB);

        // UserA offre 3 copie di card1, UserB offre 2 copie di card2
        List<Card> offeredByA = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            offeredByA.add(card1);
        }

        List<Card> offeredByB = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            offeredByB.add(card2);
        }

        TradeSession session = new TradeSession(
                1,
                TradeStatus.INSPECTION_PASSED,
                new TradeSession.TradeParticipants(userA, userB, "store1"),
                new TradeSession.TradeDetails(LocalDateTime.now(), LocalDateTime.now(), offeredByA, offeredByB));

        // Esegue scambio
        controller.performTrade(session);

        // Recupera binder aggiornati
        Binder updatedBinderA = demoBinderDao.getBinderByOwnerAndSet(userA, "set1");
        Binder updatedBinderB = demoBinderDao.getBinderByOwnerAndSet(userB, "set1");

        // Verifica quantità UserA: 2 copie card1 rimanenti, 2 copie card2 ricevute
        int card1Qty = updatedBinderA.getOwnedCards().stream()
                .filter(item -> item.getCard().getCardID().equals("card1"))
                .findFirst()
                .map(CollectionItem::getQuantity)
                .orElse(0);
        assertEquals(2, card1Qty, "UserA dovrebbe avere 2 copie di card1 rimaste");

        int card2QtyA = updatedBinderA.getOwnedCards().stream()
                .filter(item -> item.getCard().getCardID().equals("card2"))
                .findFirst()
                .map(CollectionItem::getQuantity)
                .orElse(0);
        assertEquals(2, card2QtyA, "UserA dovrebbe aver ricevuto 2 copie di card2");

        // Verifica quantità UserB: 2 copie card2 rimanenti, 3 copie card1 ricevute
        int card2QtyB = updatedBinderB.getOwnedCards().stream()
                .filter(item -> item.getCard().getCardID().equals("card2"))
                .findFirst()
                .map(CollectionItem::getQuantity)
                .orElse(0);
        assertEquals(2, card2QtyB, "UserB dovrebbe avere 2 copie di card2 rimaste");

        int card1QtyB = updatedBinderB.getOwnedCards().stream()
                .filter(item -> item.getCard().getCardID().equals("card1"))
                .findFirst()
                .map(CollectionItem::getQuantity)
                .orElse(0);
        assertEquals(3, card1QtyB, "UserB dovrebbe aver ricevuto 3 copie di card1");
    }
}
