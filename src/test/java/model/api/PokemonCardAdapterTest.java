package model.api.adapter;

import model.api.PokemonCardAdapter;
import model.api.dto.PokemonCardDTO;
import model.domain.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PokemonCardAdapterTest {

    private PokemonCardAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PokemonCardAdapter();
    }

    @Test
    void testAdaptToDomain_CompleteDTO_ConvertsSuccessfully() {
        
        PokemonCardDTO dto = createCompleteDTO();

        
        Card card = adapter.adaptToDomain(dto);

        
        assertNotNull(card);
        assertEquals("swsh4-25", card.getId());
        assertEquals("Charizard", card.getName());
        assertEquals("Pokémon", card.getSupertype());
        assertEquals(Arrays.asList("Stage 2"), card.getSubtypes());
        assertEquals("170", card.getHp());
        assertEquals(Arrays.asList("Fire"), card.getTypes());
        assertEquals("Rare", card.getRarity());
        assertEquals("25", card.getCardNumber());
        assertEquals("Ryuta Fuse", card.getArtist());
    }

    @Test
    void testAdaptToDomain_WithSetInfo_MapsSetCorrectly() {
        
        PokemonCardDTO dto = createCompleteDTO();

        
        Card card = adapter.adaptToDomain(dto);

        
        assertEquals("swsh4", card.getSet());
        assertEquals("Vivid Voltage", card.getSetName());
    }

    @Test
    void testAdaptToDomain_WithImages_MapsImagesCorrectly() {
        
        PokemonCardDTO dto = createCompleteDTO();

        
        Card card = adapter.adaptToDomain(dto);

        
        assertEquals("https://images.pokemontcg.io/swsh4/25.png", card.getImageUrl());
        assertEquals("https://images.pokemontcg.io/swsh4/25_hires.png", card.getImageUrlHiRes());
    }

    @Test
    void testAdaptToDomain_WithTCGPlayerPrice_ExtractsMarketPrice() {
        
        PokemonCardDTO dto = createCompleteDTO();

        
        Card card = adapter.adaptToDomain(dto);

        
        assertNotNull(card.getMarketPrice());
        assertEquals(2.82, card.getMarketPrice());
    }

    @Test
    void testAdaptToDomain_WithOnlyCardMarketPrice_ExtractsCardMarketPrice() {
        
        PokemonCardDTO dto = createDTOWithOnlyCardMarket();

        
        Card card = adapter.adaptToDomain(dto);

        
        assertNotNull(card.getMarketPrice());
        assertEquals(9.38, card.getMarketPrice());
    }

    @Test
    void testAdaptToDomain_NullDTO_ReturnsNull() {
        
        Card card = adapter.adaptToDomain(null);

        
        assertNull(card);
    }

    @Test
    void testAdaptToDomain_MinimalDTO_HandlesNullFields() {
        
        PokemonCardDTO dto = new PokemonCardDTO();
        dto.setId("test-1");
        dto.setName("Test Card");

        
        Card card = adapter.adaptToDomain(dto);

        
        assertNotNull(card);
        assertEquals("test-1", card.getId());
        assertEquals("Test Card", card.getName());
        assertNull(card.getSet());
        assertNull(card.getImageUrl());
        assertNull(card.getMarketPrice());
    }

    @Test
    void testAdaptToDTO_CompleteCard_ConvertsSuccessfully() {
        
        Card card = createCompleteCard();

        
        PokemonCardDTO dto = adapter.adaptToDTO(card);

        
        assertNotNull(dto);
        assertEquals("swsh4-25", dto.getId());
        assertEquals("Charizard", dto.getName());
        assertEquals("Pokémon", dto.getSupertype());
        assertEquals("170", dto.getHp());
        assertEquals("Rare", dto.getRarity());
    }

    @Test
    void testAdaptToDTO_NullCard_ReturnsNull() {
        
        PokemonCardDTO dto = adapter.adaptToDTO(null);

        
        assertNull(dto);
    }

    

    private PokemonCardDTO createCompleteDTO() {
        PokemonCardDTO dto = new PokemonCardDTO();
        dto.setId("swsh4-25");
        dto.setName("Charizard");
        dto.setSupertype("Pokémon");
        dto.setSubtypes(Arrays.asList("Stage 2"));
        dto.setHp("170");
        dto.setTypes(Arrays.asList("Fire"));
        dto.setRarity("Rare");
        dto.setNumber("25");
        dto.setArtist("Ryuta Fuse");

        
        PokemonCardDTO.SetDTO setDTO = new PokemonCardDTO.SetDTO();
        setDTO.setId("swsh4");
        setDTO.setName("Vivid Voltage");
        dto.setSet(setDTO);

        
        PokemonCardDTO.ImagesDTO imagesDTO = new PokemonCardDTO.ImagesDTO();
        imagesDTO.setSmall("https://images.pokemontcg.io/swsh4/25.png");
        imagesDTO.setLarge("https://images.pokemontcg.io/swsh4/25_hires.png");
        dto.setImages(imagesDTO);

        
        PokemonCardDTO.TcgPlayerDTO tcgPlayerDTO = new PokemonCardDTO.TcgPlayerDTO();
        PokemonCardDTO.PricesDTO pricesDTO = new PokemonCardDTO.PricesDTO();
        PokemonCardDTO.PriceDetailDTO normalPrice = new PokemonCardDTO.PriceDetailDTO();
        normalPrice.setMarket(2.82);
        pricesDTO.setNormal(normalPrice);
        tcgPlayerDTO.setPrices(pricesDTO);
        dto.setTcgplayer(tcgPlayerDTO);

        
        PokemonCardDTO.CardMarketDTO cardMarketDTO = new PokemonCardDTO.CardMarketDTO();
        PokemonCardDTO.CardMarketPricesDTO cardMarketPrices = new PokemonCardDTO.CardMarketPricesDTO();
        cardMarketPrices.setAverageSellPrice(9.38);
        cardMarketDTO.setPrices(cardMarketPrices);
        dto.setCardmarket(cardMarketDTO);

        return dto;
    }

    private PokemonCardDTO createDTOWithOnlyCardMarket() {
        PokemonCardDTO dto = new PokemonCardDTO();
        dto.setId("test-1");
        dto.setName("Test Card");

        
        PokemonCardDTO.CardMarketDTO cardMarketDTO = new PokemonCardDTO.CardMarketDTO();
        PokemonCardDTO.CardMarketPricesDTO cardMarketPrices = new PokemonCardDTO.CardMarketPricesDTO();
        cardMarketPrices.setAverageSellPrice(9.38);
        cardMarketDTO.setPrices(cardMarketPrices);
        dto.setCardmarket(cardMarketDTO);

        return dto;
    }

    private Card createCompleteCard() {
        Card card = new Card();
        card.setId("swsh4-25");
        card.setName("Charizard");
        card.setSupertype("Pokémon");
        card.setSubtypes(Arrays.asList("Stage 2"));
        card.setHp("170");
        card.setTypes(Arrays.asList("Fire"));
        card.setRarity("Rare");
        card.setCardNumber("25");
        card.setArtist("Ryuta Fuse");
        card.setSet("swsh4");
        card.setSetName("Vivid Voltage");
        card.setImageUrl("https://images.pokemontcg.io/swsh4/25.png");
        card.setImageUrlHiRes("https://images.pokemontcg.io/swsh4/25_hires.png");
        card.setMarketPrice(2.82);
        return card;
    }
}
