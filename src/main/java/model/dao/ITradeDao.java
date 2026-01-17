package model.dao;

import model.domain.TradeTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ITradeDao extends IDao<TradeTransaction> {
    TradeTransaction getTradeTransactionById(int id);
    void updateTransactionStatus(int id, String status);
    List<TradeTransaction> getUserTradeTransactions(String userId);
    List<TradeTransaction> getStoreTradeScheduledTransactions(String userId, String tradeId);//ritorna la lista degli scambi programmati
    List<TradeTransaction> getUserTradeTransactions(String userId, String tradeId);

    // New: ritorna tutti gli scambi conclusi (COMPLETED o CANCELLED) per l'utente
    List<TradeTransaction> getUserCompletedTrades(String userId);

    // Cerca una trade transaction dati proposer/receiver e data (se presente) - utile per mappare Proposal->TradeTransaction
    Optional<TradeTransaction> findByParticipantsAndDate(String proposerId, String receiverId, LocalDateTime tradeDate);

    /**
     * Cerca una TradeTransaction tramite i session code dei due collezionisti.
     * Restituisce la transazione se trovata, altrimenti null.
     */
    TradeTransaction getTradeTransactionBySessionCodes(int proposerCode, int receiverCode);

    /**
     * Restituisce la lista degli scambi in corso (inspection phase o inspection passed) per lo store.
     */
    List<TradeTransaction> getStoreTradeInProgressTransactions(String storeId);
}
