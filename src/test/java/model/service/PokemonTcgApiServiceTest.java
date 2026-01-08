package model.service;

import model.domain.card.PokemonCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for PokemonTcgApiService
 * Note: Requires internet connection and valid API key
 */
class PokemonTcgApiServiceTest {

    private PokemonTcgApiService service;

    @BeforeEach
    void setUp() {
        service = new PokemonTcgApiService();
    }

    @Test
    void testSearchCardsByName() throws IOException, InterruptedException {
        // Search for a well-known Pokemon card
        List<PokemonCard> cards = service.searchCardsByName("Pikachu");

        assertNotNull(cards, "Cards list should not be null");
        assertFalse(cards.isEmpty(), "Should find at least one Pikachu card");

        // Verify the first card has expected data
        PokemonCard firstCard = cards.get(0);
        assertNotNull(firstCard.getId(), "Card should have an ID");
        assertTrue(firstCard.getName().contains("Pikachu"), "Card name should contain Pikachu");
    }

    @Test
    void testGetCardById() throws IOException, InterruptedException {
        // Test with a known card ID (Venusaur-EX from XY set)
        PokemonCard card = service.getCardById("xy1-1");

        assertNotNull(card, "Card should not be null");
        assertEquals("xy1-1", card.getId(), "Card ID should match");
        assertEquals("Venusaur-EX", card.getName(), "Card name should be Venusaur-EX");
        assertEquals("180", card.getHp(), "HP should be 180");
        assertNotNull(card.getTypes(), "Types should not be null");
        assertFalse(card.getTypes().isEmpty(), "Types should not be empty");
    }

    @Test
    void testGetCardsByType() throws IOException, InterruptedException {
        // Search for Fire type cards
        List<PokemonCard> cards = service.getCardsByType("Fire");

        assertNotNull(cards, "Cards list should not be null");
        assertFalse(cards.isEmpty(), "Should find Fire type cards");

        // Verify at least one card has Fire type
        boolean hasFireType = cards.stream()
            .anyMatch(card -> card.getTypes() != null && card.getTypes().contains("Fire"));
        assertTrue(hasFireType, "At least one card should have Fire type");
    }

    @Test
    void testGetCardsBySet() throws IOException, InterruptedException {
        // Search for cards from Base Set
        List<PokemonCard> cards = service.getCardsBySet("base1");

        assertNotNull(cards, "Cards list should not be null");
        assertFalse(cards.isEmpty(), "Should find cards from base1 set");

        // Verify cards have set information
        PokemonCard firstCard = cards.get(0);
        assertNotNull(firstCard.getSetName(), "Card should have set name");
    }

    @Test
    void testInvalidCardId() {
        // Test with an invalid card ID
        assertThrows(IOException.class, () -> {
            service.getCardById("invalid-id-12345");
        }, "Should throw IOException for invalid card ID");
    }

    @Test
    void testCardValidation() throws IOException, InterruptedException {
        // Get a card and test its validation
        PokemonCard card = service.getCardById("xy1-1");

        assertTrue(card.isValid(), "Fetched card should be valid");
    }
}
