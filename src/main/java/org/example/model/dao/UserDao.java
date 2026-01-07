package org.example.model.dao;

import org.example.model.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    void save(User user);
    Optional<User> findByName(String name);
    List<User> findAll();
    void update(User user);
    void delete(String name);
    boolean authenticate(String username, String password);

    /**
     * Register a new user with username and password.
     * @param username the username
     * @param password the password
     * @throws IllegalArgumentException if username already exists
     */
    void register(String username, String password);
}
