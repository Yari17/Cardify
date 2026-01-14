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

        LOGGER.log(java.util.logging.Level.INFO, "Saved binder ID: {0} in demo for user: {1}",
                new Object[] { binder.getId(), binder.getOwner() });
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

        LOGGER.log(java.util.logging.Level.INFO, "Updated binder ID: {0} in demo", binder.getId());
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

        LOGGER.log(java.util.logging.Level.INFO, "Deleted binder ID: {0} from demo", binder.getId());
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
    public List<Binder> getBindersExcludingOwner(String owner) {
        List<Binder> result = new ArrayList<>();
        for (Binder b : bindersById.values()) {
            if (b == null) continue;
            if (owner == null || !owner.equals(b.getOwner())) {
                result.add(b);
            }
        }
        return result;
    }

    @Override
    public void createBinder(String owner, String setId, String setName) {
        Binder binder = new Binder(owner, setId, setName);
        save(binder);
    }

    @Override
    public void deleteBinder(String binderId) {
        try {
            long id = Long.parseLong(binderId);
            Optional<Binder> binderOpt = get(id);

            if (binderOpt.isPresent()) {
                delete(binderOpt.get());
                LOGGER.log(java.util.logging.Level.INFO, "Binder deleted from demo: {0}", binderId);
            } else {
                LOGGER.log(java.util.logging.Level.WARNING, "No binder found to delete with id: {0}", binderId);
            }
        } catch (NumberFormatException _) {
            LOGGER.log(java.util.logging.Level.WARNING, "Invalid binder ID format: {0}", binderId);
        }
    }
}
