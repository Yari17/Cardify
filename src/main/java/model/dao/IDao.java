package model.dao;

import java.util.Optional;

public interface IDao<T>{
    Optional<T> get(long id);

    void save(T t);

    void update(T t, String[] params);

    void delete(T t);

}
