package com.eventsphere.service;

import com.eventsphere.util.Db;
import java.sql.Connection;
import java.sql.SQLException;

/** In-app notifications. (Email can be added later: see README, "Enabling email".) */
public final class NotificationService {
    private NotificationService() {}

    private static String cut(String m) { return m.length() > 490 ? m.substring(0, 490) : m; }

    public static void send(Connection c, long userId, String msg) throws SQLException {
        Db.update(c, "INSERT INTO notifications(user_id,message) VALUES(?,?)", userId, cut(msg));
    }

    public static void send(long userId, String msg) throws SQLException {
        Db.update("INSERT INTO notifications(user_id,message) VALUES(?,?)", userId, cut(msg));
    }

    /** Notify every attendee with a confirmed booking for the event. Returns number notified. */
    public static int sendToAttendees(Connection c, long eventId, String msg) throws SQLException {
        return Db.update(c, "INSERT INTO notifications(user_id,message) SELECT DISTINCT user_id, ? FROM bookings WHERE event_id=? AND status='CONFIRMED'",
                cut(msg), eventId);
    }
}
