package org.example.model.dao.jdbc;

import org.example.config.DatabaseConfig;
import org.example.model.dao.DaoFactory;
import org.example.model.dao.UserDao;

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
