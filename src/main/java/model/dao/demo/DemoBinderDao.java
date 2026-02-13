package model.dao.demo;

import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.Card;
import model.domain.CollectionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione in-memory del DAO per i raccoglitori (modalità Demo).
 * Mantiene le collezioni degli utenti durante l'esecuzione dell'app senza
 * persistenza su disco.
 */
public class DemoBinderDao implements IBinderDao {
    /** Cache dei raccoglitori creati durante la sessione. */
    List<Binder> bindersCache;
    /** Cache delle carte (caricate tipicamente via API). */
    List<Card> cardsCache;

    /**
     * Inizializza le liste per la gestione in memoria.
     */
    public DemoBinderDao() {
        bindersCache = new ArrayList<>();
        cardsCache = new ArrayList<>();

        // Binder precaricato per FrancescoTotti10
        Card charizard = new Card("Charizard", "base4-4", "base4",
                "https://assets.tcgdex.net/en/base/base4/4/high.jpg",
                model.domain.enumerations.CardGameType.POKEMON);
        CollectionItem charizardItem = new CollectionItem(charizard, 10);
        List<CollectionItem> francescoCards = new ArrayList<>();
        francescoCards.add(charizardItem);
        Binder francescoBinder = new Binder("FrancescoTotti10", "base4", "Base Set 2", francescoCards);
        bindersCache.add(francescoBinder);
    }

    @Override
    public List<Binder> getUserBinders(String owner) {
        List<Binder> result = new ArrayList<>();
        for (Binder b : bindersCache) {
            if (b.getOwner().equals(owner)) {
                result.add(b);
            }
        }
        return result;
    }

    @Override
    public List<Binder> getBindersExcludingOwner(String owner) {
        List<Binder> result = new ArrayList<>();
        for (Binder b : bindersCache) {
            if (!b.getOwner().equals(owner)) {
                result.add(b);
            }
        }
        return result;
    }

    @Override
    public Binder getBinderByOwnerAndSet(String owner, String setId) {
        for (Binder b : bindersCache) {
            if (b.getOwner().equals(owner) && b.getSetID().equals(setId)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public Binder findOrCreateBinder(String owner, String setId, String setName) {
        Binder binder = getBinderByOwnerAndSet(owner, setId);
        if (binder == null) {
            binder = new Binder(owner, setId, setName, new ArrayList<>());
            createBinder(owner, binder);
        }
        return binder;
    }

    @Override
    public void createBinder(String owner, Binder binder) {
        // Deep copy items if needed, or just use the list
        Binder newBinder = new Binder(owner, binder.getSetID(), binder.getSetName(), binder.getOwnedCards());
        bindersCache.add(newBinder);
    }

    @Override
    public void deleteBinder(String owner, Binder binder) {
        bindersCache.removeIf(b -> b.getOwner().equals(owner) && b.getSetID().equals(binder.getSetID()));
    }

    @Override
    public void save(Binder binder) {
        bindersCache.removeIf(b -> b.getOwner().equals(binder.getOwner()) && b.getSetID().equals(binder.getSetID()));
        bindersCache.add(binder);
    }

}
