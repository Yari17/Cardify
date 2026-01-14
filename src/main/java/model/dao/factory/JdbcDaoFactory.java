package model.dao.factory;

import config.DatabaseConfig;
import model.dao.IBinderDao;
import model.dao.IUserDao;
import model.dao.json.JsonBinderDao;
import model.dao.jdbc.JdbcUserDao;
import model.dao.IProposalDao;
import model.dao.json.JsonProposalDao;
import java.io.File;


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

    @Override
    public IProposalDao createProposalDao() {
        // JDBC-based proposal persistence not implemented; fallback to JSON file
        return new JsonProposalDao(DatabaseConfig.JSON_DIR + File.separator + "proposals.json");
    }

    @Override
    public model.dao.ITradeDao createTradeDao() {
        return new model.dao.json.JsonTradeDao(DatabaseConfig.JSON_DIR + File.separator + "trades.json");
    }

}
