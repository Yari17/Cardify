package model.dao.factory;

import model.dao.IBinderDao;
import model.dao.IUserDao;

/**
 * Abstract Factory pattern per la creazione di famiglie di DAO.
 *
 * Questo pattern fornisce un'interfaccia per creare famiglie di oggetti correlati
 * (UserDao, BinderDao, ecc.) senza specificare le loro classi concrete.
 *
 * Ogni implementazione concreta (JsonDaoFactory, JdbcDaoFactory, DemoDaoFactory)
 * garantisce che tutti i DAO creati appartengano alla stessa "famiglia" di persistenza.
 *
 * Pattern: Abstract Factory
 * - DaoFactory: Abstract Factory
 * - JsonDaoFactory, JdbcDaoFactory, DemoDaoFactory: Concrete Factories
 * - IUserDao, IBinderDao: Abstract Products
 * - JsonUserDao, JdbcUserDao, JsonBinderDao, ecc.: Concrete Products
 *
 * Uso:
 * <pre>
 * DaoFactory factory = DaoFactory.getFactory(PersistenceType.JSON);
 * IUserDao userDao = factory.createUserDao();
 * IBinderDao binderDao = factory.createBinderDao();
 * </pre>
 */
public abstract class DaoFactory {

    /**
     * Metodo statico per ottenere l'Abstract Factory appropriata.
     * Questo è un Simple Factory che restituisce l'Abstract Factory desiderata.
     *
     * @param type il tipo di persistenza desiderato
     * @return l'Abstract Factory concreta per il tipo specificato
     */
    public static DaoFactory getFactory(PersistenceType type) {
        return switch (type) {
            case JSON -> new JsonDaoFactory();
            case JDBC -> new JdbcDaoFactory();
            case DEMO -> new DemoDaoFactory();
        };
    }

    /**
     * Factory method per creare un UserDao.
     * Ogni sottoclasse concreta implementa questo metodo per restituire
     * l'implementazione appropriata di UserDao per la sua famiglia.
     *
     * @return istanza di IUserDao
     */
    public abstract IUserDao createUserDao();

    /**
     * Factory method per creare un BinderDao.
     * Ogni sottoclasse concreta implementa questo metodo per restituire
     * l'implementazione appropriata di BinderDao per la sua famiglia.
     *
     * @return istanza di IBinderDao
     */
    public abstract IBinderDao createBinderDao();

    /**
     * Enum che definisce i tipi di persistenza disponibili.
     * Ogni tipo corrisponde a una famiglia di DAO (Abstract Factory concreta).
     */
    public enum PersistenceType {
        /** Persistenza su file JSON */
        JSON,
        /** Persistenza su database JDBC */
        JDBC,
        /** Persistenza in memoria (per demo/test) */
        DEMO
    }
}
