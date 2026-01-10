package controller;

import model.bean.CardBean;
import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.card.Card;
import model.domain.card.CardProvider;
import view.collection.FXCollectionView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CollectionController {
    private static final Logger LOGGER = Logger.getLogger(CollectionController.class.getName());

    private final String username;
    private final Navigator navigator;
    private final IBinderDao binderDao;
    private final CardProvider cardProvider;
    private FXCollectionView view;

    public CollectionController(String username, Navigator navigator, IBinderDao binderDao) {
        this.username = username;
        this.navigator = navigator;
        this.binderDao = binderDao;
        this.cardProvider = new CardProvider();
    }

    public void setView(FXCollectionView view) {
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

            if (view != null) {
                view.displayCollection(bindersBySet, cardProvider);
            }

            LOGGER.info(() -> "Loaded collection with " + userBinders.size() + " sets for user: " + username);
        } catch (Exception e) {
            LOGGER.severe("Error loading collection: " + e.getMessage());
            if (view != null) {
                view.showError("Errore nel caricamento della collezione");
            }
        }
    }

    /**
     * Ottiene la lista dei set disponibili dal CardProvider
     */
    public Map<String, String> getAvailableSets() {
        try {
            return cardProvider.getPokemonSets();
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
     * Aggiunge una carta al set (o incrementa la quantità se già posseduta)
     */
    public void addCardToSet(String setId, Card card) {
        try {
            List<Binder> userBinders = binderDao.getUserBinders(username);
            Binder binder = userBinders.stream()
                    .filter(b -> b.getSetId().equals(setId))
                    .findFirst()
                    .orElse(null);

            if (binder == null) {
                LOGGER.warning("Set not found for setId: " + setId);
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
                    card.getGameType()
                );
                binder.addCard(cardBean);
                LOGGER.info(() -> "Added card " + card.getName() + " to set " + setId);
            }

            binderDao.update(binder, null);

            // Ricarica la collezione
            loadUserCollection();
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
            List<Binder> userBinders = binderDao.getUserBinders(username);
            Binder binder = userBinders.stream()
                    .filter(b -> b.getSetId().equals(setId))
                    .findFirst()
                    .orElse(null);

            if (binder == null) {
                LOGGER.warning("Set not found for setId: " + setId);
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

                binderDao.update(binder, null);
            }

            // Ricarica la collezione
            loadUserCollection();
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
            List<Binder> userBinders = binderDao.getUserBinders(username);
            Binder binder = userBinders.stream()
                    .filter(b -> b.getSetId().equals(setId))
                    .findFirst()
                    .orElse(null);

            if (binder == null) {
                LOGGER.warning("Set not found for setId: " + setId);
                return;
            }

            // Trova la carta e aggiorna il flag tradable
            CardBean cardToUpdate = binder.getCards().stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .orElse(null);

            if (cardToUpdate != null) {
                cardToUpdate.setTradable(tradable);
                binderDao.update(binder, null);

                LOGGER.info(() -> "Set card " + cardId + " as " + (tradable ? "tradable" : "not tradable"));
            }
        } catch (Exception e) {
            LOGGER.severe("Error toggling tradable: " + e.getMessage());
        }
    }

    public void navigateToHome() {
        LOGGER.info(() -> "Navigating to home page for user: " + username);
        if (view != null) {
            view.close();
        }
        navigator.navigateToCollectorHomePage(new model.bean.UserBean(username, "Collezionista"));
    }

    public void navigateToTrade() {
        LOGGER.info("Navigating to trade page");
        if (view != null) {
            view.close();
        }
    }

    public void onLogoutRequested() {
        LOGGER.info(() -> "User " + username + " logging out");
        if (view != null) {
            view.close();
        }
        navigator.logout();
    }
}
