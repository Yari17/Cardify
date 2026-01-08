package model.domain.card;

import config.AppConfig;
import model.bean.CardBean;
import model.domain.card.adapter.ICardApiAdapter;
import model.domain.card.adapter.CardApiAdapterFactory;

import java.util.List;

public class CardProvider {

    public List<CardBean> search(String gameType, String query) {
        ICardApiAdapter<?> adapter = CardApiAdapterFactory.getAdapter(gameType);
        return adapter.search(query);
    }

    public List<CardBean> searchSet(String gameType, String setId) {
        ICardApiAdapter<?> adapter = CardApiAdapterFactory.getAdapter(gameType);
        return adapter.searchSet(setId);
    }

    public CardBean getCardByID(String gameType, String cardId) {
        ICardApiAdapter<?> adapter = CardApiAdapterFactory.getAdapter(gameType);
        return adapter.getCardById(cardId);
    }

    public List<CardBean> searchPokemon(String query) {
        return search(AppConfig.POKEMON_GAME, query);
    }

    public List<CardBean> searchPokemonSet(String setId) {
        return searchSet(AppConfig.POKEMON_GAME, setId);
    }

    public List<CardBean> searchMagic(String query) {
        return search(AppConfig.MAGIC_GAME, query);
    }

    public List<CardBean> searchMagicSet(String setId) {
        return searchSet(AppConfig.MAGIC_GAME, setId);
    }

    public CardBean getPokemonById(String cardId) {
        return getCardByID(AppConfig.POKEMON_GAME, cardId);
    }

    public CardBean getMagicById(String cardId) {
        return getCardByID(AppConfig.POKEMON_GAME, cardId);
    }
}
