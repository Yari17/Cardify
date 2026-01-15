package controller;

import config.AppConfig;
import model.api.ApiFactory;
import model.api.ICardProvider;
import model.bean.CardBean;
import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.Card;
import view.collection.ICollectionView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

//Controller applicativo per la gestione della collezione di carte di un utente
public class CollectionController {
    private static final Logger LOGGER = Logger.getLogger(CollectionController.class.getName());
    private static final String SET_NOT_FOUND_MSG = "Set not found for setId: ";

    private final String username;
    private final ApplicationController navigationController;
    private final IBinderDao binderDao;
    private final ApiFactory apiFactory;
    private ICollectionView view;

    // Cache locale dei binder dell'utente: setId -> Binder
    private Map<String, Binder> cachedBinders;

    // Mappa per tracciare i binder modificati: setId -> Binder
    private final Map<String, Binder> pendingChanges;
    private boolean hasUnsavedChanges;

    public CollectionController(String username, ApplicationController navigationController, IBinderDao binderDao) {
        this.username = username;
        this.navigationController = navigationController;
        this.binderDao = binderDao;
        this.apiFactory = new ApiFactory();
        this.cachedBinders = new HashMap<>();
        this.pendingChanges = new HashMap<>();
        this.hasUnsavedChanges = false;
    }

    public void setView(ICollectionView view) {
        this.view = view;
        if (view != null) {
            view.setWelcomeMessage(username);
            loadUserCollection();
        }
    }

    public String getUsername() {
        return username;
    }

    /**
     * Carica la collezione dell'utente organizzata per set
     */
    public void loadUserCollection() {
        try {
            List<Binder> userBinders = binderDao.getUserBinders(username);

            // Mappa: setId -> Binder
            Map<String, Binder> bindersBySet = new HashMap<>();
            for (Binder binder : userBinders) {
                bindersBySet.put(binder.getSetId(), binder);
            }

            // Update the cached binders
            this.cachedBinders = new HashMap<>(bindersBySet);

            // Prepare setId -> List<Card> map by fetching card details via provider for card ids in binders
            Map<String, List<Card>> setCardsMap = new HashMap<>();
            model.api.ICardProvider provider = null;
            try {
                provider = apiFactory.getCardProvider(AppConfig.POKEMON_GAME);
            } catch (Exception ex) {
                LOGGER.warning(() -> "Could not obtain ICardProvider: " + ex.getMessage());
            }

            for (String setId : bindersBySet.keySet()) {
                try {
                    // First try to fetch the entire set from the provider so we can
                    // show both owned and non-owned cards for that set.
                    List<Card> allSetCards = null;
                    if (provider != null) {
                        try {
                            allSetCards = provider.searchSet(setId);
                        } catch (Exception e) {
                            LOGGER.fine(() -> "Provider.searchSet failed for " + setId + ": " + e.getMessage());
                        }
                    }

                    // If the provider returned nothing, fall back to fetching only
                    // details for the card ids present in the binder (legacy behavior).
                    if (allSetCards == null || allSetCards.isEmpty()) {
                        allSetCards = new java.util.ArrayList<>();
                        Binder binder = bindersBySet.get(setId);
                        if (binder != null && binder.getCards() != null) {
                            for (CardBean cb : binder.getCards()) {
                                if (cb == null || cb.getId() == null) continue;
                                if (provider != null) {
                                    try {
                                        Card detail = provider.getCardDetails(cb.getId());
                                        if (detail != null) allSetCards.add(detail);
                                    } catch (Exception provEx) {
                                        LOGGER.fine(() -> "Provider failed to fetch details for " + cb.getId() + ": " + provEx.getMessage());
                                    }
                                }
                            }
                        }
                    }

                    setCardsMap.put(setId, allSetCards != null ? allSetCards : java.util.Collections.emptyList());
                } catch (Exception ex) {
                    LOGGER.warning(() -> "Failed to load provider cards for set " + setId + ": " + ex.getMessage());
                    setCardsMap.put(setId, java.util.Collections.emptyList());
                }
            }

            // Clear pending changes since we're loading fresh data
            pendingChanges.clear();
            hasUnsavedChanges = false;
            if (view != null) {
                view.setSaveButtonVisible(false);
                view.displayCollection(bindersBySet, setCardsMap);
            }

            LOGGER.info(() -> "Loaded collection with " + userBinders.size() + " sets for user: " + username);
        } catch (Exception e) {
            LOGGER.severe("Error loading collection: " + e.getMessage());
            if (view != null) {
                view.showError("Errore nel caricamento della collezione");
            }
        }
    }


    public Map<String, String> getAvailableSets() {
        try {
            ICardProvider provider = apiFactory.getCardProvider(AppConfig.POKEMON_GAME);
            if (provider != null) {
                return provider.getAllSets();
            } else {
                LOGGER.warning("No provider available for game: " + AppConfig.POKEMON_GAME);
                return Map.of();
            }
        } catch (Exception e) {
            LOGGER.severe("Error fetching available sets: " + e.getMessage());
            return Map.of();
        }
    }

    /**
     * Crea un nuovo set nella collezione
     */
    public void createBinder(String setId, String setName) {
        try {
            binderDao.createBinder(username, setId, setName);

            LOGGER.info(() -> "Created new set: " + setName + " for user: " + username);

            // Ricarica la collezione
            loadUserCollection();

            if (view != null) {
                view.showSuccess("Set \"" + setName + "\" aggiunto alla collezione!");
            }
        } catch (Exception e) {
            LOGGER.severe("Error creating set: " + e.getMessage());
            if (view != null) {
                view.showError("Errore nell'aggiunta del set");
            }
        }
    }

    /**
     * Elimina un binder dalla collezione
     */
    public void deleteBinder(String setId) {
        try {
            Binder binder = cachedBinders.get(setId);
            if (binder == null) {
                LOGGER.warning(() -> "Binder not found for setId: " + setId);
                return;
            }

            binderDao.deleteBinder(String.valueOf(binder.getId()));

            LOGGER.info(() -> "Deleted set: " + binder.getSetName() + " for user: " + username);

            // Ricarica la collezione
            loadUserCollection();

            if (view != null) {
                view.showSuccess("Set \"" + binder.getSetName() + "\" eliminato dalla collezione!");
            }
        } catch (Exception e) {
            LOGGER.severe("Error deleting set: " + e.getMessage());
            if (view != null) {
                view.showError("Errore nell'eliminazione del set");
            }
        }
    }

    /**
     * Aggiunge una carta al set (o incrementa la quantità se già posseduta)
     */
    public void addCardToSet(String setId, Card card) {
        try {
            // Use cached binder instead of reloading from database
            Binder binder = cachedBinders.get(setId);

            if (binder == null) {
                LOGGER.warning(() -> SET_NOT_FOUND_MSG + setId);
                return;
            }

            // Controlla se la carta esiste già
            CardBean existingCard = binder.getCards().stream()
                    .filter(c -> c.getId().equals(card.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingCard != null) {
                // Incrementa la quantità
                existingCard.setQuantity(existingCard.getQuantity() + 1);
                LOGGER.info(() -> "Increased quantity of " + card.getName() + " to " + existingCard.getQuantity());
            } else {
                // Aggiungi nuova carta
                CardBean cardBean = new CardBean(
                        card.getId(),
                        card.getName(),
                        card.getImageUrl(),
                        card.getGameType());
                binder.addCard(cardBean);
                LOGGER.info(() -> "Added card " + card.getName() + " to set " + setId);
            }

            // Track pending changes instead of persisting immediately
            pendingChanges.put(setId, binder);
            hasUnsavedChanges = true;
            if (view != null) {
                view.setSaveButtonVisible(true);
                // Update only the specific card in the UI
                view.updateCardInSet(setId, card.getId());
            }
        } catch (Exception e) {
            LOGGER.severe("Error adding card: " + e.getMessage());
            if (view != null) {
                view.showError("Errore nell'aggiunta della carta");
            }
        }
    }

    /**
     * Rimuove una carta dal set (o decrementa la quantità)
     */
    public void removeCardFromSet(String setId, Card card) {
        try {
            // Use cached binder instead of reloading from database
            Binder binder = cachedBinders.get(setId);

            if (binder == null) {
                LOGGER.warning(() -> SET_NOT_FOUND_MSG + setId);
                return;
            }

            // Trova la carta
            CardBean existingCard = binder.getCards().stream()
                    .filter(c -> c.getId().equals(card.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingCard != null) {
                if (existingCard.getQuantity() > 1) {
                    // Decrementa la quantità
                    existingCard.setQuantity(existingCard.getQuantity() - 1);
                    LOGGER.info(() -> "Decreased quantity of " + card.getName() + " to " + existingCard.getQuantity());
                } else {
                    // Rimuovi completamente la carta
                    binder.removeCard(card.getId());
                    LOGGER.info(() -> "Removed card " + card.getName() + " from set " + setId);
                }

                // Track pending changes instead of persisting immediately
                pendingChanges.put(setId, binder);
                hasUnsavedChanges = true;
                if (view != null) {
                    view.setSaveButtonVisible(true);
                    // Update only the specific card in the UI
                    view.updateCardInSet(setId, card.getId());
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error removing card: " + e.getMessage());
            if (view != null) {
                view.showError("Errore nella rimozione della carta");
            }
        }
    }

    /**
     * Toglie/imposta una carta come scambiabile
     */
    public void toggleCardTradable(String setId, String cardId, boolean tradable) {
        try {
            // Use cached binder instead of reloading from database
            Binder binder = cachedBinders.get(setId);

            if (binder == null) {
                LOGGER.warning(() -> SET_NOT_FOUND_MSG + setId);
                return;
            }

            // Trova la carta e aggiorna il flag tradable
            CardBean cardToUpdate = binder.getCards().stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .orElse(null);

            if (cardToUpdate != null) {
                cardToUpdate.setTradable(tradable);

                // Track pending changes instead of persisting immediately
                pendingChanges.put(setId, binder);
                hasUnsavedChanges = true;
                if (view != null) {
                    view.setSaveButtonVisible(true);
                    // DO NOT update UI for tradable toggle - it's already updated by the checkbox
                }

                LOGGER.info(() -> "Set card " + cardId + " as " + (tradable ? "tradable" : "not tradable"));
            }
        } catch (Exception e) {
            LOGGER.severe("Error toggling tradable: " + e.getMessage());
        }
    }

    /**
     * Salva tutte le modifiche pendenti in persistenza
     */
    public void saveChanges() {
        if (!hasUnsavedChanges || pendingChanges.isEmpty()) {
            LOGGER.info("No changes to save");
            return;
        }

        try {
            int savedCount = 0;
            for (Map.Entry<String, Binder> entry : pendingChanges.entrySet()) {
                Binder binder = entry.getValue();
                binderDao.update(binder, null);
                savedCount++;
                LOGGER.info(() -> "Saved changes for set: " + binder.getSetName());
            }

            final int finalSavedCount = savedCount;

            // Clear pending changes after successful save
            pendingChanges.clear();
            hasUnsavedChanges = false;

            if (view != null) {
                view.setSaveButtonVisible(false);
                view.showSuccess("Salvate " + finalSavedCount + " modifiche con successo!");
            }

            LOGGER.info(() -> "Successfully saved " + finalSavedCount + " binder changes");

            // Reload the collection to refresh the UI with saved data
            loadUserCollection();
        } catch (Exception e) {
            LOGGER.severe("Error saving changes: " + e.getMessage());
            if (view != null) {
                view.showError("Errore nel salvataggio delle modifiche");
            }
        }
    }

    public void navigateToHome() {
        LOGGER.info(() -> "Navigating to home page for user: " + username);
        if (view != null) {
            view.close();
        }
        navigationController
                .navigateToCollectorHomePage(new model.bean.UserBean(username, config.AppConfig.USER_TYPE_COLLECTOR));
    }

    public void navigateToTrade() {
        LOGGER.info("Navigating to trade page");
        if (view != null) {
            view.close();
        }
        try {
            navigationController.navigateToLiveTrades(username);
        } catch (exception.NavigationException e) {
            LOGGER.severe(() -> "Failed to navigate to Manage Trades: " + e.getMessage());
            if (view != null) {
                view.showError("Impossibile aprire la sezione Manage Trades");
            }
        }
    }

    public void onLogoutRequested() {
        LOGGER.info(() -> "User " + username + " logging out");
        if (view != null) {
            view.close();
        }
        navigationController.logout();
    }
}
