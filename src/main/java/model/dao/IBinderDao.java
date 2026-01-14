package model.dao;

import model.domain.Binder;

import java.util.List;


public interface IBinderDao extends IDao<Binder> {


    List<Binder> getUserBinders(String owner);


    List<Binder> getBindersExcludingOwner(String owner);


    void createBinder(String owner, String setId, String setName);


    void deleteBinder(String binderId);
}
