package model.dao.demo;

import model.dao.ITradeDao;
import model.domain.TradeTransaction;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DemoTradeDao implements ITradeDao {
    private static final Logger LOGGER = Logger.getLogger(DemoTradeDao.class.getName());
    private final Map<Integer, TradeTransaction> tradesById = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public DemoTradeDao() {
        // starts empty in-memory
    }

    // Allow pre-loading a collection of trades for demo/test purposes (volatile in-memory)
    public void loadFromCollection(Collection<TradeTransaction> initialTrades) {
        tradesById.clear();
        if (initialTrades == null) {
            LOGGER.log(Level.INFO, "DemoTradeDao.loadFromCollection: loaded 0 trades (null input)");
            return;
        }
        int max = 0;
        int count = 0;
        for (TradeTransaction t : initialTrades) {
            if (t == null) continue;
            if (t.getTransactionId() == 0) {
                int id = idGenerator.incrementAndGet();
                t.setTransactionId(id);
            }
            tradesById.put(t.getTransactionId(), t);
            if (t.getTransactionId() > max) max = t.getTransactionId();
            count++;
        }
        if (max > 0) idGenerator.set(max);
        LOGGER.log(Level.INFO, "DemoTradeDao.loadFromCollection: loaded {0} trade transactions into memory", count);
    }

    @Override
    public TradeTransaction getTradeTransactionById(int id) {
        return tradesById.get(id);
    }

    @Override
    public void updateTransactionStatus(int id, String status) {
        TradeTransaction t = tradesById.get(id);
        if (t == null) return;
        try {
            model.domain.enumerations.TradeStatus ts = model.domain.enumerations.TradeStatus.valueOf(status);
            t.updateTradeStatus(ts);
            tradesById.put(id, t);
            LOGGER.log(Level.INFO, "DemoTradeDao.updateTransactionStatus: updated transaction {0} to status {1}", new Object[]{id, status});
        } catch (IllegalArgumentException _) {
            LOGGER.log(Level.WARNING, "DemoTradeDao.updateTransactionStatus: unknown status {0} for transaction {1}", new Object[]{status, id});
        }
    }

    @Override
    public List<TradeTransaction> getUserTradeTransactions(String userId) {
        List<TradeTransaction> result = new ArrayList<>();
        if (userId == null) return result;
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue; // single continue
            if (userId.equals(t.getProposerId()) || userId.equals(t.getReceiverId())) result.add(t);
        }
        return result;
    }

    @Override
    public List<TradeTransaction> getStoreTradeScheduledTransactions(String userId, String tradeId) {
        List<TradeTransaction> result = new ArrayList<>();
        if (userId == null) return result;
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue; // single continue per loop
            if (userId.equals(t.getStoreId())) {
                model.domain.enumerations.TradeStatus s = t.getTradeStatus();
                if (s != model.domain.enumerations.TradeStatus.COMPLETED
                        && s != model.domain.enumerations.TradeStatus.CANCELLED) {
                    result.add(t);
                }
            }
        }
        return result;
    }

    @Override
    public List<TradeTransaction> getUserTradeTransactions(String userId, String tradeId) {
        List<TradeTransaction> result = new ArrayList<>();
        if (userId == null) return result;
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue; // single continue
            if ((userId.equals(t.getProposerId()) || userId.equals(t.getReceiverId())) && String.valueOf(t.getTransactionId()).equals(tradeId)) {
                result.add(t);
            }
        }
        return result;
    }

    @Override
    public List<TradeTransaction> getUserCompletedTrades(String userId) {
        List<TradeTransaction> result = new ArrayList<>();
        if (userId == null) return result;
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue; // single continue per loop
            if (isCompletedForUser(t, userId)) {
                result.add(t);
            }
        }
        // Diagnostic logging similar to JsonTradeDao
        logUserCompletedTrades(result, userId);
        return result;
    }

    // Helper: determine whether a trade is completed/canceled and involves the given user
    private boolean isCompletedForUser(TradeTransaction t, String userId) {
        boolean involved = (userId.equals(t.getProposerId()) || userId.equals(t.getReceiverId()));
        if (!involved) return false;
        model.domain.enumerations.TradeStatus s = t.getTradeStatus();
        return s == model.domain.enumerations.TradeStatus.COMPLETED || s == model.domain.enumerations.TradeStatus.CANCELLED;
    }

    // Helper: centralized logging for completed trades
    private void logUserCompletedTrades(List<TradeTransaction> result, String userId) {
        try {
            if (result.isEmpty()) {
                LOGGER.info(() -> "DemoTradeDao.getUserCompletedTrades: found 0 completed trades for user=" + userId);
            } else {
                StringBuilder ids = new StringBuilder();
                for (TradeTransaction tt : result) ids.append(tt.getTransactionId()).append(',');
                String idsStr = ids.toString();
                LOGGER.info(() -> "DemoTradeDao.getUserCompletedTrades: found " + result.size() + " completed trades for user=" + userId + " ids=" + idsStr);
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "DemoTradeDao.getUserCompletedTrades logging failed: " + ex.getMessage());
        }
    }

    @Override
    public Optional<TradeTransaction> get(long id) {
        return Optional.ofNullable(tradesById.get((int) id));
    }

    @Override
    public void save(TradeTransaction tradeTransaction) {
        if (tradeTransaction == null) throw new IllegalArgumentException("TradeTransaction cannot be null");
        if (tradeTransaction.getTransactionId() == 0) {
            int id = idGenerator.incrementAndGet();
            tradeTransaction.setTransactionId(id);
        }
        tradesById.put(tradeTransaction.getTransactionId(), tradeTransaction);
        LOGGER.log(Level.INFO, "DemoTradeDao.save: saved trade transaction {0}", tradeTransaction.getTransactionId());
    }

    @Override
    public void update(TradeTransaction tradeTransaction, String[] params) {
        if (tradeTransaction == null || tradeTransaction.getTransactionId() == 0) throw new IllegalArgumentException("Invalid trade transaction");
        tradesById.put(tradeTransaction.getTransactionId(), tradeTransaction);
        LOGGER.log(Level.INFO, "DemoTradeDao.update: updated trade transaction {0}", tradeTransaction.getTransactionId());
    }

    @Override
    public void delete(TradeTransaction tradeTransaction) {
        if (tradeTransaction == null || tradeTransaction.getTransactionId() == 0) throw new IllegalArgumentException("Invalid trade transaction");
        tradesById.remove(tradeTransaction.getTransactionId());
        LOGGER.log(Level.INFO, "DemoTradeDao.delete: deleted trade transaction {0}", tradeTransaction.getTransactionId());
    }

    @Override
    public Optional<TradeTransaction> findByParticipantsAndDate(String proposerId, String receiverId, LocalDateTime tradeDate) {
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue; // single continue
            if (matchesParticipantsAndDate(t, proposerId, receiverId, tradeDate)) return Optional.of(t);
        }
        return Optional.empty();
    }

    private boolean matchesParticipantsAndDate(TradeTransaction t, String proposerId, String receiverId, LocalDateTime tradeDate) {
        if (proposerId != null && !proposerId.equals(t.getProposerId())) return false;
        if (receiverId != null && !receiverId.equals(t.getReceiverId())) return false;
        if (tradeDate != null) {
            if (t.getTradeDate() == null) return false;
            return t.getTradeDate().toLocalDate().equals(tradeDate.toLocalDate());
        }
        return true;
    }

    @Override
    public TradeTransaction getTradeTransactionBySessionCodes(int proposerCode, int receiverCode) {
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue;
            if (t.getProposerSessionCode() == proposerCode && t.getReceiverSessionCode() == receiverCode) {
                return t;
            }
        }
        return null;
    }

    @Override
    public List<TradeTransaction> getStoreTradeInProgressTransactions(String storeId) {
        List<TradeTransaction> result = new ArrayList<>();
        if (storeId == null) return result;
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue; // single continue per loop
            if (storeId.equals(t.getStoreId())) {
                model.domain.enumerations.TradeStatus s = t.getTradeStatus();
                if (s == model.domain.enumerations.TradeStatus.INSPECTION_PHASE
                        || s == model.domain.enumerations.TradeStatus.INSPECTION_PASSED) {
                    result.add(t);
                }
            }
        }
        return result;
    }

    @Override
    public List<TradeTransaction> getStoreCompletedTrades(String storeId) {
        List<TradeTransaction> result = new ArrayList<>();
        if (storeId == null) return result;
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue; // single continue per loop
            if (storeId.equals(t.getStoreId())) {
                model.domain.enumerations.TradeStatus s = t.getTradeStatus();
                if (s == model.domain.enumerations.TradeStatus.COMPLETED || s == model.domain.enumerations.TradeStatus.CANCELLED) {
                    result.add(t);
                }
            }
        }
        try {
            if (result.isEmpty()) LOGGER.info(() -> "DemoTradeDao.getStoreCompletedTrades: found 0 completed trades for store=" + storeId);
            else {
                StringBuilder ids = new StringBuilder();
                for (TradeTransaction tt : result) ids.append(tt.getTransactionId()).append(',');
                String idsStr = ids.toString();
                LOGGER.info(() -> "DemoTradeDao.getStoreCompletedTrades: found " + result.size() + " completed trades for store=" + storeId + " ids=" + idsStr);
            }
        } catch (Exception ex) { LOGGER.fine(() -> "DemoTradeDao.getStoreCompletedTrades logging failed: " + ex.getMessage()); }
        return result;
    }

}
