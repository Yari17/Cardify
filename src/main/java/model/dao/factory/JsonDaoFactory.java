package model.dao.factory;

import config.DatabaseConfig;
import model.dao.IBinderDao;
import model.dao.IUserDao;
import model.dao.json.JsonBinderDao;
import model.dao.json.JsonUserDao;
import model.dao.json.JsonProposalDao;
import model.dao.json.JsonTradeDao;

import java.io.File;


public class JsonDaoFactory extends DaoFactory {

    
    @Override
    public IUserDao createUserDao() {
        return new JsonUserDao(DatabaseConfig.JSON_FILE_PATH);
    }

    
    @Override
    public IBinderDao createBinderDao() {
        return new JsonBinderDao(DatabaseConfig.BINDERS_JSON_PATH);
    }

    @Override
    public model.dao.IProposalDao createProposalDao() {
        return new JsonProposalDao(DatabaseConfig.JSON_DIR + File.separator + "proposals.json");
    }

    @Override
    public model.dao.ITradeDao createTradeDao() {
        
        return new JsonTradeDao();
    }

    
}
