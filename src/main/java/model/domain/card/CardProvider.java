package model.domain.card;

import config.AppConfig;
import model.domain.card.adapter.ICardApiAdapter;
import model.domain.card.adapter.CardApiAdapterFactory;

import java.util.List;
import java.util.Map;

public class CardProvider {

    public List<Card> searchSet(String gameType, String setId) {
        ICardApiAdapter<?> adapter = CardApiAdapterFactory.getAdapter(gameType);
        return adapter.searchSet(setId);
    }

    public List<Card> searchPokemonSet(String setId) {
        return searchSet(AppConfig.POKEMON_GAME, setId);
    }

    public Map<String,String> getPokemonSets() {
        ICardApiAdapter<?> adapter = CardApiAdapterFactory.getAdapter(AppConfig.POKEMON_GAME);
        return adapter.getAllSets();
    }

    /**
     * Cerca carte per nome utilizzando l'API del gioco specificato.
     *
     * @param gameType tipo di gioco (es: "pokemon", "magic", "yugioh")
     * @param name nome della carta da cercare
     * @return lista di carte che corrispondono al nome cercato
     */
    public List<Card> searchCardsByName(String gameType, String name) {
        ICardApiAdapter<?> adapter = CardApiAdapterFactory.getAdapter(gameType);
        return adapter.searchCardsByName(name);
    }

    /**
     * Cerca carte Pokemon per nome.
     *
     * @param name nome della carta da cercare
     * @return lista di carte Pokemon che corrispondono al nome cercato
     */
    public List<Card> searchPokemonCardsByName(String name) {
        return searchCardsByName(AppConfig.POKEMON_GAME, name);
    }

    /**
     * Ottiene i dettagli completi di una carta specifica.
     * Metodo generico scalabile per tutte le tipologie di carte (Pokemon, Magic, YuGiOh, etc.).
     *
     * @param gameType tipo di gioco (es: "pokemon", "magic", "yugioh")
     * @param cardId ID della carta
     * @param <T> tipo specifico di carta (PokemonCard, MagicCard, YuGiOhCard, etc.)
     * @return carta con tutti i dettagli, o null se non trovata
     */
    public <T extends Card> T getCardDetails(String gameType, String cardId) {
        ICardApiAdapter<?> adapter = CardApiAdapterFactory.getAdapter(gameType);
        return (T) adapter.getCardDetails(cardId);
    }

    /**
     * Ottiene i dettagli completi di una carta Pokemon.
     * Metodo di convenienza che usa il metodo generico getCardDetails.
     *
     * @param cardId ID della carta Pokemon
     * @return PokemonCard con tutti i dettagli, o null se non trovata
     */
    public PokemonCard getPokemonCardDetails(String cardId) {
        return getCardDetails(AppConfig.POKEMON_GAME, cardId);
    }

}
