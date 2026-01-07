package org.example.model.dao;

@FunctionalInterface
public interface DaoFactory {
    UserDao createUserDao();
}
