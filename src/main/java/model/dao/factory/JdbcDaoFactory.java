package model.dao.factory;

import config.DatabaseConfig;
import model.dao.IBinderDao;
import model.dao.IUserDao;
import model.dao.jdbc.JdbcBinderDao;
import model.dao.jdbc.JdbcUserDao;

/**
 * Concrete Factory per la creazione di DAO basati su database JDBC.
 *
 * Implementazione concreta dell'Abstract Factory DaoFactory.
 * Questa classe crea una famiglia coerente di DAO che persistono i dati
 * su database relazionale tramite JDBC.
 *
 * Tutti i DAO creati da questa factory utilizzano la stessa connessione JDBC,
 * garantendo coerenza nell'implementazione della persistenza.
 *
 * Pattern: Abstract Factory (Concrete Factory)
 */
public class JdbcDaoFactory extends DaoFactory {

    /**
     * Crea un'istanza di UserDao che usa JDBC per la persistenza.
     *
     * @return implementazione JDBC di IUserDao
     */
    @Override
    public IUserDao createUserDao() {
        return new JdbcUserDao(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.JDBC_USER,
                DatabaseConfig.JDBC_PASSWORD);
    }

    /**
     * Crea un'istanza di BinderDao che usa JDBC per la persistenza.
     *
     * @return implementazione JDBC di IBinderDao
     */
    @Override
    public IBinderDao createBinderDao() {
        return new JdbcBinderDao(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.JDBC_USER,
                DatabaseConfig.JDBC_PASSWORD,
                createCardDao());
    }

    @Override
    public model.dao.ICardDao createCardDao() {
        return new model.dao.jdbc.JdbcCardDao(
                config.DatabaseConfig.JDBC_URL,
                config.DatabaseConfig.JDBC_USER,
                config.DatabaseConfig.JDBC_PASSWORD);
    }
}
