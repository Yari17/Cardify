package model.dao.demo;

import model.dao.ITradeDao;
import model.domain.TradeTransaction;

import java.util.List;
import java.util.Optional;

public class DemoTradeDao implements ITradeDao {
    @Override
    public TradeTransaction getTradeTransactionById(int id) {
        return null;
    }

    @Override
    public void updateTransactionStatus(int id, String status) {

    }

    @Override
    public List<TradeTransaction> getUserTradeTransactions(String userId) {
        return List.of();
    }

    @Override
    public Optional get(long id) {
        return Optional.empty();
    }

    @Override
    public void save(TradeTransaction tradeTransaction) {

    }

    @Override
    public void update(TradeTransaction tradeTransaction, String[] params) {

    }

    @Override
    public void delete(TradeTransaction tradeTransaction) {

    }




    }


