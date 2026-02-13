package model.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import config.DBConnector;
import exception.DaoException;
import model.dao.IProposalDao;
import model.domain.CollectionItem;
import model.domain.Proposal;
import model.domain.User;
import model.domain.Card;
import model.domain.enumerations.CardGameType;
import model.domain.enumerations.ProposalStatus;

/**
 * Implementazione JDBC del DAO per le proposte di scambio.
 * Include una logica di migrazione automatica per assicurare la presenza delle
 * colonne necessarie
 * (es. quantità e tipo item) nel database.
 */
public class JdbcProposalDao implements IProposalDao {
    static {
        checkAndMigrateSchema();
    }

    /**
     * Verifica e aggiorna lo schema del database all'avvio del DAO.
     * Assicura che le colonne 'quantity' e 'item_type' esistano nella tabella
     * 'proposal_items'.
     */
    private static void checkAndMigrateSchema() {
        // Garantisce la presenza delle colonne necessarie per la gestione avanzata
        // dello scambio
        try {
            Connection conn = DBConnector.getConnection();
            try (java.sql.Statement stmt = conn.createStatement()) {
                executeSchemaUpdate(stmt, "ALTER TABLE proposal_items ADD COLUMN quantity INT DEFAULT 1",
                        "Colonna 'quantity' aggiunta a proposal_items");
                executeSchemaUpdate(stmt,
                        "ALTER TABLE proposal_items ADD COLUMN item_type VARCHAR(20) DEFAULT 'OFFERED'",
                        "Colonna 'item_type' aggiunta a proposal_items");
            }
        } catch (java.sql.SQLException e) {
            throw new DaoException("Migrazione dello schema fallita", e);
        }
    }

    private static void executeSchemaUpdate(java.sql.Statement stmt, String sql, String successMsg) {
        try {
            stmt.execute(sql);
            java.util.logging.Logger.getLogger(JdbcProposalDao.class.getName())
                    .info(successMsg);
        } catch (java.sql.SQLException _) {
            // Ignore if exists
        }
    }

    @Override
    public Optional<Proposal> getById(String proposalId) {
        if (proposalId == null)
            return Optional.empty();

        // Note: Removed asked_item_id from SELECT
        String sqlProposal = "SELECT " +
                "p.id, p.status, p.scheduled_at, p.created_at, " +
                "prop.username AS prop_un, prop.password AS prop_pw, prop.userType AS prop_type, " +
                "recv.username AS recv_un, recv.password AS recv_pw, recv.userType AS recv_type, " +
                "meet.username AS meet_un, meet.password AS meet_pw, meet.userType AS meet_type " +
                "FROM proposals p " +
                "JOIN users prop ON p.proposer_username = prop.username " +
                "JOIN users recv ON p.receiver_username = recv.username " +
                "LEFT JOIN users meet ON p.meeting_store_username = meet.username " +
                "WHERE p.id = ?";

        String sqlItems = "SELECT c.card_id, c.name, c.set_id, c.image_url, c.game_type, pi.quantity, pi.item_type " +
                "FROM proposal_items pi " +
                "JOIN cards c ON pi.item_id = c.card_id " +
                "WHERE pi.proposal_id = ?";

        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement psProp = conn.prepareStatement(sqlProposal)) {
                psProp.setString(1, proposalId);
                try (ResultSet rsProp = psProp.executeQuery()) {
                    if (rsProp.next()) {
                        return Optional.of(mapResultSetToProposal(rsProp, conn, sqlItems));
                    }
                }
            }
        } catch (Exception e) {
            throw new DaoException("Failed to get proposal by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Proposal> getSentPendingProposal(User user) {
        if (user == null)
            return new ArrayList<>();
        return getProposalsByQuery("SELECT " +
                "p.id, p.status, p.scheduled_at, p.created_at, " +
                "prop.username AS prop_un, prop.password AS prop_pw, prop.userType AS prop_type, " +
                "recv.username AS recv_un, recv.password AS recv_pw, recv.userType AS recv_type, " +
                "meet.username AS meet_un, meet.password AS meet_pw, meet.userType AS meet_type " +
                "FROM proposals p " +
                "JOIN users prop ON p.proposer_username = prop.username " +
                "JOIN users recv ON p.receiver_username = recv.username " +
                "LEFT JOIN users meet ON p.meeting_store_username = meet.username " +
                "WHERE p.proposer_username = ? AND p.status = 'PENDING'", user.getUsername());
    }

    @Override
    public List<Proposal> getReceivedPendingProposals(User user) {
        if (user == null)
            return new ArrayList<>();
        return getProposalsByQuery("SELECT " +
                "p.id, p.status, p.scheduled_at, p.created_at, " +
                "prop.username AS prop_un, prop.password AS prop_pw, prop.userType AS prop_type, " +
                "recv.username AS recv_un, recv.password AS recv_pw, recv.userType AS recv_type, " +
                "meet.username AS meet_un, meet.password AS meet_pw, meet.userType AS meet_type " +
                "FROM proposals p " +
                "JOIN users prop ON p.proposer_username = prop.username " +
                "JOIN users recv ON p.receiver_username = recv.username " +
                "LEFT JOIN users meet ON p.meeting_store_username = meet.username " +
                "WHERE p.receiver_username = ? AND p.status = 'PENDING'", user.getUsername());
    }

    @Override
    public List<Proposal> getCompletedProposals(User user) {
        if (user == null)
            return new ArrayList<>();
        // Note: For completed proposals, logic is similar.
        String sql = "SELECT " +
                "p.id, p.status, p.scheduled_at, p.created_at, " +
                "prop.username AS prop_un, prop.password AS prop_pw, prop.userType AS prop_type, " +
                "recv.username AS recv_un, recv.password AS recv_pw, recv.userType AS recv_type, " +
                "meet.username AS meet_un, meet.password AS meet_pw, meet.userType AS meet_type " +
                "FROM proposals p " +
                "JOIN users prop ON p.proposer_username = prop.username " +
                "JOIN users recv ON p.receiver_username = recv.username " +
                "LEFT JOIN users meet ON p.meeting_store_username = meet.username " +
                "WHERE (p.proposer_username = ? OR p.receiver_username = ?) " +
                "AND (p.status = 'ACCEPTED' OR p.status = 'REJECTED' OR p.status = 'EXPIRED')";

        return getProposalsByQuery(sql, user.getUsername(), user.getUsername());
    }

    private List<Proposal> getProposalsByQuery(String sql, String... params) {
        List<Proposal> list = new ArrayList<>();
        String sqlItems = "SELECT c.card_id, c.name, c.set_id, c.image_url, c.game_type, pi.quantity, pi.item_type " +
                "FROM proposal_items pi " +
                "JOIN cards c ON pi.item_id = c.card_id " +
                "WHERE pi.proposal_id = ?";

        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setString(i + 1, params[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToProposal(rs, conn, sqlItems));
                    }
                }
            }
        } catch (Exception e) {
            throw new DaoException("Failed to get proposals by user", e);
        }
        return list;
    }

    private Proposal mapResultSetToProposal(ResultSet rs, Connection conn, String sqlItems)
            throws java.sql.SQLException {
        // Users
        User proposer = new User(rs.getString("prop_un"), rs.getString("prop_pw"),
                rs.getString("prop_type"));
        User receiver = new User(rs.getString("recv_un"), rs.getString("recv_pw"),
                rs.getString("recv_type"));
        User store = null;
        if (rs.getString("meet_un") != null) {
            store = new User(rs.getString("meet_un"), rs.getString("meet_pw"),
                    rs.getString("meet_type"));
        }

        List<CollectionItem> offered = new ArrayList<>();
        List<CollectionItem> asked = new ArrayList<>();

        String proposalId = rs.getString("id");

        try (PreparedStatement psItems = conn.prepareStatement(sqlItems)) {
            psItems.setString(1, proposalId);
            try (ResultSet rsItems = psItems.executeQuery()) {
                while (rsItems.next()) {
                    Card card = new Card(
                            rsItems.getString("name"),
                            rsItems.getString("card_id"),
                            rsItems.getString("set_id"),
                            rsItems.getString("image_url"),
                            CardGameType.valueOf(rsItems.getString("game_type")));

                    CollectionItem item = new CollectionItem(card, rsItems.getInt("quantity"));
                    String type = rsItems.getString("item_type");

                    if ("REQUESTED".equalsIgnoreCase(type)) {
                        asked.add(item);
                    } else {
                        offered.add(item);
                    }
                }
            }
        }

        Proposal p = new Proposal(proposalId, proposer, receiver, offered, asked, store);
        p.setStatus(ProposalStatus.valueOf(rs.getString("status")));
        java.sql.Timestamp ts = rs.getTimestamp("scheduled_at");
        if (ts != null)
            p.setScheduledAt(ts.toLocalDateTime());

        return p;
    }

    @Override
    public void save(Proposal proposal) {
        // Updated to remove asked_item_id
        String sqlProposal = "INSERT INTO proposals (id, proposer_username, receiver_username, status, meeting_store_username, scheduled_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlItems = "INSERT INTO proposal_items (proposal_id, item_id, quantity, item_type) VALUES (?, ?, ?, ?)";

        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement psProp = conn.prepareStatement(sqlProposal)) {
                psProp.setString(1, proposal.getId());
                psProp.setString(2, proposal.getProposer().getUsername());
                psProp.setString(3, proposal.getReceiver().getUsername());
                psProp.setString(4, proposal.getStatus().name());

                String meetingPlace = proposal.getMeetingStore() != null ? proposal.getMeetingStore().getUsername()
                        : null;
                java.sql.Timestamp meetingDate = proposal.getScheduledAt() != null
                        ? java.sql.Timestamp.valueOf(proposal.getScheduledAt())
                        : null;

                psProp.setString(5, meetingPlace);
                psProp.setTimestamp(6, meetingDate);

                java.sql.Timestamp createdTimestamp = proposal.getCreatedAt() != null
                        ? java.sql.Timestamp.valueOf(proposal.getCreatedAt())
                        : java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
                psProp.setTimestamp(7, createdTimestamp);

                psProp.executeUpdate();
            }

            try (PreparedStatement psItems = conn.prepareStatement(sqlItems)) {
                // Save Offered Items (item_type = 'OFFERED')
                psItems.setString(1, proposal.getId());
                psItems.setString(4, "OFFERED");
                for (CollectionItem item : proposal.getProposedItems()) {
                    psItems.setString(2, item.getCard().getCardID());
                    psItems.setInt(3, item.getQuantity());
                    psItems.addBatch();
                }

                // Save Asked Items (item_type = 'REQUESTED')
                if (proposal.getAskedItems() != null) {
                    psItems.setString(4, "REQUESTED");
                    for (CollectionItem item : proposal.getAskedItems()) {
                        psItems.setString(2, item.getCard().getCardID());
                        psItems.setInt(3, item.getQuantity());
                        psItems.addBatch();
                    }
                }
                psItems.executeBatch();
            }

        } catch (java.sql.SQLException e) {
            throw new DaoException("Failed to save proposal", e);
        }
    }

    @Override
    public void update(Proposal proposal) {
        String sqlUpdate = "UPDATE proposals SET status = ?, meeting_store_username = ?, scheduled_at = ? WHERE id = ?";
        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, proposal.getStatus().name());
                ps.setString(2, proposal.getMeetingStore() != null ? proposal.getMeetingStore().getUsername() : null);
                ps.setTimestamp(3,
                        proposal.getScheduledAt() != null ? java.sql.Timestamp.valueOf(proposal.getScheduledAt())
                                : null);
                ps.setString(4, proposal.getId());
                ps.executeUpdate();
            }
        } catch (java.sql.SQLException e) {
            throw new DaoException("Failed to update proposal", e);
        }
    }

    @Override
    public void delete(Proposal proposal) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
