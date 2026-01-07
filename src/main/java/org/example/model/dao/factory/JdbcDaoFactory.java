package org.example.model.dao.factory;

import org.example.config.DatabaseConfig;
import org.example.model.dao.UserDao;
import org.example.model.dao.jdbc.JdbcUserDao;

public class JdbcDaoFactory implements DaoFactory {
    @Override
    public UserDao createUserDao() {
        return new JdbcUserDao(
            DatabaseConfig.JDBC_URL,
            DatabaseConfig.JDBC_USER,
            DatabaseConfig.JDBC_PASSWORD
        );
    }
}
