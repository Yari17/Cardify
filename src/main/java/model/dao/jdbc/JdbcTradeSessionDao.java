package model.dao.jdbc;

import model.dao.ITradeSessionDao;
import model.domain.TradeSession;
import model.domain.User;
import model.domain.Card;
import model.domain.enumerations.TradeStatus;
import config.DBConnector;
import exception.DaoException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementazione JDBC del DAO per le sessioni di scambio fisico (Trade
 * Sessions).
 * Funzionalità: Gestisce l'intero ciclo di vita dello scambio presso lo store,
 * inclusa
 * la persistenza dei dati, la gestione delle transazioni e il tracciamento
 * degli arrivi.
 * Utilità: Permette la persistenza robusta su database relazionale delle
 * sessioni di scambio,
 * garantendo l'integrità dei dati tramite transazioni SQL.
 */
public class JdbcTradeSessionDao implements ITradeSessionDao {

    /** Cache thread-safe delle sessioni di scambio indicizzate per ID. */
    private final Map<Integer, TradeSession> cache = new ConcurrentHashMap<>();

    /**
     * Salva una nuova sessione di scambio nel database.
     * Funzionalità: Inserisce i dati della sessione e le carte associate in una
     * singola transazione.
     * Utilità: Utilizzato durante la fase di prenotazione o creazione di uno
     * scambio fisico.
     * Delega l'inserimento batch delle carte al metodo helper
     * {@link #saveCards(PreparedStatement, int, List, String)}
     * per separare la logica di inserimento della sessione principale dai dettagli
     * dei singoli oggetti scambiati.
     * 
     * @param tradeSession L'oggetto sessione da persistere.
     * @throws DaoException Se si verifica un errore SQL durante l'operazione o il
     *                      rollback.
     */
    @Override
    public void saveTradeSession(TradeSession tradeSession) {
        String sqlSession = "INSERT INTO trade_sessions (proposer_id, receiver_id, store_id, status, trade_date, created_at, proposer_code, receiver_code, proposer_arrived, receiver_arrived) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlItems = "INSERT INTO trade_session_items (session_id, card_id, item_type, quantity) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnector.getConnection();
            conn.setAutoCommit(false); // Operazione transazionale

            int sessionId;
            try (PreparedStatement stmt = conn.prepareStatement(sqlSession, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, tradeSession.getProposerId());
                stmt.setString(2, tradeSession.getReceiverId());
                stmt.setString(3, tradeSession.getStoreId());
                stmt.setString(4, tradeSession.getTradeStatus().name());
                if (tradeSession.getTradeDate() != null) {
                    stmt.setTimestamp(5, Timestamp.valueOf(tradeSession.getTradeDate()));
                } else {
                    stmt.setTimestamp(5, null);
                }
                stmt.setTimestamp(6, Timestamp.valueOf(tradeSession.getCreationTimestamp()));
                stmt.setInt(7, tradeSession.getProposerSessionCode());
                stmt.setInt(8, tradeSession.getReceiverSessionCode());
                stmt.setBoolean(9, tradeSession.isProposerArrived());
                stmt.setBoolean(10, tradeSession.isReceiverArrived());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        sessionId = rs.getInt(1);
                        tradeSession.setSessionId(sessionId);
                    } else {
                        throw new SQLException("Salvataggio fallito: impossibile ottenere l'ID della sessione.");
                    }
                }
            }

            // Salvataggio degli oggetti (carte) associati
            try (PreparedStatement stmtItems = conn.prepareStatement(sqlItems)) {
                // Carte offerte
                saveCards(stmtItems, sessionId, tradeSession.getOfferedCards(), "OFFERED");
                // Carte richieste
                saveCards(stmtItems, sessionId, tradeSession.getRequestedCards(), "REQUESTED");

                stmtItems.executeBatch();
            }

            conn.commit();

            // Popola la cache dopo il commit riuscito
            cache.put(tradeSession.getSessionId(), tradeSession);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new DaoException("Errore durante il rollback della transazione", ex);
                }
            }
            throw new DaoException("Errore durante il salvataggio della sessione di scambio", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,
                            "Chiusura connessione fallita", e);
                }
            }
        }
    }

    /**
     * Metodo helper per preparare il batch di inserimento delle carte.
     * Raggruppa le carte per ID per gestire la quantità in modo aggregato.
     * 
     * @param stmt      Lo statement SQL pronto per il batch.
     * @param sessionId ID della sessione di riferimento.
     * @param cards     Lista delle carte da inserire.
     * @param type      Tipo di associazione (OFFERED o REQUESTED).
     * @throws SQLException In caso di errore SQL.
     */
    private void saveCards(PreparedStatement stmt, int sessionId, List<Card> cards, String type) throws SQLException {
        if (cards == null || cards.isEmpty())
            return;

        // Raggruppa per ID per contare le quantità
        Map<String, Integer> counts = new HashMap<>();
        for (Card c : cards) {
            counts.put(c.getCardID(), counts.getOrDefault(c.getCardID(), 0) + 1);
        }

        stmt.setInt(1, sessionId);
        stmt.setString(3, type);
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            stmt.setString(2, entry.getKey());
            stmt.setInt(4, entry.getValue());
            stmt.addBatch();
        }
    }

    /**
     * Recupera le sessioni attualmente in corso presso un determinato negozio.
     * Utilizzato dalla dashboard dello Store per visualizzare il lavoro
     * corrente.
     * Delega l'esecuzione della query al metodo helper
     * {@link #executeQueryForList(String, Object...)}
     * per centralizzare la gestione delle risorse JDBC e il mapping dei risultati.
     * 
     * @param user L'utente negozio proprietario.
     * @return Lista di sessioni di scambio in corso.
     */
    @Override
    public List<TradeSession> getStoreInProgressTrades(User user) {
        String sql = "SELECT * FROM trade_sessions WHERE store_id = ? AND status IN ('PARTIALLY_ARRIVED', 'BOTH_ARRIVED', 'INSPECTION_PHASE', 'INSPECTION_PASSED')";
        return executeQueryForList(sql, user.getUsername());
    }

    /**
     * Recupera le sessioni pianificate presso un negozio.
     * Utilizzato dalla dashboard dello Store per visualizzare le sessioni
     * pianificate.
     * Delega l'esecuzione della query al metodo helper
     * {@link #executeQueryForList(String, Object...)}
     * per garantire uniformità nel recupero dei dati.
     * 
     * @param user L'utente negozio proprietario.
     * @return Lista di sessioni pianificate.
     */
    @Override
    public List<TradeSession> getStoreScheduledTrades(User user) {
        String sql = "SELECT * FROM trade_sessions WHERE store_id = ? AND status = 'WAITING_FOR_ARRIVAL'";
        return executeQueryForList(sql, user.getUsername());
    }

    /**
     * Recupera lo storico delle sessioni completate o concluse presso un negozio.
     * Utilizzato dalla dashboard dello Store per visualizzare le sessioni
     * completate o concluse.
     * Delega l'esecuzione della query al metodo helper
     * {@link #executeQueryForList(String, Object...)}
     * per centralizzare la logica di accesso ai dati.
     * 
     * @param user L'utente negozio proprietario.
     * @return Lista di sessioni archiviate.
     */
    @Override
    public List<TradeSession> getStoreCompletedTrades(User user) {
        String sql = "SELECT * FROM trade_sessions WHERE store_id = ? AND status IN ('COMPLETED', 'CANCELLED', 'EXPIRED')";
        return executeQueryForList(sql, user.getUsername());
    }

    /**
     * Metodo helper generico per l'esecuzione di query SELECT che restituiscono una
     * lista.
     * Delega la trasformazione di ogni riga del ResultSet in un oggetto di dominio
     * al metodo
     * {@link #mapResultSetToTradeSession(ResultSet, Connection)}, mantenendo il
     * codice pulito e modulare.
     * 
     * @param sql    Query SQL con parametri posizionali.
     * @param params Valori dei parametri da impostare nel PreparedStatement.
     * @return Una lista di oggetti {@link TradeSession}.
     * @throws DaoException In caso di errori di accesso ai dati.
     */
    private List<TradeSession> executeQueryForList(String sql, Object... params) {
        List<TradeSession> list = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TradeSession session = mapResultSetToTradeSession(rs, conn);
                    list.add(session);
                    // Aggiorna la cache per ogni sessione caricata
                    cache.put(session.getSessionId(), session);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Esecuzione query fallita per le sessioni di scambio", e);
        }
        return list;
    }

    /**
     * Trasforma una riga del ResultSet in un oggetto TradeSession.
     * Funzionalità: Estrae i campi principali e ricostruisce gli oggetti Value
     * Object (Participants, Details).
     * Delega il caricamento delle liste di carte (offerte e richieste) al metodo
     * helper {@link #loadCards(Connection, int, String)}
     * per gestire la relazione uno-a-molti in modo isolato.
     * 
     * @param rs   Il ResultSet puntato alla riga corrente.
     * @param conn La connessione attiva per caricare le carte associate.
     * @return Un'istanza popolata di {@link TradeSession}.
     * @throws SQLException In caso di nomi colonna errati o problemi di
     *                      connessione.
     */
    private TradeSession mapResultSetToTradeSession(ResultSet rs, Connection conn) throws SQLException {
        int sessionId = rs.getInt("session_id");
        String proposerId = rs.getString("proposer_id");
        String receiverId = rs.getString("receiver_id");
        String storeId = rs.getString("store_id");
        String statusStr = rs.getString("status");
        TradeStatus status = TradeStatus.valueOf(statusStr);
        Timestamp tradeDateTs = rs.getTimestamp("trade_date");
        LocalDateTime tradeDate = (tradeDateTs != null) ? tradeDateTs.toLocalDateTime() : null;

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (createdAtTs != null) ? createdAtTs.toLocalDateTime() : null;

        // Caricamento asincrono/differito delle carte associate
        List<Card> offered = loadCards(conn, sessionId, "OFFERED");
        List<Card> requested = loadCards(conn, sessionId, "REQUESTED");

        TradeSession.TradeParticipants participants = new TradeSession.TradeParticipants(proposerId, receiverId,
                storeId);
        TradeSession.TradeDetails details = new TradeSession.TradeDetails(createdAt, tradeDate, offered, requested);

        TradeSession session = new TradeSession(sessionId, status, participants, details);
        session.setProposerSessionCode(rs.getInt("proposer_code"));
        session.setReceiverSessionCode(rs.getInt("receiver_code"));
        session.setProposerArrived(rs.getBoolean("proposer_arrived"));
        session.setReceiverArrived(rs.getBoolean("receiver_arrived"));
        return session;
    }

    /**
     * Carica dal database le carte associate a una sessione tramite join con la
     * tabella cards.
     * Funzionalità: Recupera i dettagli completi delle carte e ne gestisce la
     * quantità.
     * 
     * @param conn      Connessione attiva.
     * @param sessionId ID della sessione.
     * @param type      Tipo di set di carte (OFFERED o REQUESTED).
     * @return Lista di oggetti {@link Card}.
     * @throws SQLException In caso di errore SQL.
     */
    private List<Card> loadCards(Connection conn, int sessionId, String type) throws SQLException {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT c.card_id, c.name, c.set_id, c.image_url, c.game_type, tsi.quantity " +
                "FROM trade_session_items tsi " +
                "JOIN cards c ON tsi.card_id = c.card_id " +
                "WHERE tsi.session_id = ? AND tsi.item_type = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setString(2, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("card_id");
                    String name = rs.getString("name");
                    String setId = rs.getString("set_id");
                    String img = rs.getString("image_url");
                    String gameTypeStr = rs.getString("game_type");
                    int quantity = rs.getInt("quantity");

                    model.domain.enumerations.CardGameType gameType = model.domain.enumerations.CardGameType
                            .valueOf(gameTypeStr);
                    Card card = new Card(name, id, setId, img, gameType);

                    // Gestione della quantità: aggiunge più istanze per mantenere compatibilità con
                    // la lista domini
                    for (int i = 0; i < quantity; i++) {
                        cards.add(card);
                    }
                }
            }
        }
        return cards;
    }

    /**
     * Recupera una singola sessione di scambio tramite il suo identificativo
     * univoco.
     * Funzionalità: Carica l'intera struttura dati di una sessione specifica.
     * Utilità: Usato per visualizzare i dettagli di una transazione singola.
     * Delega la logica di ricerca al metodo helper
     * {@link #executeQueryForList(String, Object...)}.
     * 
     * @param id L'identificativo unico della sessione.
     * @return L'oggetto {@link TradeSession} se trovato, null altrimenti.
     */
    @Override
    public TradeSession getTradeSessionById(int id) {
        // Controlla prima la cache
        TradeSession cached = cache.get(id);
        if (cached != null) {
            return cached;
        }

        String sql = "SELECT * FROM trade_sessions WHERE session_id = ?";
        List<TradeSession> results = executeQueryForList(sql, id);
        TradeSession session = results.isEmpty() ? null : results.getFirst();

        // Popola la cache se trovato
        if (session != null) {
            cache.put(id, session);
        }
        return session;
    }

    /**
     * Recupera tutte le sessioni associate a un utente (collezionista), sia come
     * proponente che come ricevente.
     * Funzionalità: Fornisce l'elenco completo delle attività di scambio
     * dell'utente.
     * Utilità: Utilizzato per popolare la dashboard "I miei scambi" dell'utente.
     * Delega l'esecuzione della query al metodo helper
     * {@link #executeQueryForList(String, Object...)}.
     * 
     * @param user L'utente collezionista.
     * @return Lista di sessioni associate all'utente.
     */
    @Override
    public List<TradeSession> getUserTradeSessions(User user) {

        String sql = "SELECT * FROM trade_sessions WHERE proposer_id = ? OR receiver_id = ?";
        List<TradeSession> sessions = executeQueryForList(sql, user.getUsername(), user.getUsername());

        // Logging per evidenziare se ci sono errori
        if (sessions.isEmpty()) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .info("Nessuna sessione di scambio trovata per l'utente: " + user.getUsername());
        } else {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .info(() -> "Trovate " + sessions.size() + " sessioni per l'utente: " + user.getUsername());
        }

        return sessions;
    }

    /**
     * Aggiorna lo stato e i codici di arrivo di una sessione esistente.
     * Funzionalità: Modifica permanentemente lo stato corrente, i codici sessione e
     * i flag di arrivo.
     * Utilità: Fondamentale per far avanzare lo stato dello scambio (es. da
     * programmato ad arrivato).
     * 
     * @param tradeSession L'oggetto sessione con i dati aggiornati.
     * @throws DaoException In caso di errori SQL durante l'aggiornamento.
     */
    @Override
    public void updateTradeSessionStatus(TradeSession tradeSession) {
        String sql = "UPDATE trade_sessions SET status = ?, proposer_code = ?, receiver_code = ?, proposer_arrived = ?, receiver_arrived = ? WHERE session_id = ?";
        try (Connection conn = DBConnector.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tradeSession.getTradeStatus().name());
            stmt.setInt(2, tradeSession.getProposerSessionCode());
            stmt.setInt(3, tradeSession.getReceiverSessionCode());
            stmt.setBoolean(4, tradeSession.isProposerArrived());
            stmt.setBoolean(5, tradeSession.isReceiverArrived());
            stmt.setInt(6, tradeSession.getSessionId());

            stmt.executeUpdate();

            // Aggiorna la cache dopo l'update riuscito
            cache.put(tradeSession.getSessionId(), tradeSession);
        } catch (SQLException e) {
            throw new DaoException("Errore durante l'aggiornamento dello stato della sessione di scambio", e);
        }
    }

}
