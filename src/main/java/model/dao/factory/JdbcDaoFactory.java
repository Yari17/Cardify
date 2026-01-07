package model.dao.factory;

import config.DatabaseConfig;
import model.dao.UserDao;
import model.dao.jdbc.JdbcUserDao;

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
