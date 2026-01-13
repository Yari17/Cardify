package model.dao.factory;

import model.dao.*;
import model.dao.demo.DemoBinderDao;
import model.dao.demo.DemoUserDao;

/**
 * Concrete Factory per la creazione di DAO in-memory (demo/test).
 *
 * Implementazione concreta dell'Abstract Factory DaoFactory.
 * Questa classe crea una famiglia coerente di DAO che mantengono i dati
 * solo in memoria durante l'esecuzione dell'applicazione.
 *
 * I dati non sono persistenti e vengono persi alla chiusura dell'applicazione.
 * Utile per demo, testing e sviluppo rapido.
 *
 * Pattern: Abstract Factory (Concrete Factory)
 */
public class DemoDaoFactory extends DaoFactory {

    /**
     * Crea un'istanza di UserDao che mantiene i dati in memoria.
     *
     * @return implementazione in-memory di IUserDao
     */
    @Override
    public IUserDao createUserDao() {
        return new DemoUserDao();
    }

    /**
     * Crea un'istanza di BinderDao che mantiene i dati in memoria.
     *
     * @return implementazione in-memory di IBinderDao
     */
    @Override
    public IBinderDao createBinderDao() {
        return new DemoBinderDao();
    }

    // cards are not persisted separately in demo factory
}
