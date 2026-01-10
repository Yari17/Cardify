package model.dao.demo;

import model.dao.IBinderDao;
import model.domain.Binder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * In-demo implementation of IBinderDao.
 * Data persists only during application runtime and is lost on shutdown.
 * Useful for demos and testing.
 */
public class DemoBinderDao implements IBinderDao {
    private static final Logger LOGGER = Logger.getLogger(DemoBinderDao.class.getName());

    private final Map<Long, Binder> bindersById;
    private final Map<String, List<Binder>> bindersByOwner;
    private final AtomicLong idGenerator;

    public DemoBinderDao() {
        this.bindersById = new ConcurrentHashMap<>();
        this.bindersByOwner = new ConcurrentHashMap<>();
        this.idGenerator = new AtomicLong(0);

        LOGGER.info("DemoBinderDao initialized - data will be volatile");
    }

    @Override
    public Optional<Binder> get(long id) {
        return Optional.ofNullable(bindersById.get(id));
    }

    @Override
    public List<Binder> getAll() {
        return new ArrayList<>(bindersById.values());
    }

    @Override
    public void save(Binder binder) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder cannot be null");
        }

        if (binder.getId() == 0) {
            binder.setId(idGenerator.incrementAndGet());
        }

        if (binder.getCreatedAt() == null) {
            binder.setCreatedAt(LocalDateTime.now());
        }

        binder.setLastModified(LocalDateTime.now());

        bindersById.put(binder.getId(), binder);
        bindersByOwner.computeIfAbsent(binder.getOwner(), k -> new ArrayList<>()).add(binder);

        LOGGER.info("Saved binder ID: " + binder.getId() + " in demo for user: " + binder.getOwner());
    }

    @Override
    public void update(Binder binder, String[] params) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder cannot be null");
        }

        if (!bindersById.containsKey(binder.getId())) {
            throw new IllegalArgumentException("Binder not found with ID: " + binder.getId());
        }

        binder.setLastModified(LocalDateTime.now());

        String owner = binder.getOwner();
        List<Binder> ownerBinders = bindersByOwner.get(owner);
        if (ownerBinders != null) {
            ownerBinders.removeIf(b -> b.getId() == binder.getId());
            ownerBinders.add(binder);
        }

        bindersById.put(binder.getId(), binder);

        LOGGER.info("Updated binder ID: " + binder.getId() + " in demo");
    }

    @Override
    public void delete(Binder binder) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder cannot be null");
        }

        bindersById.remove(binder.getId());

        List<Binder> ownerBinders = bindersByOwner.get(binder.getOwner());
        if (ownerBinders != null) {
            ownerBinders.removeIf(b -> b.getId() == binder.getId());
            if (ownerBinders.isEmpty()) {
                bindersByOwner.remove(binder.getOwner());
            }
        }

        LOGGER.info("Deleted binder ID: " + binder.getId() + " from demo");
    }

    public List<Binder> findByOwner(String owner) {
        List<Binder> binders = bindersByOwner.get(owner);
        return binders != null ? new ArrayList<>(binders) : new ArrayList<>();
    }

    public Optional<Binder> findByOwnerAndSetId(String owner, String setId) {
        return bindersByOwner.getOrDefault(owner, Collections.emptyList()).stream()
                .filter(binder -> binder.getSetId().equals(setId))
                .findFirst();
    }

    @Override
    public List<Binder> getUserBinders(String owner) {
        return findByOwner(owner);
    }

    @Override
    public void addCardToBinder(String binderId, String cardId) {
        // TODO: Implementare quando avremo la gestione delle carte nei binder
        LOGGER.warning("addCardToBinder not yet implemented");
    }

    @Override
    public void createBinder(String owner, String setId, String setName) {
        Binder binder = new Binder(owner, setId, setName);
        save(binder);
    }
}

