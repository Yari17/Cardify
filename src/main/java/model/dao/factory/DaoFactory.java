package model.dao.factory;

import model.dao.IBinderDao;
import model.dao.IProposalDao;
import model.dao.IUserDao;
import model.domain.enumerations.PersistenceType;


public abstract class DaoFactory {

    public static DaoFactory getFactory(PersistenceType type) {
        return switch (type) {
            case JSON -> new JsonDaoFactory();
            case JDBC -> new JdbcDaoFactory();
            case DEMO -> new DemoDaoFactory();
        };
    }

    public abstract IUserDao createUserDao();


    public abstract IBinderDao createBinderDao();

    
    public abstract IProposalDao createProposalDao();

    
    public abstract model.dao.ITradeDao createTradeDao();

}
