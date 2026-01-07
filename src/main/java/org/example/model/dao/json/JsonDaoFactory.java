package org.example.model.dao.json;

import org.example.config.DatabaseConfig;
import org.example.model.dao.DaoFactory;
import org.example.model.dao.UserDao;

public class JsonDaoFactory implements DaoFactory {
    @Override
    public UserDao createUserDao() {
        return new JsonUserDao(DatabaseConfig.JSON_FILE_PATH);
    }
}
