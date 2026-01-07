package model.api;

import model.api.dto.MagicCardDTO;
import model.domain.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MagicCardAdapterTest {

    private MagicCardAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MagicCardAdapter();
    }

    @Test
    void testAdaptToDomain_CompleteDTO_ConvertsSuccessfully() {
        
        MagicCardDTO dto = createCompleteDTO();

        
        Card card = adapter.adaptToDomain(dto);

        
        assertNotNull(card);
        assertEquals("15e45fe0-92ea-52dc-8665-7105ac30db70", card.getId());
        assertEquals("Narset, Enlightened Master", card.getName());
        assertEquals("Legendary", card.getSupertype());
        assertEquals(Arrays.asList("Human", "Monk"), card.getSubtypes());
        assertEquals("2", card.getHp()); 
        assertEquals(Arrays.asList("R", "U", "W"), card.getTypes()); 
        assertEquals("Mythic", card.getRarity());
    }

    @Test
    void testAdaptToDomain_WithSetInfo_MapsSetCorrectly() {
        
        MagicCardDTO dto = createCompleteDTO();

        
        Card card = adapter.adaptToDomain(dto);

        
        assertEquals("KTK", card.getSet());
        assertEquals("Khans of Tarkir", card.getSetName());
    }

    @Test
    void testAdaptToDomain_WithImage_MapsImageCorrectly() {
        
        MagicCardDTO dto = createCompleteDTO();

        
        Card card = adapter.adaptToDomain(dto);

        
        assertNotNull(card.getImageUrl());
        assertTrue(card.getImageUrl().contains("gatherer.wizards.com"));
    }

    @Test
    void testAdaptToDomain_NullDTO_ReturnsNull() {
        
        Card card = adapter.adaptToDomain(null);

        
        assertNull(card);
    }

    @Test
    void testAdaptToDomain_NullCardData_ReturnsNull() {
        
        MagicCardDTO dto = new MagicCardDTO();
        dto.setCard(null);

        
        Card card = adapter.adaptToDomain(dto);

        
        assertNull(card);
    }

    @Test
    void testAdaptToDTO_CompleteCard_ConvertsSuccessfully() {
        
        Card card = createCompleteCard();

        
        MagicCardDTO dto = adapter.adaptToDTO(card);

        
        assertNotNull(dto);
        assertNotNull(dto.getCard());
        assertEquals("test-id", dto.getCard().getId());
        assertEquals("Test Card", dto.getCard().getName());
    }

    @Test
    void testAdaptToDTO_NullCard_ReturnsNull() {
        
        MagicCardDTO dto = adapter.adaptToDTO(null);

        
        assertNull(dto);
    }

    

    private MagicCardDTO createCompleteDTO() {
        MagicCardDTO dto = new MagicCardDTO();
        MagicCardDTO.CardData cardData = new MagicCardDTO.CardData();

        cardData.setId("15e45fe0-92ea-52dc-8665-7105ac30db70");
        cardData.setName("Narset, Enlightened Master");
        cardData.setManaCost("{3}{U}{R}{W}");
        cardData.setCmc(6.0);
        cardData.setColors(Arrays.asList("R", "U", "W"));
        cardData.setColorIdentity(Arrays.asList("R", "U", "W"));
        cardData.setType("Legendary Creature — Human Monk");
        cardData.setSupertypes(Arrays.asList("Legendary"));
        cardData.setTypes(Arrays.asList("Creature"));
        cardData.setSubtypes(Arrays.asList("Human", "Monk"));
        cardData.setRarity("Mythic");
        cardData.setSet("KTK");
        cardData.setSetName("Khans of Tarkir");
        cardData.setText("First strike, hexproof\nWhenever Narset, Enlightened Master attacks...");
        cardData.setArtist("Magali Villeneuve");
        cardData.setNumber("190");
        cardData.setPower("3");
        cardData.setToughness("2");
        cardData.setLayout("normal");
        cardData.setMultiverseid("386616");
        cardData.setImageUrl("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=386616&type=card");
        cardData.setWatermark("jeskai");

        dto.setCard(cardData);
        return dto;
    }

    private Card createCompleteCard() {
        Card card = new Card();
        card.setId("test-id");
        card.setName("Test Card");
        card.setSupertype("Creature");
        card.setSubtypes(Arrays.asList("Human", "Warrior"));
        card.setHp("3");
        card.setTypes(Arrays.asList("R", "W"));
        card.setRarity("Rare");
        card.setSet("TST");
        card.setSetName("Test Set");
        card.setArtist("Test Artist");
        card.setCardNumber("1");
        card.setImageUrl("http://test.com/image.jpg");
        return card;
    }
}
