package model.dao;

import model.domain.TradeTransaction;

import java.util.List;

public interface ITradeDao extends IDao<TradeTransaction> {
    TradeTransaction getTradeTransactionById(int id);
    void updateTransactionStatus(int id, String status);
    List<TradeTransaction> getUserTradeTransactions(String userId);
}
