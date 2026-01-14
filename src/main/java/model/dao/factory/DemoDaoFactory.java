package model.dao.factory;

import model.dao.*;
import model.dao.demo.DemoBinderDao;
import model.dao.demo.DemoUserDao;
import model.dao.demo.DemoProposalDao;


public class DemoDaoFactory extends DaoFactory {

    @Override
    public IUserDao createUserDao() {
        return new DemoUserDao();
    }

    @Override
    public IBinderDao createBinderDao() {
        return new DemoBinderDao();
    }

    @Override
    public IProposalDao createProposalDao() {
        return new DemoProposalDao();
    }

    @Override
    public model.dao.ITradeDao createTradeDao() {
        return new model.dao.demo.DemoTradeDao();
    }

    // No additional factory methods required for demo implementation

}
