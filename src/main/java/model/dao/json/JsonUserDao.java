package model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.dao.IUserDao;
import model.domain.User;
import model.exception.DataPersistenceException;
import model.exception.UserAlreadyExistsException;
import model.exception.UserNotFoundException;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class JsonUserDao implements IUserDao {
    private static final Logger LOGGER = Logger.getLogger(JsonUserDao.class.getName());
    private final Map<String, User> users;
    private final Map<String, String> credentials; 
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
        this.users = new ConcurrentHashMap<>();
        this.credentials = new ConcurrentHashMap<>();

        
        File file = new File(jsonFilePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        loadFromJson();
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
        return Optional.ofNullable(users.get(name));
    }
    @Override
    public boolean authenticate(String username, String password) {
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
        // Gli User sono indicizzati per username, non per ID numerico
        // Cerca l'utente che ha questo ID
        return users.values().stream()
                .filter(user -> user.getId() == id)
                .findFirst();
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String username = user.getUsername();
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

        String username = user.getUsername();
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

        String username = user.getUsername();
        if (!users.containsKey(username)) {
            throw new UserNotFoundException(username);
        }

        users.remove(username);
        credentials.remove(username);
        saveToJson();
    }
}
