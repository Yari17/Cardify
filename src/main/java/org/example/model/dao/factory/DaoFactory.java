package org.example.model.dao.factory;

import org.example.model.dao.UserDao;

@FunctionalInterface
public interface DaoFactory {
    UserDao createUserDao();
}
