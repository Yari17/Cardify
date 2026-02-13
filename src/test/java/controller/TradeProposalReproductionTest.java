package controller;

import model.bean.CardBean;
import model.dao.IBinderDao;
import model.dao.IProposalDao;
import model.dao.IUserDao;
import model.domain.Binder;
import model.domain.CollectionItem;
import model.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeProposalReproductionTest {

    @Mock
    private ApplicationController applicationController;
    @Mock
    private IUserDao userDao;
    @Mock
    private IProposalDao proposalDao;
    @Mock
    private IBinderDao binderDao;
    @Mock
    private User sessionUser;

    private TradeProposalController controller;

    @BeforeEach
    void setUp() {
        controller = new TradeProposalController(applicationController, sessionUser, userDao, proposalDao, binderDao);
    }

    @Test
    void testGetUserCollection_ShouldPopulateCache() {
        // Arrange
        String cardId = "card-123";
        String username = "testUser";

        when(sessionUser.getUsername()).thenReturn(username);

        // Mock binder DAO to return a binder with one card
        // Add card details for mapper
        model.domain.Card cardDetails = new model.domain.Card();
        cardDetails.setCardID(cardId);
        cardDetails.setCardName("Test Card");

        CollectionItem item = new CollectionItem(cardDetails, 2);

        java.util.List<CollectionItem> items = new java.util.ArrayList<>();
        items.add(item);
        Binder mockBinder = new Binder(username, "set-1", "Set 1", items);

        when(binderDao.getUserBinders(username)).thenReturn(Collections.singletonList(mockBinder));

        // Act
        List<CardBean> collection = controller.getUserCollection();

        // Assert
        assertFalse(collection.isEmpty(), "Collection should not be empty");
        assertEquals(cardId, collection.get(0).getId(), "Card ID should match");

        // Verify cache population - THIS IS THE CRITICAL CHECK
        CardBean cachedCard = controller.getProposalCandidate(cardId);
        assertNotNull(cachedCard, "Card should be cached in proposalCandidates after getUserCollection()");

        // Verify addOfferedCard works with the cache
        controller.addOfferedCard(cardId, 1);
        Map<String, Integer> offered = controller.getOfferedCardsMap();
        assertTrue(offered.containsKey(cardId), "Offered map should contain the card");
        assertEquals(1, offered.get(cardId), "Offered quantity should be 1");
    }
}
