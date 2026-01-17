package model.dao;

import model.dao.json.JsonTradeDao;
import model.domain.TradeTransaction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonTradeDaoCompletionTest {

    @Test
    public void jsonDao_shouldReturnCompletedTradesForUser1() {
        JsonTradeDao dao = new JsonTradeDao();
        List<TradeTransaction> completed = dao.getUserCompletedTrades("user1");
        boolean found = completed.stream().anyMatch(tx -> tx.getTransactionId() == 1214693586);
        assertTrue(found, "Expected to find transaction 1214693586 among completed trades for user1");
    }
}
