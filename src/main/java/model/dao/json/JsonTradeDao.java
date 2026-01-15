package model.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import config.DatabaseConfig;
import exception.DataPersistenceException;
import model.dao.ITradeDao;
import model.domain.TradeTransaction;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonTradeDao implements ITradeDao {
    private static final Logger LOGGER = Logger.getLogger(JsonTradeDao.class.getName());
    private final String jsonFilePath;
    private final Gson gson;
    private final Map<Integer, TradeTransaction> tradesById;
    private final AtomicInteger idGenerator;

    public JsonTradeDao() {
        this(DatabaseConfig.JSON_DIR + File.separator + "trades.json");
    }

    public JsonTradeDao(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new com.google.gson.TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
                        if (value == null) out.nullValue(); else out.value(value.toString());
                    }

                    @Override
                    public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) { in.nextNull(); return null; }
                        return LocalDateTime.parse(in.nextString());
                    }
                })
                .create();

        this.tradesById = new ConcurrentHashMap<>();
        this.idGenerator = new AtomicInteger(0);

        initializeFile();
        loadFromJson();
    }

    private void initializeFile() {
        File file = new File(jsonFilePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        if (!file.exists()) saveToJson();
    }

    private void loadFromJson() {
        File file = new File(jsonFilePath);
        if (!file.exists()) return;
        try (Reader r = new FileReader(file)) {
            Type listType = new TypeToken<List<TradeTransaction>>(){}.getType();
            List<TradeTransaction> list = gson.fromJson(r, listType);
            if (list != null) {
                int max = 0;
                for (TradeTransaction t : list) {
                    tradesById.put(t.getTransactionId(), t);
                    if (t.getTransactionId() > max) max = t.getTransactionId();
                }
                idGenerator.set(max);
                LOGGER.log(Level.INFO, "Loaded {0} trade transactions from JSON", list.size());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading trades JSON, starting empty", e);
            saveToJson();
        }
    }

    private void saveToJson() {
        try (Writer w = new FileWriter(jsonFilePath)) {
            List<TradeTransaction> list = new ArrayList<>(tradesById.values());
            gson.toJson(list, w);
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to save trades to " + jsonFilePath, e);
        }
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
            saveToJson();
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Unknown trade status: {0}", status);
        }
    }

    @Override
    public List<TradeTransaction> getUserTradeTransactions(String userId) {
        List<TradeTransaction> result = new ArrayList<>();
        for (TradeTransaction t : tradesById.values()) {
            if (t == null) continue;
            if (userId != null && (userId.equals(t.getProposerId()) || userId.equals(t.getReceiverId()))) result.add(t);
        }
        return result;
    }

    @Override
    public Optional<TradeTransaction> get(long id) {
        TradeTransaction t = tradesById.get((int) id);
        return Optional.ofNullable(t);
    }

    @Override
    public void save(TradeTransaction tradeTransaction) {
        if (tradeTransaction == null) throw new IllegalArgumentException("TradeTransaction cannot be null");
        if (tradeTransaction.getTransactionId() == 0) {
            int id = idGenerator.incrementAndGet();
            tradeTransaction.setTransactionId(id);
        }
        tradesById.put(tradeTransaction.getTransactionId(), tradeTransaction);
        saveToJson();
        LOGGER.log(Level.INFO, "Saved trade transaction {0}", tradeTransaction.getTransactionId());
    }

    @Override
    public void update(TradeTransaction tradeTransaction, String[] params) {
        if (tradeTransaction == null || tradeTransaction.getTransactionId() == 0) throw new IllegalArgumentException("Invalid trade transaction");
        tradesById.put(tradeTransaction.getTransactionId(), tradeTransaction);
        saveToJson();
        LOGGER.log(Level.INFO, "Updated trade transaction {0}", tradeTransaction.getTransactionId());
    }

    @Override
    public void delete(TradeTransaction tradeTransaction) {
        if (tradeTransaction == null || tradeTransaction.getTransactionId() == 0) throw new IllegalArgumentException("Invalid trade transaction");
        tradesById.remove(tradeTransaction.getTransactionId());
        saveToJson();
        LOGGER.log(Level.INFO, "Deleted trade transaction {0}", tradeTransaction.getTransactionId());
    }

}
