package model.dao;

import model.domain.TradeTransaction;

import java.util.List;

public interface ITradeDao extends IDao<TradeTransaction> {
    TradeTransaction getTradeTransactionById(int id);
    void updateTransactionStatus(int id, String status);
    List<TradeTransaction> getUserTradeTransactions(String userId);
    List<TradeTransaction> getStoreTradeScheduledTransactions(String userId, String tradeId);//ritorna la lista degli scambi programmati
    List<TradeTransaction> getUserTradeTransactions(String userId, String tradeId);
}
