package model.api;

import model.api.dto.MagicCardDTO;
import model.api.dto.PokemonCardDTO;
import model.domain.CardGameType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardAdapterFactoryTest {

    @Test
    void testCreateAdapter_Pokemon_ReturnsPokemonAdapter() {
        
        ICardAdapter<?> adapter = CardAdapterFactory.createAdapter(CardGameType.POKEMON);

        
        assertNotNull(adapter);
        assertTrue(adapter instanceof PokemonCardAdapter);
    }

    @Test
    void testCreateAdapter_Magic_ReturnsMagicAdapter() {
        
        ICardAdapter<?> adapter = CardAdapterFactory.createAdapter(CardGameType.MAGIC);

        
        assertNotNull(adapter);
        assertTrue(adapter instanceof MagicCardAdapter);
    }

    @Test
    void testCreateAdapter_YuGiOh_ThrowsUnsupportedOperationException() {
        
        assertThrows(UnsupportedOperationException.class, () ->
            CardAdapterFactory.createAdapter(CardGameType.YUGIOH)
        );
    }

    @Test
    void testCreateAdapter_Null_ReturnsNull() {
        
        ICardAdapter<?> adapter = CardAdapterFactory.createAdapter(null);

        
        assertNull(adapter);
    }

    @Test
    void testCreatePokemonAdapter_ReturnsCorrectType() {
        
        ICardAdapter<PokemonCardDTO> adapter = CardAdapterFactory.createPokemonAdapter();

        
        assertNotNull(adapter);
        assertTrue(adapter instanceof PokemonCardAdapter);
    }

    @Test
    void testCreateMagicAdapter_ReturnsCorrectType() {
        
        ICardAdapter<MagicCardDTO> adapter = CardAdapterFactory.createMagicAdapter();

        
        assertNotNull(adapter);
        assertTrue(adapter instanceof MagicCardAdapter);
    }

    @Test
    void testGetAdapterTypeName_Pokemon_ReturnsCorrectName() {
        
        String name = CardAdapterFactory.getAdapterTypeName(CardGameType.POKEMON);

        
        assertEquals("Pokemon TCG Adapter", name);
    }

    @Test
    void testGetAdapterTypeName_Magic_ReturnsCorrectName() {
        
        String name = CardAdapterFactory.getAdapterTypeName(CardGameType.MAGIC);

        
        assertEquals("Magic: The Gathering Adapter", name);
    }

    @Test
    void testGetAdapterTypeName_Null_ReturnsUnknown() {
        
        String name = CardAdapterFactory.getAdapterTypeName(null);

        
        assertEquals("Unknown", name);
    }

    @Test
    void testConstructor_ThrowsException() {
        
        assertThrows(IllegalStateException.class, () -> {
            
            java.lang.reflect.Constructor<CardAdapterFactory> constructor =
                CardAdapterFactory.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testPolymorphism_BothAdaptersImplementSameInterface() {
        
        ICardAdapter<?> pokemonAdapter = CardAdapterFactory.createPokemonAdapter();
        ICardAdapter<?> magicAdapter = CardAdapterFactory.createMagicAdapter();

        
        assertNotNull(pokemonAdapter);
        assertNotNull(magicAdapter);
        assertTrue(pokemonAdapter instanceof ICardAdapter);
        assertTrue(magicAdapter instanceof ICardAdapter);
    }
}
