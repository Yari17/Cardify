package model.dao.demo;

import model.dao.IUserDao;
import model.domain.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * In-memory implementation of IUserDao for demo purposes.
 * Data persists only during application runtime and is lost on shutdown.
 * Useful for demos and testing.
 */
public class DemoUserDao implements IUserDao {
    private static final Logger LOGGER = Logger.getLogger(DemoUserDao.class.getName());

    private final Map<String, User> users;
    private final Map<String, String> credentials;
    private long nextId = 1;

    public DemoUserDao() {
        this.users = new ConcurrentHashMap<>();
        this.credentials = new ConcurrentHashMap<>();
        LOGGER.info("DemoUserDao initialized - data will be volatile");
    }

    // Seed users into demo DAO
    public void loadFromCollection(Collection<User> initialUsers, Map<String, String> creds) {
        users.clear();
        credentials.clear();
        if (initialUsers == null) {
            LOGGER.log(Level.INFO, "DemoUserDao.loadFromCollection: loaded 0 users (null input)");
            return;
        }
        int count = 0;
        for (User u : initialUsers) {
            if (u == null) continue;
            if (u.getId() == 0) u.setId(nextId++);
            users.put(u.getName(), u);
            count++;
        }
        if (creds != null) credentials.putAll(creds);
        LOGGER.log(Level.INFO, "DemoUserDao.loadFromCollection: loaded {0} users into memory", count);
    }

    @Override
    public Optional<User> get(long id) {
        return users.values().stream()
                .filter(user -> user.getId() == id)
                .findFirst();
    }


    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == 0) {
            user.setId(nextId++);
        }
        users.put(user.getName(), user);
        LOGGER.log(Level.INFO, "User saved: {0}", user.getName());
    }

    @Override
    public void update(User user, String[] params) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!users.containsKey(user.getName())) {
            throw new IllegalStateException("User not found: " + user.getName());
        }
        users.put(user.getName(), user);
        LOGGER.log(Level.INFO, "User updated: {0}", user.getName());
    }

    @Override
    public void delete(User user) {
        if (user != null) {
            users.remove(user.getName());
            credentials.remove(user.getName());
            LOGGER.log(Level.INFO, "User deleted: {0}", user.getName());
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
            throw new IllegalArgumentException("User already exists: " + username);
        }

        User user = new User(username, 0, 0);
        user.setId(nextId++);
        user.setUserType(userType);

        users.put(username, user);
        credentials.put(username, password);
        LOGGER.log(Level.INFO, "User registered: {0}", username);
    }

    @Override
    public java.util.List<String> findAllUsernames() {
        return new java.util.ArrayList<>(users.keySet());
    }
}
