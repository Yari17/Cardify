package model.dao.factory;

import config.DatabaseConfig;
import model.dao.IBinderDao;
import model.dao.IUserDao;
import model.dao.json.JsonBinderDao;
import model.dao.jdbc.JdbcUserDao;


public class JdbcDaoFactory extends DaoFactory {


    @Override
    public IUserDao createUserDao() {
        return new JdbcUserDao(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.JDBC_USER,
                DatabaseConfig.JDBC_PASSWORD);
    }

    @Override
    public IBinderDao createBinderDao() {
        // JDBC binder implementation was removed; use JSON persistence as fallback
        return new JsonBinderDao(DatabaseConfig.BINDERS_JSON_PATH);
    }

}
