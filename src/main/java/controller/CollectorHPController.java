package controller;

import model.bean.CardBean;
import model.domain.Card;
import view.collectorhomepage.ICollectorHPView;

import java.util.*;
import java.util.logging.Logger;

import model.api.ApiFactory;
import model.api.ICardProvider;
import config.AppConfig;

//Controller applicativo della homepage del collezionista.
public class CollectorHPController {
    private static final Logger LOGGER = Logger.getLogger(CollectorHPController.class.getName());
    private static final String EXCEPTION_DETAILS = "Exception details:";

    private final String username;
    private final ApplicationController navigationController;
    private final model.dao.IBinderDao binderDao;
    private final ICardProvider cardProvider;
    private ICollectorHPView view;

    // Lightweight in-memory cache for fetched card details (not persisted)
    private final Map<String, Card> localCardCache = new HashMap<>();

    public CollectorHPController(String username, ApplicationController navigationController,
            model.dao.IBinderDao binderDao) {
        this.username = username;
        this.navigationController = navigationController;
        this.binderDao = binderDao;
        // Obtain provider polymorphically via ApiFactory so controller doesn't depend on concrete provider
        ICardProvider provider = new ApiFactory().getCardProvider(AppConfig.POKEMON_GAME);
        if (provider == null) {
            LOGGER.warning("ApiFactory returned null provider for game: " + AppConfig.POKEMON_GAME + "; using no-op fallback provider to avoid NPE.");
            provider = new ICardProvider() {
                @Override
                public java.util.List<Card> searchSet(String setId) { return java.util.Collections.emptyList(); }
                @Override
                public java.util.List<Card> searchCardsByName(String cardName) { return java.util.Collections.emptyList(); }
                @Override
                public <T extends Card> T getCardDetails(String cardId) { return null; }
                @Override
                public java.util.Map<String, String> getAllSets() { return java.util.Collections.emptyMap(); }
            };
        }
        this.cardProvider = provider;
    }

    public void loadPopularCards() {
        // Load popular cards across all other users' binders, not only the default set
        loadCardsFromOtherUsersAllSets();
    }

    /**
     * Load tradable cards from all other users, ignoring set filter.
     * This is used for the "Popular Cards" shortcut so the UI shows other collectors' tradable cards
     * even when the DEFAULT_SET_ID has no tradable cards among other users.
     */
    private void loadCardsFromOtherUsersAllSets() {
        try {
            List<model.domain.Binder> otherBinders = binderDao.getBindersExcludingOwner(username);

            LOGGER.log(java.util.logging.Level.INFO, "(Popular) otherBinders count: {0}", otherBinders.size());

            // Build maps from binder data
            Map<String, model.bean.CardBean> binderMap = new LinkedHashMap<>();
            Map<String, String> ownerMap = new LinkedHashMap<>();
            for (model.domain.Binder b : otherBinders) {
                for (model.bean.CardBean cb : b.getCards()) {
                    if (cb != null && cb.isTradable() && cb.getId() != null) {
                        binderMap.putIfAbsent(cb.getId(), cb);
                        ownerMap.putIfAbsent(cb.getId(), b.getOwner());
                    }
                }
            }

            LOGGER.log(java.util.logging.Level.INFO, "(Popular) unique tradable beans count: {0}", binderMap.size());

            List<CardBean> cardBeans = buildCardBeansFromBinderMap(binderMap, ownerMap);

            // Shuffle popular cards to present them in random order
            java.util.Collections.shuffle(cardBeans, new java.util.Random());

            LOGGER.log(java.util.logging.Level.INFO, "(Popular) Loaded {0} cards to display", cardBeans.size());

            if (view != null) {
                view.displayCards(cardBeans);
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error loading popular cards: {0}", e.getMessage());
            LOGGER.log(java.util.logging.Level.SEVERE, EXCEPTION_DETAILS, e);
        }
    }

    /**
     * Load cards for a set, but only those owned by other users (and marked tradable).
     * Fetches persistence data from Binder DAO, then uses ICardProvider to obtain
     * details required for the UI. This keeps responsibilities separated (GRASP).
     */
    public void loadCardsFromSet(String setId) {
        try {
            // Get binders owned by other users
            List<model.domain.Binder> otherBinders = binderDao.getBindersExcludingOwner(username);

            LOGGER.log(java.util.logging.Level.INFO, "otherBinders count: {0}", otherBinders.size());

            // Filter binders for the requested set
            List<model.domain.Binder> setBinders = new ArrayList<>();
            for (model.domain.Binder b : otherBinders) {
                if (setId != null && setId.equals(b.getSetId())) {
                    setBinders.add(b);
                }
            }

            LOGGER.log(java.util.logging.Level.INFO, "setBinders count for set {0}: {1}", new Object[]{setId, setBinders.size()});

            // Build maps from binder data
            Map<String, model.bean.CardBean> binderMap = new LinkedHashMap<>();
            Map<String, String> ownerMap = new LinkedHashMap<>();
            for (model.domain.Binder b : setBinders) {
                for (model.bean.CardBean cb : b.getCards()) {
                    if (cb != null && cb.isTradable() && cb.getId() != null) {
                        binderMap.putIfAbsent(cb.getId(), cb);
                        ownerMap.putIfAbsent(cb.getId(), b.getOwner());
                    }
                }
            }

            LOGGER.log(java.util.logging.Level.INFO, "unique tradable beans count: {0}", binderMap.size());

            List<CardBean> cardBeans = buildCardBeansFromBinderMap(binderMap, ownerMap);

            LOGGER.log(java.util.logging.Level.INFO, "Loaded {0} cards for set {1} to display",
                    new Object[]{cardBeans.size(), setId});

            if (view != null) {
                view.displayCards(cardBeans);
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error loading cards from set {0}: {1}",
                    new Object[]{setId, e.getMessage()});
            LOGGER.log(java.util.logging.Level.SEVERE, EXCEPTION_DETAILS, e);
        }
    }

    private Map<String, String> getTradableIdToOwnerMap() {
        List<model.domain.Binder> otherBinders = binderDao.getBindersExcludingOwner(username);
        return collectIdToOwnerMapFromBinders(otherBinders);
    }

    private List<Card> searchProviderCardsByName(String name) {
        try {
            return cardProvider.searchCardsByName(name);
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Provider search failed for name: {0} -> {1}", new Object[]{name, e.getMessage()});
            return Collections.emptyList();
        }
    }

    /**
     * Cerca carte Pokemon per nome e le visualizza nella view.
     * Mostra solamente carte possedute da altri utenti e contrassegnate come tradable.
     *
     * @param name nome della carta da cercare
     */
    public void searchCardsByName(String name) {
        // thin orchestrator: validate input and delegate to helper
        if (name == null || name.trim().isEmpty()) {
            LOGGER.log(java.util.logging.Level.INFO, "Empty search query");
            if (view != null) view.displayCards(java.util.Collections.emptyList());
            return;
        }

        List<CardBean> cardBeans = performSearchByName(name);
        if (view != null) {
            view.displayCards(cardBeans);
        }
    }

    private List<CardBean> performSearchByName(String name) {
        try {
            LOGGER.log(java.util.logging.Level.INFO, "Searching for cards with name: {0}", name);

            Map<String, String> idToOwner = getTradableIdToOwnerMap();
            LOGGER.log(java.util.logging.Level.INFO, "Found {0} tradable ids among other users", idToOwner.size());

            if (idToOwner.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            List<Card> matches = searchProviderCardsByName(name);
            LOGGER.log(java.util.logging.Level.INFO, "Provider returned {0} matches for name {1}", new Object[]{matches.size(), name});

            List<CardBean> cardBeans = buildCardBeansFromMatches(matches, idToOwner);
            LOGGER.log(java.util.logging.Level.INFO, "Filtered and loaded {0} cards to display for search", cardBeans.size());
            return cardBeans;
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error searching cards by name ''{0}'': {1}",
                    new Object[]{name, e.getMessage()});
            LOGGER.log(java.util.logging.Level.SEVERE, EXCEPTION_DETAILS, e);
            return java.util.Collections.emptyList();
        }
    }

    public void loadAvailableSets() {
        LOGGER.info("loadAvailableSets() called");

        try {
            LOGGER.info("Fetching Pokemon sets from ICardProvider...");
            Map<String, String> setsMap = cardProvider.getAllSets();

            LOGGER.log(java.util.logging.Level.INFO, "Received map from ICardProvider with {0} sets",
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
                LOGGER.warning("Received empty or null map from ICardProvider");
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error loading available sets: {0}", e.getMessage());
            LOGGER.log(java.util.logging.Level.SEVERE, EXCEPTION_DETAILS, e);
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
            // Try local cache first
            Card detailedCard = localCardCache.get(card.getId());
            if (detailedCard == null) {
                LOGGER.log(java.util.logging.Level.INFO, "Card not in local cache, fetching from API: {0}", card.getId());
                detailedCard = cardProvider.getCardDetails(card.getId());
                if (detailedCard != null) {
                    localCardCache.put(detailedCard.getId(), detailedCard);
                }
            }

            if (detailedCard != null) {
                CardBean detailedBean = detailedCard.toBean();
                view.showCardOverview(detailedBean);
            } else {
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

    private void addIfTradableToMap(LinkedHashMap<String, String> result, model.bean.CardBean cb, String owner) {
        if (cb == null) return;
        if (!cb.isTradable()) return;
        String id = cb.getId();
        if (id == null) return;
        result.putIfAbsent(id, owner);
    }

    private Map<String, String> collectIdToOwnerMapFromBinders(List<model.domain.Binder> binders) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        if (binders == null || binders.isEmpty()) return result;

        for (model.domain.Binder b : binders) {
            // Single continue for any invalid binder or empty cards
            if (b == null || b.getCards() == null || b.getCards().isEmpty()) continue;
            List<model.bean.CardBean> cards = b.getCards();
            String owner = b.getOwner();
            for (model.bean.CardBean cb : cards) {
                addIfTradableToMap(result, cb, owner);
            }
        }
        return result;
    }

    private CardBean buildFinalCardBean(String id, CardBean binderBean) {
        if (id == null) return null;

        // Prefer binder-provided bean if it already has display info
        if (binderBean != null && binderBean.getName() != null && binderBean.getImageUrl() != null) {
            return new CardBean(binderBean); // copy
        }

        // Try local cache
        Card cached = localCardCache.get(id);
        if (cached != null) {
            return cached.toBean();
        }

        // Try provider
        try {
            Card detailed = cardProvider.getCardDetails(id);
            if (detailed != null) {
                localCardCache.put(id, detailed);
                return detailed.toBean();
            }
        } catch (Exception e) {
            LOGGER.fine(() -> "Provider failed to fetch details for " + id + ": " + e.getMessage());
        }

        // Fallbacks
        if (binderBean != null) {
            return new CardBean(binderBean); // copy even if partial
        }

        CardBean minimal = new CardBean();
        minimal.setId(id);
        return minimal;
    }

    private List<CardBean> buildCardBeansFromBinderMap(Map<String, CardBean> binderMap, Map<String, String> idToOwnerMap) {
        List<CardBean> result = new ArrayList<>();
        if (binderMap == null || binderMap.isEmpty()) return result;

        for (Map.Entry<String, CardBean> e : binderMap.entrySet()) {
            String id = e.getKey();
            CardBean binderBean = e.getValue();
            String owner = idToOwnerMap != null ? idToOwnerMap.get(id) : null;

            CardBean finalBean = buildFinalCardBean(id, binderBean);
            if (finalBean == null) continue;

            // set owner for display
            finalBean.setOwner(owner);
            result.add(finalBean);
        }
        return result;
    }

    private List<CardBean> buildCardBeansFromMatches(List<Card> matches, Map<String, String> idToOwner) {
        if (matches == null || matches.isEmpty() || idToOwner == null || idToOwner.isEmpty()) return java.util.Collections.emptyList();

        return matches.stream()
                .filter(Objects::nonNull)
                .map(c -> {
                    String id = c.getId();
                    if (id == null || !idToOwner.containsKey(id)) return null;
                    Card cached = localCardCache.get(id);
                    Card source = java.util.Objects.requireNonNullElse(cached, c);
                    CardBean bean = source.toBean();
                    bean.setOwner(idToOwner.get(id));
                    return bean;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
