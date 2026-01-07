package org.example.model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.domain.User;
import org.example.model.dao.UserDao;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonUserDao implements UserDao {
    private final Map<String, User> users;
    private final Map<String, String> credentials; // username -> password
    private final String jsonFilePath;
    private final Gson gson;

    // Inner class for JSON serialization
    private static class UserData {
        Map<String, User> users;
        Map<String, String> credentials;

        UserData() {
            this.users = new HashMap<>();
            this.credentials = new HashMap<>();
        }

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

        // Create directory if not exists
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
            initializeDefaults();
            saveToJson(); // Save defaults to file
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
            initializeDefaults();
            saveToJson();
        }
    }

    private void initializeDefaults() {
        credentials.put("admin", "password123");
        users.put("admin", new User("admin", 0, 0));
    }

    private void saveToJson() {
        try (Writer writer = new FileWriter(jsonFilePath)) {
            UserData data = new UserData(users, credentials);
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save JSON", e);
        }
    }

    @Override
    public void save(User user) {
        users.put(user.getName(), user);
        saveToJson();
    }

    @Override
    public Optional<User> findByName(String name) {
        return Optional.ofNullable(users.get(name));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void update(User user) {
        if (users.containsKey(user.getName())) {
            users.put(user.getName(), user);
            saveToJson();
        }
    }

    @Override
    public void delete(String name) {
        users.remove(name);
        credentials.remove(name);
        saveToJson();
    }

    @Override
    public boolean authenticate(String username, String password) {
        String storedPassword = credentials.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    @Override
    public void register(String username, String password) {
        // Check if user already exists
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Username già esistente");
        }

        // Create new user with credentials
        credentials.put(username, password);
        users.put(username, new User(username, 0, 0));
        saveToJson();
    }
}
