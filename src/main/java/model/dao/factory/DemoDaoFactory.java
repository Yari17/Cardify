package model.dao.factory;

import model.dao.*;
import model.dao.demo.DemoBinderDao;
import model.dao.demo.DemoUserDao;
import model.dao.demo.DemoProposalDao;
import model.dao.demo.DemoTradeDao;


public class DemoDaoFactory extends DaoFactory {


    private DemoUserDao userDao;
    private DemoBinderDao binderDao;
    private DemoProposalDao proposalDao;
    private DemoTradeDao tradeDao;

    @Override
    public IUserDao createUserDao() {
        if (userDao == null) userDao = new DemoUserDao();
        return userDao;
    }

    @Override
    public IBinderDao createBinderDao() {
        if (binderDao == null) binderDao = new DemoBinderDao();
        return binderDao;
    }

    @Override
    public IProposalDao createProposalDao() {
        if (proposalDao == null) proposalDao = new DemoProposalDao();
        return proposalDao;
    }

    @Override
    public model.dao.ITradeDao createTradeDao() {
        if (tradeDao == null) tradeDao = new DemoTradeDao();
        return tradeDao;
    }


}
