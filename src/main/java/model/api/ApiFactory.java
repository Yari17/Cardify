package model.api;

import model.domain.enumerations.CardGameType;

public class ApiFactory {
    public ICardProvider getCardProvider(CardGameType gameType) {
        if (gameType.equals(CardGameType.POKEMON)) {
            return new PokemonCardProvider();
        } else {
            throw new UnsupportedOperationException("Game type not supported");
        }
    }
}
