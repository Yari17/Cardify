package controller;

import config.AppConfig;
import model.bean.CardBean;
import model.domain.card.CardProvider;
import view.collectorhomepage.ICollectorHPView;

import java.util.List;
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

    private void loadInitialCards() {
        try {
            List<CardBean> cards = cardProvider.searchPokemonSet("sv08.5");
            LOGGER.info(() -> "Loaded " + cards.size() + " cards from Pokemon set sv08.5");

            for (CardBean card : cards) {
                LOGGER.info(() -> "Card Details - ID: " + card.getId() +
                    ", Name: " + card.getName() +
                    ", ImageURL: " + card.getImageUrl() +
                    ", GameType: " + card.getGameType());
            }

            if (view != null) {
                view.displayCards(cards);
            }
        } catch (Exception e) {
            LOGGER.severe(() -> "Error loading initial cards: " + e.getMessage());
        }
    }

    public void setView(ICollectorHPView view) {
        this.view = view;
        view.showWelcomeMessage(username);
        loadInitialCards();
    }

    public String getUsername() {
        return username;
    }

    public void onLogoutRequested() {
        LOGGER.info(() -> "User " + username + " logging out");
        if (view != null) {
            view.close();
        }
        navigator.logout();
    }

    public void onExitRequested() {
        if (view != null) {
            view.close();
        }
        System.exit(0);
    }
}
