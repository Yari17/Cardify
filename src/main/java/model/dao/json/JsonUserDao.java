package model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.dao.IUserDao;
import model.domain.User;
import exception.DataPersistenceException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class JsonUserDao implements IUserDao {
    private static final Logger LOGGER = Logger.getLogger(JsonUserDao.class.getName());
    // Static caches to persist data until application stops
    private static final Map<String, User> users = new ConcurrentHashMap<>();
    private static final Map<String, String> credentials = new ConcurrentHashMap<>();
    private static boolean loaded = false;

    private final String jsonFilePath;
    private final Gson gson;

    private static class UserData {
        Map<String, User> users;
        Map<String, String> credentials;

        UserData(Map<String, User> users, Map<String, String> credentials) {
            this.users = users;
            this.credentials = credentials;
        }
    }

    public JsonUserDao(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        File file = new File(jsonFilePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Remove direct call to loadFromJson() from constructor to support lazy loading
    }

    private synchronized void ensureLoaded() {
        if (!loaded) {
            loadFromJson();
            loaded = true;
        }
    }

    private void loadFromJson() {
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            saveToJson();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            UserData data = gson.fromJson(reader, UserData.class);
            if (data != null) {
                if (data.users != null) {
                    users.putAll(data.users);
                }
                if (data.credentials != null) {
                    credentials.putAll(data.credentials);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading JSON, creating new file", e);
            saveToJson();
        }
    }

    private void saveToJson() {
        try (Writer writer = new FileWriter(jsonFilePath)) {
            UserData data = new UserData(users, credentials);
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to save JSON to " + jsonFilePath, e);
        }
    }

    @Override
    public Optional<User> findByName(String name) {
        ensureLoaded();
        return Optional.ofNullable(users.get(name));
    }

    @Override
    public boolean authenticate(String username, String password) {
        ensureLoaded();
        String storedPassword = credentials.get(username);
        if (storedPassword == null) {
            throw new UserNotFoundException(username);
        }
        return storedPassword.equals(password);
    }

    @Override
    public Optional<User> authenticateAndGetUser(String username, String password) {
        if (authenticate(username, password)) {
            return findByName(username);
        }
        return Optional.empty();
    }

    @Override
    public void register(String username, String password, String userType) {
        ensureLoaded();

        if (users.containsKey(username)) {
            throw new UserAlreadyExistsException(username);
        }

        credentials.put(username, password);
        User newUser = new User(username, 0, 0);
        newUser.setUserType(userType);
        users.put(username, newUser);
        saveToJson();
    }

    // ========== Implementazione metodi IDao<User> ==========

    @Override
    public Optional<User> get(long id) {
        ensureLoaded();
        // Gli User sono indicizzati per username, non per ID numerico
        // Cerca l'utente che ha questo ID
        return users.values().stream()
                .filter(user -> user.getId() == id)
                .findFirst();
    }

    @Override
    public List<User> getAll() {
        ensureLoaded();
        return new ArrayList<>(users.values());
    }

    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        ensureLoaded();

        String username = user.getName();
        if (users.containsKey(username)) {
            throw new UserAlreadyExistsException(username);
        }

        users.put(username, user);
        saveToJson();
    }

    @Override
    public void update(User user, String[] params) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        ensureLoaded();

        String username = user.getName();
        if (!users.containsKey(username)) {
            throw new UserNotFoundException(username);
        }

        users.put(username, user);
        saveToJson();
    }

    @Override
    public void delete(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        ensureLoaded();

        String username = user.getName();
        if (!users.containsKey(username)) {
            throw new UserNotFoundException(username);
        }

        users.remove(username);
        credentials.remove(username);
        saveToJson();
    }
}
