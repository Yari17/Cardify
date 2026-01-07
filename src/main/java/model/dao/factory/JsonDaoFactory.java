package model.dao.factory;

import config.DatabaseConfig;
import model.dao.UserDao;
import model.dao.json.JsonUserDao;

public class JsonDaoFactory implements DaoFactory {
    @Override
    public UserDao createUserDao() {
        return new JsonUserDao(DatabaseConfig.JSON_FILE_PATH);
    }
}
