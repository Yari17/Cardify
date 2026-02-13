package model.dao.demo;

import model.dao.ITradeSessionDao;
import model.domain.TradeSession;
import model.domain.User;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementazione in-memory del DAO per le sessioni di scambio (modalità Demo).
 * Mantiene lo stato degli scambi presso lo store, simulando la gestione di
 * arrivi e ispezioni.
 */
public class DemoTradeSessionDao implements ITradeSessionDao {
    /** Cache delle sessioni di scambio attive e concluse. */
    private List<TradeSession> cachedTradeSessions;

    /**
     * Inizializza la cache locale.
     */
    public DemoTradeSessionDao() {
        this.cachedTradeSessions = new ArrayList<>();
    }

    @Override
    public TradeSession getTradeSessionById(int id) {
        return cachedTradeSessions.stream()
                .filter(t -> t.getSessionId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void saveTradeSession(TradeSession tradeSession) {
        if (tradeSession == null)
            return;
        // Simulate Auto Increment
        int newId = cachedTradeSessions.size() + 1;
        tradeSession.setSessionId(newId);
        cachedTradeSessions.add(tradeSession);
    }

    @Override
    public List<TradeSession> getUserTradeSessions(User user) {
        if (user == null)
            return new ArrayList<>();
        return cachedTradeSessions.stream()
                .filter(t -> t.getProposerId().equals(user.getUsername())
                        || t.getReceiverId().equals(user.getUsername()))
                .toList();
    }

    @Override
    public List<TradeSession> getStoreScheduledTrades(User user) {
        if (user == null)
            return new ArrayList<>();
        // Assuming simplified logic: returns session for this store if pending
        return cachedTradeSessions.stream()
                .filter(t -> t.getStoreId() != null && t.getStoreId().equals(user.getUsername()))
                .filter(t -> t.getTradeStatus() == model.domain.enumerations.TradeStatus.WAITING_FOR_ARRIVAL)
                .toList();
    }

    @Override
    public List<TradeSession> getStoreInProgressTrades(User user) {
        if (user == null)
            return new ArrayList<>();
        return cachedTradeSessions.stream()
                .filter(t -> t.getStoreId() != null && t.getStoreId().equals(user.getUsername()))
                .filter(t -> t.getTradeStatus() == model.domain.enumerations.TradeStatus.PARTIALLY_ARRIVED ||
                        t.getTradeStatus() == model.domain.enumerations.TradeStatus.BOTH_ARRIVED ||
                        t.getTradeStatus() == model.domain.enumerations.TradeStatus.INSPECTION_PHASE ||
                        t.getTradeStatus() == model.domain.enumerations.TradeStatus.INSPECTION_PASSED)
                .toList();
    }

    @Override
    public List<TradeSession> getStoreCompletedTrades(User user) {
        if (user == null)
            return new ArrayList<>();
        return cachedTradeSessions.stream()
                .filter(t -> t.getStoreId() != null && t.getStoreId().equals(user.getUsername()))
                .filter(t -> t.getTradeStatus() == model.domain.enumerations.TradeStatus.COMPLETED ||
                        t.getTradeStatus() == model.domain.enumerations.TradeStatus.CANCELLED ||
                        t.getTradeStatus() == model.domain.enumerations.TradeStatus.EXPIRED)
                .toList();
    }

    @Override
    public void updateTradeSessionStatus(TradeSession tradeSession) {
        if (tradeSession == null)
            return;
        // In memory, object reference is often same.
        // But finding and replacing ensures safety if new instance passed.
        for (int i = 0; i < cachedTradeSessions.size(); i++) {
            if (cachedTradeSessions.get(i).getSessionId() == tradeSession.getSessionId()) {
                cachedTradeSessions.set(i, tradeSession);
                return;
            }
        }
    }

}
