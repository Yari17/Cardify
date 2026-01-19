package model.api;

import config.AppConfig;

public class ApiFactory {
    public ICardProvider getCardProvider(String gameType) {
        
        
        if (gameType.equals(AppConfig.POKEMON_GAME)) {
            return new PokemonCardProvider();
        } else {
            return null;
        }
    }
}
