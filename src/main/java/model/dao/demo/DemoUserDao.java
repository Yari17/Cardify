package model.dao.demo;

import model.dao.IUserDao;
import model.domain.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    @Override
    public Optional<User> get(long id) {
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
        if (user.getId() == 0) {
            user.setId(nextId++);
        }
        users.put(user.getUsername(), user);
        LOGGER.info(() -> "User saved: " + user.getUsername());
    }

    @Override
    public void update(User user, String[] params) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!users.containsKey(user.getUsername())) {
            throw new IllegalStateException("User not found: " + user.getUsername());
        }
        users.put(user.getUsername(), user);
        LOGGER.info(() -> "User updated: " + user.getUsername());
    }

    @Override
    public void delete(User user) {
        if (user != null) {
            users.remove(user.getUsername());
            credentials.remove(user.getUsername());
            LOGGER.info(() -> "User deleted: " + user.getUsername());
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
        LOGGER.info(() -> "User registered: " + username);
    }
}

