package model.domain.card;

import model.domain.CardGameType;

public class CardFactory {

    private CardFactory() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    public static Card createCard(CardGameType gameType, String id, String name) {
        if (gameType == null) {
            throw new IllegalArgumentException("Game type cannot be null");
        }

        return switch (gameType) {
            case MAGIC -> new MagicCard(id, name);
            case POKEMON -> new PokemonCard(id, name);
            default -> throw new IllegalArgumentException("Unsupported game type: " + gameType);
        };
    }

    public static MagicCard createMagicCard(String id, String name) {
        return new MagicCard(id, name);
    }

    public static PokemonCard createPokemonCard(String id, String name) {
        return new PokemonCard(id, name);
    }
}

