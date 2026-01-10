package controller;

import model.bean.CardBean;
import model.bean.PokemonCardBean;
import model.domain.card.Card;
import model.domain.card.CardProvider;
import view.collectorhomepage.ICollectorHPView;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CollectorHPController {
    private static final Logger LOGGER = Logger.getLogger(CollectorHPController.class.getName());

    private final String username;
    private final Navigator navigator;
    private final CardProvider cardProvider;
    private ICollectorHPView view;

    public CollectorHPController(String username, Navigator navigator) {
        this.username = username;
        this.navigator = navigator;
        this.cardProvider = new CardProvider();
    }
    public void loadPopularCards() {
        loadCardsFromSet("sv08.5");
    }

    public void loadCardsFromSet(String setId) {
        try {
            List<Card> cards = cardProvider.searchPokemonSet(setId);
            LOGGER.info(() -> "Loaded " + cards.size() + " cards from set " + setId);

            if (view != null) {
                List<CardBean> cardBeans = cards.stream()
                    .map(Card::toBean)
                    .toList();
                view.displayCards(cardBeans);
            }
        } catch (Exception e) {
            LOGGER.severe(() -> "Error loading cards from set " + setId + ": " + e.getMessage());
        }
    }

    /**
     * Cerca carte Pokemon per nome e le visualizza nella view.
     *
     * @param name nome della carta da cercare
     */
    public void searchCardsByName(String name) {
        try {
            LOGGER.info(() -> "Searching for cards with name: " + name);
            List<Card> cards = cardProvider.searchPokemonCardsByName(name);
            LOGGER.info(() -> "Found " + cards.size() + " cards matching '" + name + "'");

            if (view != null) {
                List<CardBean> cardBeans = cards.stream()
                    .map(Card::toBean)
                    .toList();
                view.displayCards(cardBeans);
            }
        } catch (Exception e) {
            LOGGER.severe(() -> "Error searching cards by name '" + name + "': " + e.getMessage());
        }
    }

    public void loadAvailableSets() {
        LOGGER.info("loadAvailableSets() called");

        try {
            LOGGER.info("Fetching Pokemon sets from API...");
            Map<String, String> setsMap = cardProvider.getPokemonSets();

            LOGGER.info("Received map from API with " + (setsMap != null ? setsMap.size() + " sets" : "null"));

            if (setsMap != null && !setsMap.isEmpty()) {
                // Log primi 5 set per debug
                int count = 0;
                for (Map.Entry<String, String> entry : setsMap.entrySet()) {
                    if (count < 5) {
                        LOGGER.info("Set: " + entry.getKey() + " -> " + entry.getValue());
                        count++;
                    } else {
                        break;
                    }
                }

                LOGGER.info("Total sets in map: " + setsMap.size());

                if (view != null) {
                    LOGGER.info("Calling view.displayAvailableSets()");
                    view.displayAvailableSets(setsMap);
                } else {
                    LOGGER.warning("View is NULL - cannot display sets!");
                }
            } else {
                LOGGER.warning("Received empty or null map from API");
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading available sets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setView(ICollectorHPView view) {
        LOGGER.info("setView() called with view: " + (view != null ? view.getClass().getName() : "null"));
        this.view = view;

        // Carica i set disponibili DOPO che la view è stata impostata
        if (view != null) {
            LOGGER.info("Loading available sets after view is set");
            loadAvailableSets();
        }
    }

    public String getUsername() {
        return username;
    }

    public void navigateToCollection() {
        LOGGER.info(() -> "Navigating to collection page for user: " + username);
        if (view != null) {
            view.close();
        }
        navigator.navigateToCollection(username);
    }

    public void onLogoutRequested() {
        LOGGER.info(() -> "User " + username + " logging out");
        if (view != null) {
            view.close();
        }
        navigator.logout();
    }
    public void showCardDetails(CardBean card) {
        LOGGER.info(() -> "Opening card details for: " + card.getName() + " (ID: " + card.getId() + ")");

        if (view != null) {
            // Carica i dettagli completi della carta
            model.domain.card.PokemonCard detailedCard = cardProvider.getPokemonCardDetails(card.getId());

            if (detailedCard != null) {
                // Converti in bean con tutti i dettagli
                PokemonCardBean detailedBean = detailedCard.toBean();
                view.showCardOverview(detailedBean);
            } else {
                // Fallback: mostra il bean base se non si riescono a caricare i dettagli
                LOGGER.warning(() -> "Could not load detailed info for card: " + card.getId());
                view.showCardOverview(card);
            }
        }
    }

    public void onExitRequested() {
        if (view != null) {
            view.close();
        }
        System.exit(0);
    }
}
