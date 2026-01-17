package model.dao.demo;

import model.dao.ITradeDao;
import model.domain.TradeTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DemoTradeDao implements ITradeDao {
    @Override
    public TradeTransaction getTradeTransactionById(int id) {
        return null;
    }

    @Override
    public void updateTransactionStatus(int id, String status) {
        //TODO implementare update dello status
    }

    @Override
    public List<TradeTransaction> getUserTradeTransactions(String userId) {
        return List.of();
    }

    @Override
    public List<TradeTransaction> getStoreTradeScheduledTransactions(String userId, String tradeId) {
        return List.of();
    }

    @Override
    public List<TradeTransaction> getUserTradeTransactions(String userId, String tradeId) {
        return List.of();
    }

    @Override
    public List<TradeTransaction> getUserCompletedTrades(String userId) {
        return List.of();
    }

    @Override
    public Optional<TradeTransaction> get(long id) {
        return Optional.empty();
    }

    @Override
    public void save(TradeTransaction tradeTransaction) {
        //TODO implementare salvataggio
    }

    @Override
    public void update(TradeTransaction tradeTransaction, String[] params) {
        //TODO implementare update
    }

    @Override
    public void delete(TradeTransaction tradeTransaction) {
        //TODO implementare delete
    }

    @Override
    public Optional<TradeTransaction> findByParticipantsAndDate(String proposerId, String receiverId, LocalDateTime tradeDate) {
        return Optional.empty();
    }

    @Override
    public TradeTransaction getTradeTransactionBySessionCodes(int proposerCode, int receiverCode) {
        return null;
    }

    @Override
    public List<TradeTransaction> getStoreTradeInProgressTransactions(String storeId) {
        return List.of();
    }

}
