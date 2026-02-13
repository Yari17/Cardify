package model.dao.factory;

import model.dao.IBinderDao;
import model.dao.ITradeSessionDao;
import model.dao.IUserDao;
import model.dao.jdbc.JdbcBinderDao;
import model.dao.jdbc.JdbcUserDao;
import model.dao.jdbc.JdbcProposalDao;
import model.dao.jdbc.JdbcNotificationDao;
import model.dao.IProposalDao;
import model.dao.INotificationDao;

public class JdbcDaoFactory extends DaoFactory {

    private IUserDao userDao;
    private IBinderDao binderDao;
    private IProposalDao proposalDao;
    private ITradeSessionDao tradeDao;
    private INotificationDao notificationDao;

    @Override
    public INotificationDao createNotificationDao() {
        if (notificationDao == null) {
            notificationDao = new JdbcNotificationDao();
        }
        return notificationDao;
    }

    @Override
    public IUserDao createUserDao() {
        if (userDao == null) {
            userDao = new JdbcUserDao();
        }
        return userDao;
    }

    @Override
    public IBinderDao createBinderDao() {
        if (binderDao == null) {
            binderDao = new JdbcBinderDao();
        }
        return binderDao;
    }

    @Override
    public IProposalDao createProposalDao() {
        if (proposalDao == null) {
            proposalDao = new JdbcProposalDao();
        }
        return proposalDao;
    }

    @Override
    public ITradeSessionDao createTradeDao() {
        if (tradeDao == null) {
            tradeDao = new model.dao.jdbc.JdbcTradeSessionDao();
        }
        return tradeDao;
    }

}
