package com.eventsphere.dao;

import com.eventsphere.util.Db;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/** SQL for admin statistics, settings, categories, notifications and logs. */
public final class AdminDao {
    private AdminDao() {}

    public static Map<String, Object> stats() throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("users", Db.count("SELECT COUNT(*) FROM users"));
        m.put("organizers", Db.count("SELECT COUNT(*) FROM users WHERE role='ORGANIZER'"));
        m.put("attendees", Db.count("SELECT COUNT(*) FROM users WHERE role='ATTENDEE'"));
        m.put("events", Db.count("SELECT COUNT(*) FROM events"));
        m.put("pending", Db.count("SELECT COUNT(*) FROM events WHERE status='PENDING'"));
        m.put("bookings", Db.count("SELECT COUNT(*) FROM bookings"));
        m.put("revenue", Db.scalar("SELECT COALESCE(SUM(total_amount),0) FROM bookings WHERE status='CONFIRMED'"));
        return m;
    }

    // ---- settings ----
    public static String setting(Connection c, String key, String def) throws SQLException {
        Object v = Db.scalar(c, "SELECT setting_value FROM system_settings WHERE setting_key=?", key);
        return v == null ? def : v.toString();
    }

    public static Map<String, String> settings() throws SQLException {
        Map<String, String> m = new HashMap<>();
        for (Map<String, Object> r : Db.query("SELECT * FROM system_settings")) m.put((String) r.get("setting_key"), (String) r.get("setting_value"));
        return m;
    }

    public static void saveSetting(String key, String value) throws SQLException {
        Db.update("INSERT INTO system_settings(setting_key,setting_value) VALUES(?,?) ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)", key, value);
    }

    // ---- categories ----
    public static List<Map<String, Object>> categoriesWithEvents() throws SQLException {
        return Db.query("SELECT c.*, (SELECT COUNT(*) FROM events e WHERE e.category_id=c.id) AS events FROM event_categories c ORDER BY c.name");
    }

    public static void addCategory(String name, String icon) throws SQLException {
        Db.update("INSERT INTO event_categories(name,icon) VALUES(?,?)", name, icon);
    }

    public static int deleteCategory(long id) throws SQLException {
        return Db.update("DELETE FROM event_categories WHERE id=? AND NOT EXISTS (SELECT 1 FROM events WHERE category_id=?)", id, id);
    }

    // ---- logs ----
    public static List<Map<String, Object>> auditLogs(int n) throws SQLException {
        return Db.query("SELECT a.*, u.name AS user_name FROM audit_logs a LEFT JOIN users u ON u.id=a.user_id ORDER BY a.id DESC LIMIT ?", n);
    }

    public static List<Map<String, Object>> recentUsers(int n) throws SQLException {
        return Db.query("SELECT id,name,email,role,created_at FROM users ORDER BY id DESC LIMIT ?", n);
    }

    // ---- notifications (any user) ----
    public static List<Map<String, Object>> notifications(long userId, int n) throws SQLException {
        return Db.query("SELECT * FROM notifications WHERE user_id=? ORDER BY id DESC LIMIT ?", userId, n);
    }

    public static void markAllRead(long userId) throws SQLException {
        Db.update("UPDATE notifications SET is_read=1 WHERE user_id=?", userId);
    }
}
