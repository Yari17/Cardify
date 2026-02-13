package model.dao.factory;

import model.dao.IBinderDao;
import model.dao.IProposalDao;
import model.dao.ITradeSessionDao;
import model.dao.IUserDao;
import model.dao.INotificationDao;

public class JsonDaoFactory extends DaoFactory {

    private IBinderDao binderDao;

    @Override
    public IUserDao createUserDao() {
        throw new UnsupportedOperationException("JsonUserDao not implemented yet");
    }

    @Override
    public IBinderDao createBinderDao() {
        if (binderDao == null) {
            binderDao = new model.dao.json.JsonBinderDao();
        }
        return binderDao;
    }

    @Override
    public IProposalDao createProposalDao() {
        throw new UnsupportedOperationException("JsonProposalDao not implemented yet");
    }

    @Override
    public ITradeSessionDao createTradeDao() {
        throw new UnsupportedOperationException("JsonTradeDao not implemented yet");
    }

    @Override
    public INotificationDao createNotificationDao() {
        throw new UnsupportedOperationException("JsonNotificationDao not implemented yet");
    }
}
