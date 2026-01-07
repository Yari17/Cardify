package model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.domain.User;
import model.dao.UserDao;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonUserDao implements UserDao {
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
            saveToJson();
        }
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
    public Optional<User> findByName(String name) {
        return Optional.ofNullable(users.get(name));
    }
    @Override
    public boolean authenticate(String username, String password) {
        String storedPassword = credentials.get(username);
        return storedPassword != null && storedPassword.equals(password);
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
            throw new IllegalArgumentException("Username già esistente");
        }

        
        credentials.put(username, password);
        User newUser = new User(username, 0, 0);
        newUser.setUserType(userType); 
        users.put(username, newUser);
        saveToJson();
    }
}
