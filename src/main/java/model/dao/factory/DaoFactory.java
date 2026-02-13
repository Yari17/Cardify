package model.dao.factory;

import model.dao.IBinderDao;
import model.dao.IProposalDao;
import model.dao.ITradeSessionDao;
import model.dao.IUserDao;
import model.dao.INotificationDao;
import model.domain.enumerations.PersistenceType;

/**
 * Factory astratta per la creazione di Data Access Object.
 * Implementa il pattern Abstract Factory per supportare diverse tipologie di
 * persistenza
 * (DEMO, JDBC, JSON) in modo trasparente ai client.
 */
public abstract class DaoFactory {

    // Cache delle factory instances
    private static DemoDaoFactory demoInstance;
    private static JdbcDaoFactory jdbcInstance;
    private static JsonDaoFactory jsonInstance;

    /**
     * Restituisce un'istanza di DaoFactory per il tipo specificato.
     * Ogni tipo di persistenza ha una sola istanza factory per mantenere
     * i dati in cache.
     * 
     * @param type Tipo di persistenza richiesto
     * @return Istanza della factory corrispondente
     */
    public static DaoFactory getFactory(PersistenceType type) {
        return switch (type) {
            case JSON -> {
                if (jsonInstance == null) {
                    jsonInstance = new JsonDaoFactory();
                }
                yield jsonInstance;
            }
            case JDBC -> {
                if (jdbcInstance == null) {
                    jdbcInstance = new JdbcDaoFactory();
                }
                yield jdbcInstance;
            }
            case DEMO -> {
                if (demoInstance == null) {
                    demoInstance = new DemoDaoFactory();
                }
                yield demoInstance;
            }
        };
    }

    /**
     * Crea un DAO specializzato nella gestione degli utenti.
     * 
     * @return Un'istanza di {@link IUserDao}.
     */
    public abstract IUserDao createUserDao();

    /**
     * Crea un DAO specializzato nella gestione dei raccoglitori.
     * 
     * @return Un'istanza di {@link IBinderDao}.
     */
    public abstract IBinderDao createBinderDao();

    /**
     * Crea un DAO specializzato nella gestione delle proposte di scambio.
     * 
     * @return Un'istanza di {@link IProposalDao}.
     */
    public abstract IProposalDao createProposalDao();

    /**
     * Crea un DAO specializzato nella gestione delle sessioni di scambio fisico.
     * 
     * @return Un'istanza di {@link ITradeSessionDao}.
     */
    public abstract ITradeSessionDao createTradeDao();

    /**
     * Crea un DAO specializzato nella gestione delle notifiche.
     * 
     * @return Un'istanza di {@link INotificationDao}.
     */
    public abstract INotificationDao createNotificationDao();

}
