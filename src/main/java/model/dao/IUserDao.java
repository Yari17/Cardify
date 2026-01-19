package model.dao;

import model.domain.User;

import java.util.Optional;

public interface IUserDao extends IDao<User> {
    
    Optional<User> findByName(String name);
    boolean authenticate(String username, String password);
    Optional<User> authenticateAndGetUser(String username, String password);

    void register(String username, String password, String userType);

    java.util.List<String> findAllUsernames();
}
