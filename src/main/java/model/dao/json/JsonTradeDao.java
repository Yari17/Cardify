package model.dao.json;

import config.DatabaseConfig;
import model.dao.ITradeDao;
import model.domain.TradeTransaction;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonTradeDao implements ITradeDao {
    private static final Logger LOGGER = Logger.getLogger(JsonTradeDao.class.getName());
    private final String jsonFilePath;

    public JsonTradeDao() {
        this(DatabaseConfig.JSON_DIR + File.separator + "trades.json");
    }

    public JsonTradeDao(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        // ensure parent directory exists
        try {
            File f = new File(jsonFilePath);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!f.exists()) f.createNewFile();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to initialize trade JSON file: {0}", e.getMessage());
        }
    }

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
    public Optional<TradeTransaction> get(long id) {
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
