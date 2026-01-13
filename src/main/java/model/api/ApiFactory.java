package model.api;

import config.AppConfig;

public class ApiFactory {
    public ICardProvider getCardProvider(String gameType) {
        // Andranno aggiunti altri provider per diversi giochi in futuro
        // Va sostituito con uno switch-case quando si aggiungeranno altre tipologie di giochi
        if (gameType.equals(AppConfig.POKEMON_GAME)) {
            return new PokemonCardProvider();
        } else {
            return null;
        }
    }
}
