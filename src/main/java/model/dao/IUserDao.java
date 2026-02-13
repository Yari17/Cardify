package model.dao;

import model.domain.User;

import java.util.List;

public interface IUserDao {

    void register(User user);

    User authenticateAndGetUser(String username, String password);

    List<User> getStores();
}