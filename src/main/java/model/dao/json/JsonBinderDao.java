package model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.dao.IBinderDao;
import model.domain.Binder;
import model.exception.DataPersistenceException;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonBinderDao implements IBinderDao {
    private static final Logger LOGGER = Logger.getLogger(JsonBinderDao.class.getName());

    private final String jsonFilePath;
    private final Gson gson;
    private final Map<Long, Binder> bindersById;
    private final Map<String, List<Binder>> bindersByOwner;
    private final AtomicLong idGenerator;

    public JsonBinderDao(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        this.bindersById = new ConcurrentHashMap<>();
        this.bindersByOwner = new ConcurrentHashMap<>();
        this.idGenerator = new AtomicLong(0);

        initializeFile();
        loadFromJson();
    }

    private void initializeFile() {
        File file = new File(jsonFilePath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                LOGGER.warning("Could not create parent directory: " + parentDir.getAbsolutePath());
            }
        }

        if (!file.exists()) {
            saveToJson();
        }
    }

    private void loadFromJson() {
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            LOGGER.info("Binders JSON file not found, starting with empty collection");
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Binder>>() {}.getType();
            List<Binder> binders = gson.fromJson(reader, listType);

            if (binders != null) {
                long maxId = 0;

                for (Binder binder : binders) {
                    bindersById.put(binder.getId(), binder);

                    bindersByOwner.computeIfAbsent(binder.getOwner(), _ -> new ArrayList<>())
                                  .add(binder);

                    if (binder.getId() > maxId) {
                        maxId = binder.getId();
                    }
                }

                idGenerator.set(maxId);
                LOGGER.info(() -> "Loaded " + binders.size() + " binders from JSON");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading binders from JSON, starting fresh", e);
            saveToJson();
        }
    }

    private void saveToJson() {
        try (Writer writer = new FileWriter(jsonFilePath)) {
            List<Binder> allBinders = new ArrayList<>(bindersById.values());
            gson.toJson(allBinders, writer);
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to save binders to " + jsonFilePath, e);
        }
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

        // Genera un nuovo ID se non presente
        if (binder.getId() == 0) {
            binder.setId(idGenerator.incrementAndGet());
        }

        // Imposta createdAt se non presente
        if (binder.getCreatedAt() == null) {
            binder.setCreatedAt(LocalDateTime.now());
        }

        // Aggiorna lastModified
        binder.setLastModified(LocalDateTime.now());

        // Salva in cache
        bindersById.put(binder.getId(), binder);
        bindersByOwner.computeIfAbsent(binder.getOwner(), _ -> new ArrayList<>())
                      .add(binder);

        // Persisti su file
        saveToJson();

        LOGGER.info("Saved binder ID: " + binder.getId() + " for user: " + binder.getOwner());
    }

    @Override
    public void update(Binder binder, String[] params) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder cannot be null");
        }

        if (!bindersById.containsKey(binder.getId())) {
            throw new IllegalArgumentException("Binder not found with ID: " + binder.getId());
        }

        // Aggiorna lastModified
        binder.setLastModified(LocalDateTime.now());

        // Rimuovi dalla cache per owner
        String owner = binder.getOwner();
        List<Binder> ownerBinders = bindersByOwner.get(owner);
        if (ownerBinders != null) {
            ownerBinders.removeIf(b -> b.getId() == binder.getId());
            ownerBinders.add(binder);
        }

        // Aggiorna in cache principale
        bindersById.put(binder.getId(), binder);

        // Persisti su file
        saveToJson();

        LOGGER.info("Updated binder ID: " + binder.getId());
    }

    @Override
    public void delete(Binder binder) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder cannot be null");
        }

        // Rimuovi dalla cache principale
        bindersById.remove(binder.getId());

        // Rimuovi dalla cache per owner
        List<Binder> ownerBinders = bindersByOwner.get(binder.getOwner());
        if (ownerBinders != null) {
            ownerBinders.removeIf(b -> b.getId() == binder.getId());
            if (ownerBinders.isEmpty()) {
                bindersByOwner.remove(binder.getOwner());
            }
        }

        // Persisti su file
        saveToJson();

        LOGGER.info("Deleted binder ID: " + binder.getId());
    }

    // Metodi di utilità specifici per Binder

    public List<Binder> findByOwner(String owner) {
        List<Binder> binders = bindersByOwner.get(owner);
        return binders != null ? new ArrayList<>(binders) : new ArrayList<>();
    }

    public Optional<Binder> findByOwnerAndSetId(String owner, String setId) {
        List<Binder> ownerBinders = bindersByOwner.get(owner);
        if (ownerBinders == null) {
            return Optional.empty();
        }

        return ownerBinders.stream()
                .filter(b -> b.getSetId().equals(setId))
                .findFirst();
    }

    public boolean exists(String owner, String setId) {
        return findByOwnerAndSetId(owner, setId).isPresent();
    }

    public void deleteByOwner(String owner) {
        List<Binder> ownerBinders = bindersByOwner.remove(owner);
        if (ownerBinders != null) {
            ownerBinders.forEach(binder -> bindersById.remove(binder.getId()));
            saveToJson();
            LOGGER.info(() -> "Deleted all binders for owner: " + owner);
        }
    }

    @Override
    public List<Binder> getUserBinders(String owner) {
        List<Binder> userBinders = bindersByOwner.get(owner);
        if (userBinders == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userBinders);
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

    @Override
    public void deleteBinder(String binderId) {

    }

    // Adapter per LocalDateTime con Gson
    private static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }
}
