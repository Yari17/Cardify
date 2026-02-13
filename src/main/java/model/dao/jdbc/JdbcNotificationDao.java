package model.dao.jdbc;

import config.DBConnector;
import exception.DaoException;
import model.dao.INotificationDao;
import model.domain.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione JDBC del DAO per le notifiche.
 * Gestisce la persistenza degli avvisi utente utilizzando SQL.
 */
public class JdbcNotificationDao implements INotificationDao {

    @Override
    public void addNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, ?)";
        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, notification.getUserId());
                ps.setString(2, notification.getMessage());
                ps.setBoolean(3, notification.isRead());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to add notification", e);
        }
    }

    @Override
    public List<Notification> getUnreadNotifications(String userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT id, user_id, message, is_read FROM notifications WHERE user_id = ? AND is_read = false";

        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        notifications.add(new Notification(
                                rs.getInt("id"),
                                rs.getString("user_id"),
                                rs.getString("message"),
                                rs.getBoolean("is_read")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to get notifications by user", e);
        }
        return notifications;
    }

    @Override
    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, notificationId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to update notification status", e);
        }
    }
}
