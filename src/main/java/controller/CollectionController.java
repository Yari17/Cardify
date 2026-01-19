package controller;

import config.AppConfig;
import model.api.ApiFactory;
import model.api.ICardProvider;
import model.bean.CardBean;
import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.Card;
import view.ICollectionView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class CollectionController {
    private static final Logger LOGGER = Logger.getLogger(CollectionController.class.getName());
    private static final String SET_NOT_FOUND_MSG = "Set not found for setId: ";

    private final String username;
    private final ApplicationController navigationController;
    private final IBinderDao binderDao;
    private final ApiFactory apiFactory;
    private ICollectionView view;

    
    private Map<String, Binder> cachedBinders;

    
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

    
    public void loadUserCollection() {
        try {
            List<Binder> userBinders = binderDao.getUserBinders(username);

            
            Map<String, Binder> bindersBySet = new HashMap<>();
            for (Binder binder : userBinders) {
                bindersBySet.put(binder.getSetId(), binder);
            }

            
            this.cachedBinders = new HashMap<>(bindersBySet);

            
            Map<String, List<Card>> setCardsMap = new HashMap<>();
            ICardProvider provider = getCardProviderSafe();

            
            for (Map.Entry<String, Binder> entry : bindersBySet.entrySet()) {
                String setId = entry.getKey();
                Binder binder = entry.getValue();
                List<Card> cards = fetchCardsForSet(setId, binder, provider);
                setCardsMap.put(setId, cards);
            }

            
            pendingChanges.clear();
            hasUnsavedChanges = false;
            if (view != null) {
                view.setSaveButtonVisible(false);
                view.displayCollection(bindersBySet, setCardsMap);
            }

            LOGGER.info(() -> "Loaded collection with " + userBinders.size() + " sets for user: " + username);
        } catch (Exception ex) {
            LOGGER.severe("Error loading collection: " + ex.getMessage());
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
        } catch (Exception ex) {
            LOGGER.severe("Error fetching available sets: " + ex.getMessage());
            return Map.of();
        }
    }

    
    public void createBinder(String setId, String setName) {
        try {
            binderDao.createBinder(username, setId, setName);

            LOGGER.info(() -> "Created new set: " + setName + " for user: " + username);

            
            loadUserCollection();

            if (view != null) {
                view.showSuccess("Set \"" + setName + "\" aggiunto alla collezione!");
            }
        } catch (Exception ex) {
            LOGGER.severe("Error creating set: " + ex.getMessage());
            if (view != null) {
                view.showError("Errore nell'aggiunta del set");
            }
        }
    }

    
    public void deleteBinder(String setId) {
        try {
            Binder binder = cachedBinders.get(setId);
            if (binder == null) {
                LOGGER.warning(() -> "Binder not found for setId: " + setId);
                return;
            }

            binderDao.deleteBinder(String.valueOf(binder.getId()));

            LOGGER.info(() -> "Deleted set: " + binder.getSetName() + " for user: " + username);

            
            loadUserCollection();

            if (view != null) {
                view.showSuccess("Set \"" + binder.getSetName() + "\" eliminato dalla collezione!");
            }
        } catch (Exception ex) {
            LOGGER.severe("Error deleting set: " + ex.getMessage());
            if (view != null) {
                view.showError("Errore nell'eliminazione del set");
            }
        }
    }

    
    public void addCardToSet(String setId, Card card) {
        try {
            
            Binder binder = cachedBinders.get(setId);

            if (binder == null) {
                LOGGER.warning(() -> SET_NOT_FOUND_MSG + setId);
                return;
            }

            
            CardBean existingCard = binder.getCards().stream()
                    .filter(c -> c.getId().equals(card.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingCard != null) {
                
                existingCard.setQuantity(existingCard.getQuantity() + 1);
                LOGGER.info(() -> "Increased quantity of " + card.getName() + " to " + existingCard.getQuantity());
            } else {
                
                CardBean cardBean = new CardBean(
                        card.getId(),
                        card.getName(),
                        card.getImageUrl(),
                        card.getGameType());
                binder.addCard(cardBean);
                LOGGER.info(() -> "Added card " + card.getName() + " to set " + setId);
            }

            
            pendingChanges.put(setId, binder);
            hasUnsavedChanges = true;
            if (view != null) {
                view.setSaveButtonVisible(true);
                
                view.updateCardInSet(setId, card.getId());
            }
        } catch (Exception ex) {
            LOGGER.severe("Error adding card: " + ex.getMessage());
            if (view != null) {
                view.showError("Errore nell'aggiunta della carta");
            }
        }
    }

    
    public void removeCardFromSet(String setId, Card card) {
        try {
            
            Binder binder = cachedBinders.get(setId);

            if (binder == null) {
                LOGGER.warning(() -> SET_NOT_FOUND_MSG + setId);
                return;
            }

            
            CardBean existingCard = binder.getCards().stream()
                    .filter(c -> c.getId().equals(card.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingCard != null) {
                if (existingCard.getQuantity() > 1) {
                    
                    existingCard.setQuantity(existingCard.getQuantity() - 1);
                    LOGGER.info(() -> "Decreased quantity of " + card.getName() + " to " + existingCard.getQuantity());
                } else {
                    
                    binder.removeCard(card.getId());
                    LOGGER.info(() -> "Removed card " + card.getName() + " from set " + setId);
                }

                
                pendingChanges.put(setId, binder);
                hasUnsavedChanges = true;
                if (view != null) {
                    view.setSaveButtonVisible(true);
                    
                    view.updateCardInSet(setId, card.getId());
                }
            }
        } catch (Exception ex) {
            LOGGER.severe("Error removing card: " + ex.getMessage());
            if (view != null) {
                view.showError("Errore nella rimozione della carta");
            }
        }
    }

    
    public void toggleCardTradable(String setId, String cardId, boolean tradable) {
        try {
            
            Binder binder = cachedBinders.get(setId);

            if (binder == null) {
                LOGGER.warning(() -> SET_NOT_FOUND_MSG + setId);
                return;
            }

            
            CardBean cardToUpdate = binder.getCards().stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .orElse(null);

            if (cardToUpdate != null) {
                cardToUpdate.setTradable(tradable);

                
                pendingChanges.put(setId, binder);
                hasUnsavedChanges = true;
                if (view != null) {
                    view.setSaveButtonVisible(true);
                    
                }

                LOGGER.info(() -> "Set card " + cardId + " as " + (tradable ? "tradable" : "not tradable"));
            }
        } catch (Exception ex) {
            LOGGER.severe("Error toggling tradable: " + ex.getMessage());
        }
    }

    
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

            
            pendingChanges.clear();
            hasUnsavedChanges = false;

            if (view != null) {
                view.setSaveButtonVisible(false);
                view.showSuccess("Salvate " + finalSavedCount + " modifiche con successo!");
            }

            LOGGER.info(() -> "Successfully saved " + finalSavedCount + " binder changes");

            
            loadUserCollection();
        } catch (Exception ex) {
            LOGGER.severe("Error saving changes: " + ex.getMessage());
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

    public void navigateToManageTrade() {
        LOGGER.log(java.util.logging.Level.INFO, "Navigating to Manage Trades for user: {0}", username);
        if (view != null) {
            view.close();
        }
        try {
            navigationController.navigateToManageTrade(username);
        } catch (exception.NavigationException ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to navigate to Manage Trades: {0}", ex.getMessage());
            if (view != null) view.showError("Impossibile aprire la sezione Manage Trades");
        }
    }

    public void onLogoutRequested() {
        LOGGER.info(() -> "User " + username + " logging out");
        if (view != null) {
            view.close();
        }
        navigationController.logout();
    }

    private ICardProvider getCardProviderSafe() {
        try {
            return apiFactory.getCardProvider(AppConfig.POKEMON_GAME);
        } catch (Exception ex) {
            LOGGER.warning(() -> "Could not obtain ICardProvider: " + ex.getMessage());
            return null;
        }
    }

    private List<Card> fetchCardsForSet(String setId, Binder binder, ICardProvider provider) {
        
        List<Card> fromSearch = trySearchSet(setId, provider);
        if (!fromSearch.isEmpty()) return fromSearch;
        return fetchDetailsFromBinder(binder, provider);
    }

    private List<Card> trySearchSet(String setId, ICardProvider provider) {
        if (provider == null) return java.util.Collections.emptyList();
        try {
            List<Card> res = provider.searchSet(setId);
            return res != null ? res : java.util.Collections.emptyList();
        } catch (Exception ex) {
            LOGGER.fine(() -> "Provider.searchSet failed for " + setId + ": " + ex.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    private List<Card> fetchDetailsFromBinder(Binder binder, ICardProvider provider) {
        List<Card> details = new java.util.ArrayList<>();
        if (binder == null || binder.getCards() == null || provider == null) return details;
        for (CardBean cb : binder.getCards()) {
            if (cb == null || cb.getId() == null) continue;
            try {
                Card detail = provider.getCardDetails(cb.getId());
                if (detail != null) details.add(detail);
            } catch (Exception provEx) {
                LOGGER.fine(() -> "Provider failed to fetch details for " + cb.getId() + ": " + provEx.getMessage());
            }
        }
        return details;
    }
}
