package model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.dao.IBinderDao;
import model.domain.Binder;

import exception.DaoException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione del DAO per i raccoglitori basata su file JSON.
 * Utilizza la libreria Google GSON per la serializzazione e deserializzazione
 * degli oggetti.
 * Adatta per persistenza leggera senza necessità di un server database.
 */
public class JsonBinderDao implements IBinderDao {

    private static final String FILE_PATH = "database/binders.json";
    private final Gson gson;

    public JsonBinderDao() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initFile();
    }

    /**
     * Inizializza il file JSON se non esiste, creando le directory necessarie
     * e un array vuoto come contenuto di base.
     */
    private void initFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(file)) {
                writer.write("[]");
            } catch (IOException e) {
                throw new DaoException("Inizializzazione del file JSON fallita", e);
            }
        }
    }

    /**
     * Carica l'intera lista di raccoglitori dal file JSON.
     * 
     * @return Lista di oggetti {@link Binder} caricati, o una lista vuota se il
     *         file è corrotto o vuoto.
     * @throws DaoException In caso di errori di I/O.
     */
    private List<Binder> loadAll() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<ArrayList<Binder>>() {
            }.getType();
            List<Binder> binders = gson.fromJson(reader, listType);
            return binders != null ? binders : new ArrayList<>();
        } catch (IOException e) {
            throw new DaoException("Caricamento dei raccoglitori da JSON fallito", e);
        }
    }

    /**
     * Salva la lista completa di raccoglitori sovrascrivendo il file JSON.
     * 
     * @param binders Lista di raccoglitori da persistere.
     * @throws DaoException In caso di errori durante la scrittura del file.
     */
    private void saveAll(List<Binder> binders) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(binders, writer);
        } catch (IOException e) {
            throw new DaoException("Salvataggio dei raccoglitori su JSON fallito", e);
        }
    }

    @Override
    public List<Binder> getUserBinders(String owner) {
        return loadAll().stream()
                .filter(b -> b.getOwner() != null && b.getOwner().equals(owner))
                .toList();
    }

    @Override
    public List<Binder> getBindersExcludingOwner(String owner) {
        return loadAll().stream()
                .filter(b -> b.getOwner() != null && !b.getOwner().equals(owner))
                .toList();
    }

    @Override
    public Binder getBinderByOwnerAndSet(String owner, String setId) {
        return loadAll().stream()
                .filter(b -> b.getOwner().equals(owner) && b.getSetID().equals(setId))
                .findFirst()
                .orElse(null);
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
        List<Binder> binders = loadAll();
        boolean exists = binders.stream()
                .anyMatch(b -> b.getOwner().equals(owner) && b.getSetID().equals(binder.getSetID()));

        if (!exists) {
            binders.add(binder);
            saveAll(binders);
        }
    }

    @Override
    public void deleteBinder(String owner, Binder binder) {
        List<Binder> binders = loadAll();
        binders.removeIf(b -> b.getOwner().equals(owner) && b.getSetID().equals(binder.getSetID()));
        saveAll(binders);
    }

    @Override
    public void save(Binder binder) {
        List<Binder> binders = loadAll();
        for (int i = 0; i < binders.size(); i++) {
            Binder b = binders.get(i);
            if (b.getOwner().equals(binder.getOwner()) && b.getSetID().equals(binder.getSetID())) {
                binders.set(i, binder);
                saveAll(binders);
                return;
            }
        }
    }

}
