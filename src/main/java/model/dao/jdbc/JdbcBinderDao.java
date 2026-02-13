package model.dao.jdbc;

import config.DBConnector;
import model.dao.IBinderDao;
import model.domain.Binder;
import model.domain.Card;
import model.domain.CollectionItem;
import model.domain.enumerations.CardGameType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione JDBC del DAO per la gestione dei raccoglitori.
 * Gestisce la persistenza strutturata delle collezioni, mappando le relazioni
 * tra i raccoglitori (Binders) e le carte (Items) contenute in essi.
 */
public class JdbcBinderDao implements IBinderDao {
    private static final String COL_BINDER_ID = "binder_id";
    private static final String COL_SET_ID = "set_id";
    private static final String COL_SET_NAME = "set_name";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_CARD_ID = "card_id";
    private static final String COL_IMAGE_URL = "image_url";
    private static final String COL_GAME_TYPE = "game_type";

    private static final String SQL_ITEMS_QUERY = "SELECT c.card_id, c.name, c.set_id, c.image_url, c.game_type, bi.quantity "
            +
            "FROM binder_items bi JOIN cards c ON bi.card_id = c.card_id WHERE bi.binder_id = ?";

    /**
     * Recupera tutti i raccoglitori di un utente, caricando per ognuno le carte
     * contenute.
     * Effettua una doppia query (una per i raccoglitori e una ciclica per gli item)
     * per mappare
     * correttamente la struttura a lista.
     * 
     * @param owner Nome utente del proprietario.
     * @return Lista di {@link Binder} popolati con le proprie carte.
     * @throws exception.DaoException Se il caricamento fallisce.
     */
    @Override
    public List<Binder> getUserBinders(String owner) {

        List<Binder> result = new ArrayList<>();
        String sqlBinders = "SELECT binder_id, set_id, set_name FROM binders WHERE owner = ?";

        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement psBinders = conn.prepareStatement(sqlBinders)) {

                psBinders.setString(1, owner);
                try (ResultSet rsBinders = psBinders.executeQuery()) {
                    while (rsBinders.next()) {
                        int binderId = rsBinders.getInt(COL_BINDER_ID);
                        String setId = rsBinders.getString(COL_SET_ID);
                        String setName = rsBinders.getString(COL_SET_NAME);

                        List<CollectionItem> owned = new ArrayList<>();

                        // Delega il caricamento degli item a una query specializzata
                        try (PreparedStatement psItems = conn.prepareStatement(SQL_ITEMS_QUERY)) {
                            psItems.setInt(1, binderId);
                            try (ResultSet rsItems = psItems.executeQuery()) {
                                while (rsItems.next()) {
                                    String cardId = rsItems.getString(COL_CARD_ID);
                                    String cardName = rsItems.getString("name");
                                    String cardSetId = rsItems.getString(COL_SET_ID);
                                    String image = rsItems.getString(COL_IMAGE_URL);
                                    String gameTypeStr = rsItems.getString(COL_GAME_TYPE);
                                    int qty = rsItems.getInt(COL_QUANTITY);

                                    CardGameType gameType = parseGameType(gameTypeStr);

                                    Card card = new Card(cardName, cardId, cardSetId, image, gameType);
                                    owned.add(new CollectionItem(card, qty));
                                }
                            }
                        }

                        Binder b = new Binder(owner, setId, setName, owned);
                        result.add(b);
                    }
                }
            }

        } catch (SQLException e) {
            throw new exception.DaoException("Impossibile recuperare i raccoglitori dell'utente", e);
        }
        return result;
    }

    @Override
    public List<Binder> getBindersExcludingOwner(String owner) {
        List<Binder> result = new ArrayList<>();
        String sqlBinders = "SELECT binder_id, owner, set_id, set_name FROM binders WHERE owner != ?";

        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement psBinders = conn.prepareStatement(sqlBinders)) {

                psBinders.setString(1, owner);
                try (ResultSet rsBinders = psBinders.executeQuery()) {
                    while (rsBinders.next()) {
                        int binderId = rsBinders.getInt(COL_BINDER_ID);
                        String binderOwner = rsBinders.getString("owner");
                        String setId = rsBinders.getString(COL_SET_ID);
                        String setName = rsBinders.getString(COL_SET_NAME);

                        List<CollectionItem> owned = new ArrayList<>();

                        try (PreparedStatement psItems = conn.prepareStatement(SQL_ITEMS_QUERY)) {
                            psItems.setInt(1, binderId);
                            try (ResultSet rsItems = psItems.executeQuery()) {
                                while (rsItems.next()) {
                                    String cardId = rsItems.getString(COL_CARD_ID);
                                    String cardName = rsItems.getString("name");
                                    String cardSetId = rsItems.getString(COL_SET_ID);
                                    String image = rsItems.getString(COL_IMAGE_URL);
                                    String gameTypeStr = rsItems.getString(COL_GAME_TYPE);
                                    int qty = rsItems.getInt(COL_QUANTITY);

                                    CardGameType gameType = parseGameType(gameTypeStr);

                                    Card card = new Card(cardName, cardId, cardSetId, image, gameType);
                                    owned.add(new CollectionItem(card, qty));
                                }
                            }
                        }

                        Binder b = new Binder(binderOwner, setId, setName, owned);
                        result.add(b);
                    }
                }
            }

        } catch (SQLException e) {
            throw new exception.DaoException("Failed to get binders excluding owner", e);
        }
        return result;
    }

    @Override
    public Binder getBinderByOwnerAndSet(String owner, String setId) {
        String sqlBinder = "SELECT binder_id, set_name FROM binders WHERE owner = ? AND set_id = ?";

        try (Connection conn = DBConnector.getConnection();
                PreparedStatement psBinder = conn.prepareStatement(sqlBinder)) {

            psBinder.setString(1, owner);
            psBinder.setString(2, setId);

            try (ResultSet rsBinder = psBinder.executeQuery()) {
                if (rsBinder.next()) {
                    int binderId = rsBinder.getInt(COL_BINDER_ID);
                    String setName = rsBinder.getString(COL_SET_NAME);

                    List<CollectionItem> owned = new ArrayList<>();
                    try (PreparedStatement psItems = conn.prepareStatement(SQL_ITEMS_QUERY)) {
                        psItems.setInt(1, binderId);
                        try (ResultSet rsItems = psItems.executeQuery()) {
                            while (rsItems.next()) {
                                String cardId = rsItems.getString(COL_CARD_ID);
                                String cardName = rsItems.getString("name");
                                String cardSetId = rsItems.getString(COL_SET_ID); // Use column name from query
                                String image = rsItems.getString(COL_IMAGE_URL);
                                String gameTypeStr = rsItems.getString(COL_GAME_TYPE);
                                int qty = rsItems.getInt(COL_QUANTITY);

                                CardGameType gameType = parseGameType(gameTypeStr);

                                Card card = new Card(cardName, cardId, cardSetId, image, gameType);
                                owned.add(new CollectionItem(card, qty));
                            }
                        }
                    }
                    return new Binder(owner, setId, setName, owned);
                }
            }
        } catch (SQLException e) {
            throw new exception.DaoException("Failed to get binder by owner and set", e);
        }
        return null;
    }

    @Override
    public Binder findOrCreateBinder(String owner, String setId, String setName) {
        Binder binder = getBinderByOwnerAndSet(owner, setId);
        if (binder == null) {
            binder = new Binder(owner, setId, setName, new ArrayList<>());
            createBinder(owner, binder);
        }
        return binder;
    }

    /**
     * Crea un nuovo raccoglitore e inserisce in batch tutte le carte associate.
     * Utilizza RETURN_GENERATED_KEYS per ottenere l'ID del nuovo raccoglitore
     * necessario
     * per mappare gli item nella tabella di join.
     * 
     * @param owner  Proprietario.
     * @param binder L'oggetto {@link Binder} da persistere.
     * @throws exception.DaoException In caso di errore SQL durante l'inserimento
     *                                batch.
     */
    @Override
    public void createBinder(String owner, Binder binder) {

        final String sqlInsertBinder = "INSERT INTO binders (owner, set_id, set_name) VALUES (?, ?, ?)";
        final String sqlInsertItem = "INSERT INTO binder_items (binder_id, card_id, quantity) VALUES (?, ?, ?)";

        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement psBinder = conn.prepareStatement(sqlInsertBinder,
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {

                psBinder.setString(1, owner);
                psBinder.setString(2, binder.getSetID());
                psBinder.setString(3, binder.getSetName());

                int affected = psBinder.executeUpdate();
                if (affected == 0) {
                    return;
                }

                int binderId;
                try (ResultSet generated = psBinder.getGeneratedKeys()) {
                    if (generated.next()) {
                        binderId = generated.getInt(1);
                    } else {
                        return;
                    }
                }

                List<CollectionItem> items = binder.getOwnedCards();
                if (items != null && !items.isEmpty()) {
                    // Delega l'inserimento multiplo alla modalità batch per ottimizzare le
                    // performance
                    try (PreparedStatement psItem = conn.prepareStatement(sqlInsertItem)) {
                        psItem.setInt(1, binderId);
                        for (CollectionItem ci : items) {
                            if (ci == null || ci.getCard() == null)
                                continue;
                            psItem.setString(2, ci.getCard().getCardID());
                            psItem.setInt(3, ci.getQuantity());
                            psItem.addBatch();
                        }
                        psItem.executeBatch();
                    }
                }
            }

        } catch (SQLException e) {
            throw new exception.DaoException("Salvataggio batch del raccoglitore fallito", e);
        }
    }

    @Override
    public void deleteBinder(String owner, Binder binder) {
        final String sqlDeleteItems = "DELETE FROM binder_items WHERE binder_id = (SELECT binder_id FROM binders WHERE owner = ? AND set_id = ?)";
        final String sqlDeleteBinder = "DELETE FROM binders WHERE owner = ? AND set_id = ?";

        try {
            Connection conn = DBConnector.getConnection();
            // First delete all items in the binder
            try (PreparedStatement psItems = conn.prepareStatement(sqlDeleteItems)) {
                psItems.setString(1, owner);
                psItems.setString(2, binder.getSetID());
                psItems.executeUpdate();
            }

            // Then delete the binder itself
            try (PreparedStatement psBinder = conn.prepareStatement(sqlDeleteBinder)) {
                psBinder.setString(1, owner);
                psBinder.setString(2, binder.getSetID());
                psBinder.executeUpdate();
            }

        } catch (SQLException e) {
            throw new exception.DaoException("Failed to delete binder", e);
        }
    }

    @Override
    public void save(Binder binder) {
        if (binder == null)
            return;

        String sqlGetId = "SELECT binder_id FROM binders WHERE owner = ? AND set_id = ?";
        String sqlDeleteItems = "DELETE FROM binder_items WHERE binder_id = ?";
        // Use INSERT IGNORE or ON DUPLICATE KEY UPDATE to ensure card exists
        String sqlInsertCard = "INSERT INTO cards (card_id, name, set_id, image_url, game_type) VALUES (?, ?, ?, ?, ?) "
                +
                "ON DUPLICATE KEY UPDATE name=VALUES(name), image_url=VALUES(image_url)";
        String sqlInsertItem = "INSERT INTO binder_items (binder_id, card_id, quantity) VALUES (?, ?, ?)";

        try {
            Connection conn = DBConnector.getConnection();
            int binderId = -1;

            // 1. Get Binder ID
            try (PreparedStatement psGetId = conn.prepareStatement(sqlGetId)) {
                psGetId.setString(1, binder.getOwner());
                psGetId.setString(2, binder.getSetID());
                try (ResultSet rs = psGetId.executeQuery()) {
                    if (rs.next()) {
                        binderId = rs.getInt(COL_BINDER_ID);
                    } else {
                        return;
                    }
                }
            }

            // 2. Delete all existing items for this binder (easiest way to handle
            // updates/removals)
            try (PreparedStatement psDelete = conn.prepareStatement(sqlDeleteItems)) {
                psDelete.setInt(1, binderId);
                psDelete.executeUpdate();
            }

            // 3. Insert current items
            List<CollectionItem> items = binder.getOwnedCards();
            if (items != null && !items.isEmpty()) {
                // Prepare statements
                try (PreparedStatement psCard = conn.prepareStatement(sqlInsertCard);
                        PreparedStatement psItem = conn.prepareStatement(sqlInsertItem)) {

                    // Insert relation
                    psItem.setInt(1, binderId);
                    for (CollectionItem ci : items) {
                        if (ci == null || ci.getCard() == null || ci.getQuantity() <= 0)
                            continue;

                        Card c = ci.getCard();

                        // Ensure card exists in DB
                        psCard.setString(1, c.getCardID());
                        psCard.setString(2, c.getCardName());
                        psCard.setString(3, c.getCardSetID());
                        psCard.setString(4, c.getCardImage());
                        psCard.setString(5, c.getGameType() != null ? c.getGameType().name() : null);
                        psCard.addBatch();

                        psItem.setString(2, c.getCardID());
                        psItem.setInt(3, ci.getQuantity());
                        psItem.addBatch();
                    }

                    psCard.executeBatch();
                    psItem.executeBatch();
                }
            }

        } catch (SQLException e) {
            throw new exception.DaoException("Failed to save binder", e);
        }
    }

    private CardGameType parseGameType(String gameTypeStr) {
        try {
            if (gameTypeStr != null)
                return CardGameType.valueOf(gameTypeStr);
        } catch (IllegalArgumentException _) {
            // Ignora tipo gioco non valido
        }
        return null;
    }
}
