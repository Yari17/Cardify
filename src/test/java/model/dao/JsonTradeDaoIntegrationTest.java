package model.dao;

import model.dao.json.JsonTradeDao;
import model.domain.Card;
import model.domain.TradeTransaction;
import model.domain.enumerations.CardGameType;
import model.domain.enumerations.TradeStatus;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonTradeDaoIntegrationTest {

    @Test
    void saveTrade_persistsEntryInJsonFile() throws Exception {
        File tmp = Files.createTempFile("trades-test", ".json").toFile();
        tmp.deleteOnExit();

        JsonTradeDao dao = new JsonTradeDao(tmp.getAbsolutePath());
        Card c = new Card("id1","n","http://img", CardGameType.POKEMON);
        c.setQuantity(1);
        TradeTransaction.TradeParticipants participants = new TradeTransaction.TradeParticipants("user1", "user2", "storeX");
        TradeTransaction.TradeDetails details = new TradeTransaction.TradeDetails(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            List.of(c),
            List.of()
        );
        TradeTransaction tx = new TradeTransaction(0, TradeStatus.WAITING_FOR_ARRIVAL, participants, details);
        dao.save(tx);

        // reload from file to ensure it's persisted
        JsonTradeDao dao2 = new JsonTradeDao(tmp.getAbsolutePath());
        var opt = dao2.get(tx.getTransactionId());
        assertTrue(opt.isPresent());
        TradeTransaction loaded = opt.get();
        assertEquals(tx.getProposerId(), loaded.getProposerId());
    }
}
