package org.example.model.dao.factory;

import org.example.config.DatabaseConfig;
import org.example.model.dao.UserDao;
import org.example.model.dao.json.JsonUserDao;

public class JsonDaoFactory implements DaoFactory {
    @Override
    public UserDao createUserDao() {
        return new JsonUserDao(DatabaseConfig.JSON_FILE_PATH);
    }
}
