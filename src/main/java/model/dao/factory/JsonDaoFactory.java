package model.dao.factory;

import config.DatabaseConfig;
import model.dao.IBinderDao;
import model.dao.IUserDao;
import model.dao.json.JsonBinderDao;
import model.dao.json.JsonUserDao;

/**
 * Concrete Factory per la creazione di DAO basati su file JSON.
 *
 * Implementazione concreta dell'Abstract Factory DaoFactory.
 * Questa classe crea una famiglia coerente di DAO che persistono i dati su file JSON.
 *
 * Tutti i DAO creati da questa factory utilizzano la persistenza JSON,
 * garantendo coerenza nell'implementazione della persistenza.
 *
 * Pattern: Abstract Factory (Concrete Factory)
 */
public class JsonDaoFactory extends DaoFactory {

    /**
     * Crea un'istanza di UserDao che usa file JSON per la persistenza.
     *
     * @return implementazione JSON di IUserDao
     */
    @Override
    public IUserDao createUserDao() {
        return new JsonUserDao(DatabaseConfig.JSON_FILE_PATH);
    }

    /**
     * Crea un'istanza di BinderDao che usa file JSON per la persistenza.
     *
     * @return implementazione JSON di IBinderDao
     */
    @Override
    public IBinderDao createBinderDao() {
        return new JsonBinderDao(DatabaseConfig.BINDERS_JSON_PATH);
    }
}
