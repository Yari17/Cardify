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

}
