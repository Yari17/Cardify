package controller;

import model.bean.CardBean;
import model.bean.PokemonCardBean;
import config.AppConfig;
import model.domain.card.Card;
import view.collectorhomepage.ICollectorHPView;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CollectorHPController {
    private static final Logger LOGGER = Logger.getLogger(CollectorHPController.class.getName());

    private final String username;
    private final ApplicationController navigationController;
    private final model.dao.ICardDao cardDao;
    private ICollectorHPView view;

    public CollectorHPController(String username, ApplicationController navigationController,
            model.dao.ICardDao cardDao) {
        this.username = username;
        this.navigationController = navigationController;
        this.cardDao = cardDao;
    }

    public void loadPopularCards() {
        loadCardsFromSet(config.AppConfig.DEFAULT_SET_ID);
    }

    public void loadCardsFromSet(String setId) {
        try {
            List<Card> cards = cardDao.getSetCards(setId, AppConfig.POKEMON_GAME);
            LOGGER.log(java.util.logging.Level.INFO, "Loaded {0} cards from set {1}",
                    new Object[] { cards.size(), setId });

            if (view != null) {
                List<CardBean> cardBeans = cards.stream()
                        .map(Card::toBean)
                        .toList();
                view.displayCards(cardBeans);
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error loading cards from set {0}: {1}",
                    new Object[] { setId, e.getMessage() });
        }
    }

    /**
     * Cerca carte Pokemon per nome e le visualizza nella view.
     *
     * @param name nome della carta da cercare
     */
    public void searchCardsByName(String name) {
        try {
            LOGGER.log(java.util.logging.Level.INFO, "Searching for cards with name: {0}", name);
            List<Card> cards = cardDao.searchCards(name, AppConfig.POKEMON_GAME);
            LOGGER.log(java.util.logging.Level.INFO, "Found {0} cards matching ''{1}''",
                    new Object[] { cards.size(), name });

            if (view != null) {
                List<CardBean> cardBeans = cards.stream()
                        .map(Card::toBean)
                        .toList();
                view.displayCards(cardBeans);
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error searching cards by name ''{0}'': {1}",
                    new Object[] { name, e.getMessage() });
        }
    }

    public void loadAvailableSets() {
        LOGGER.info("loadAvailableSets() called");

        try {
            LOGGER.info("Fetching Pokemon sets from API...");
            Map<String, String> setsMap = cardDao.getAllSets(AppConfig.POKEMON_GAME);

            LOGGER.log(java.util.logging.Level.INFO, "Received map from API with {0} sets",
                    (setsMap != null ? setsMap.size() : "null"));

            if (setsMap != null && !setsMap.isEmpty()) {
                // Log primi 5 set per debug
                int count = 0;
                for (Map.Entry<String, String> entry : setsMap.entrySet()) {
                    if (count < 5) {
                        LOGGER.log(java.util.logging.Level.INFO, "Set: {0} -> {1}",
                                new Object[] { entry.getKey(), entry.getValue() });
                        count++;
                    } else {
                        break;
                    }
                }

                LOGGER.log(java.util.logging.Level.INFO, "Total sets in map: {0}", setsMap.size());

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
            LOGGER.log(java.util.logging.Level.SEVERE, "Error loading available sets: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void setView(ICollectorHPView view) {
        LOGGER.log(java.util.logging.Level.INFO, "setView() called with view: {0}",
                (view != null ? view.getClass().getName() : "null"));
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
        LOGGER.log(java.util.logging.Level.INFO, "Navigating to collection page for user: {0}", username);
        if (view != null) {
            view.close();
        }
        navigationController.navigateToCollection(username);
    }

    public void navigateToTrade() {
        LOGGER.log(java.util.logging.Level.INFO, "Navigating to trade page for user: {0}", username);
        if (view != null) {
            view.close();
        }
        navigationController.navigateToTrade(username);
    }

    public void onLogoutRequested() {
        LOGGER.log(java.util.logging.Level.INFO, "User {0} logging out", username);
        if (view != null) {
            view.close();
        }
        navigationController.logout();
    }

    public void showCardDetails(CardBean card) {
        LOGGER.log(java.util.logging.Level.INFO, "Opening card details for: {0} (ID: {1})",
                new Object[] { card.getName(), card.getId() });

        if (view != null) {
            // Carica i dettagli completi della carta
            model.domain.card.Card detailedCard = cardDao.getPokemonCard(card.getId());

            if (detailedCard != null) {
                // Converti in bean con tutti i dettagli
                PokemonCardBean detailedBean = (PokemonCardBean) detailedCard.toBean();
                // cast safe if getPokemonCard returns PokemonCard domain object which returns
                // PokemonCardBean
                // Actually internal implementation returns Card, but getPokemonCard returns
                // PokemonCard.
                view.showCardOverview(detailedBean);
            } else {
                // Fallback: mostra il bean base se non si riescono a caricare i dettagli
                LOGGER.log(java.util.logging.Level.WARNING, "Could not load detailed info for card: {0}", card.getId());
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
