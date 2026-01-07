package model.dao.factory;

import model.dao.UserDao;

@FunctionalInterface
public interface DaoFactory {
    UserDao createUserDao();
}
