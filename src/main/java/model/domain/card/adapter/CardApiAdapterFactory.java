package model.domain.card.adapter;

import config.AppConfig;

import java.util.HashMap;
import java.util.Map;

public class CardApiAdapterFactory {
    private static final Map<String, ICardApiAdapter> adapters = new HashMap<>();

    // Costruttore privato per utility class
    private CardApiAdapterFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        adapters.put(AppConfig.POKEMON_GAME, new PokemonAdapter());
    }

    public static ICardApiAdapter getAdapter(String gameType) {
        ICardApiAdapter adapter = adapters.get(gameType.toUpperCase());
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported game: " + gameType);
        }
        return adapter;
    }
}

