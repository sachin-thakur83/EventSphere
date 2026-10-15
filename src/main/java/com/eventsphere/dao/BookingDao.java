package com.eventsphere.dao;

import com.eventsphere.util.Db;
import java.sql.SQLException;
import java.util.*;

/** All SQL for reading bookings (writing is done in BookingService, inside transactions). */
public final class BookingDao {
    private BookingDao() {}

    private static final String BASE = "SELECT b.*, e.title, e.event_date, e.start_time, e.venue, e.status AS event_status, u.name AS user_name, u.email AS user_email "
            + "FROM bookings b JOIN events e ON e.id=b.event_id JOIN users u ON u.id=b.user_id ";

    public static List<Map<String, Object>> byUser(long userId) throws SQLException {
        return Db.query(BASE + "WHERE b.user_id=? ORDER BY b.created_at DESC", userId);
    }

    public static Map<String, Object> byRef(String ref, long userId) throws SQLException {
        return Db.queryOne(BASE + "WHERE b.reference=? AND b.user_id=?", ref, userId);
    }

    public static Map<String, Object> activeFor(long userId, long eventId) throws SQLException {
        return Db.queryOne("SELECT reference,status FROM bookings WHERE user_id=? AND event_id=? AND status IN ('PENDING','CONFIRMED')", userId, eventId);
    }

    public static List<Map<String, Object>> items(long bookingId) throws SQLException {
        return Db.query("SELECT t.name, bi.quantity, bi.unit_price, (bi.quantity*bi.unit_price) AS line_total FROM booking_items bi JOIN ticket_types t ON t.id=bi.ticket_type_id WHERE bi.booking_id=?", bookingId);
    }

    public static Map<String, Object> payment(long bookingId) throws SQLException {
        return Db.queryOne("SELECT * FROM payments WHERE booking_id=? ORDER BY id DESC LIMIT 1", bookingId);
    }

    public static Map<String, Object> attendeeStats(long userId) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("upcoming", Db.count("SELECT COUNT(*) FROM bookings b JOIN events e ON e.id=b.event_id WHERE b.user_id=? AND b.status IN ('PENDING','CONFIRMED') AND e.event_date>=CURDATE()", userId));
        m.put("total", Db.count("SELECT COUNT(*) FROM bookings WHERE user_id=?", userId));
        m.put("unread", Db.count("SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0", userId));
        return m;
    }

    public static List<Map<String, Object>> recent(int n) throws SQLException {
        return Db.query(BASE + "ORDER BY b.created_at DESC LIMIT ?", n);
    }
}
