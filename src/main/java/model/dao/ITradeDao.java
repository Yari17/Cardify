package model.dao;

import model.domain.TradeTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ITradeDao extends IDao<TradeTransaction> {
    TradeTransaction getTradeTransactionById(int id);
    void updateTransactionStatus(int id, String status);
    List<TradeTransaction> getUserTradeTransactions(String userId);
    List<TradeTransaction> getStoreTradeScheduledTransactions(String userId, String tradeId);
    List<TradeTransaction> getUserTradeTransactions(String userId, String tradeId);

    
    List<TradeTransaction> getUserCompletedTrades(String userId);

    
    Optional<TradeTransaction> findByParticipantsAndDate(String proposerId, String receiverId, LocalDateTime tradeDate);

    
    TradeTransaction getTradeTransactionBySessionCodes(int proposerCode, int receiverCode);

    
    List<TradeTransaction> getStoreTradeInProgressTransactions(String storeId);

    
    List<TradeTransaction> getStoreCompletedTrades(String storeId);
}
