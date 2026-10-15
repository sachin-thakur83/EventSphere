package com.eventsphere.dao;

import com.eventsphere.service.NotificationService;
import com.eventsphere.util.Db;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/** All SQL about events, ticket types, announcements and attendee lists. */
public final class EventDao {
    private EventDao() {}

    /** Base SELECT used for event cards (adds category, organizer, lowest price, reserved seats). */
    private static final String CARD =
            "SELECT e.*, c.name AS category_name, c.icon AS category_icon, u.name AS organizer_name, "
          + "(SELECT MIN(price) FROM ticket_types t WHERE t.event_id=e.id) AS min_price, "
          + "(SELECT COALESCE(SUM(quantity_sold),0) FROM ticket_types t WHERE t.event_id=e.id) AS sold "
          + "FROM events e JOIN event_categories c ON c.id=e.category_id JOIN users u ON u.id=e.organizer_id ";

    private static final String PUBLIC = " e.status='APPROVED' AND e.event_date>=CURDATE() ";

    public static void markCompleted() throws SQLException {
        Db.update("UPDATE events SET status='COMPLETED' WHERE status='APPROVED' AND event_date<CURDATE()");
    }

    public static List<Map<String, Object>> categories() throws SQLException {
        return Db.query("SELECT * FROM event_categories ORDER BY name");
    }

    public static List<Map<String, Object>> categoriesWithCount() throws SQLException {
        return Db.query("SELECT c.*, (SELECT COUNT(*) FROM events e WHERE e.category_id=c.id AND" + PUBLIC + ") AS n FROM event_categories c ORDER BY c.name");
    }

    public static Map<String, Object> byId(long id) throws SQLException {
        return Db.queryOne(CARD + "WHERE e.id=?", id);
    }

    public static List<Map<String, Object>> featured(int n) throws SQLException {
        return Db.query(CARD + "WHERE" + PUBLIC + "ORDER BY sold DESC, e.event_date LIMIT ?", n);
    }

    public static List<Map<String, Object>> recommend(long userId, int n) throws SQLException {
        return Db.query(CARD + "WHERE" + PUBLIC + "AND e.id NOT IN (SELECT event_id FROM bookings WHERE user_id=? AND status IN ('PENDING','CONFIRMED')) ORDER BY e.event_date LIMIT ?", userId, n);
    }

    /** Public search: text, category (-1 = all), location, date-from, price (free/paid), sort, pagination. */
    public static Map<String, Object> search(String q, long cat, String loc, String from, String price, String sort, int page, int size) throws SQLException {
        StringBuilder w = new StringBuilder(" WHERE" + PUBLIC);
        List<Object> p = new ArrayList<>();
        if (!q.isEmpty()) { w.append(" AND (e.title LIKE ? OR e.description LIKE ?)"); p.add("%" + q + "%"); p.add("%" + q + "%"); }
        if (cat > 0) { w.append(" AND e.category_id=?"); p.add(cat); }
        if (!loc.isEmpty()) { w.append(" AND e.venue LIKE ?"); p.add("%" + loc + "%"); }
        if (!from.isEmpty()) { w.append(" AND e.event_date>=?"); p.add(java.sql.Date.valueOf(LocalDate.parse(from))); }
        if ("free".equals(price)) w.append(" AND NOT EXISTS (SELECT 1 FROM ticket_types t WHERE t.event_id=e.id AND t.price>0)");
        if ("paid".equals(price)) w.append(" AND EXISTS (SELECT 1 FROM ticket_types t WHERE t.event_id=e.id AND t.price>0)");
        String order = switch (sort) {
            case "newest" -> " ORDER BY e.created_at DESC";
            case "price_asc" -> " ORDER BY min_price ASC, e.event_date";
            case "price_desc" -> " ORDER BY min_price DESC, e.event_date";
            default -> " ORDER BY e.event_date ASC";
        };
        long total = Db.count("SELECT COUNT(*) FROM events e" + w, p.toArray());
        p.add(size);
        p.add((page - 1) * size);
        List<Map<String, Object>> rows = Db.query(CARD + w + order + " LIMIT ? OFFSET ?", p.toArray());
        Map<String, Object> r = new HashMap<>();
        r.put("rows", rows);
        r.put("total", total);
        return r;
    }

    public static List<Map<String, Object>> tickets(long eventId) throws SQLException {
        return Db.query("SELECT * FROM ticket_types WHERE event_id=? ORDER BY price", eventId);
    }

    public static List<Map<String, Object>> announcements(long eventId) throws SQLException {
        return Db.query("SELECT * FROM event_announcements WHERE event_id=? ORDER BY created_at DESC LIMIT 20", eventId);
    }

    // ---------------- organizer ----------------
    public static List<Map<String, Object>> forOrganizer(long orgId) throws SQLException {
        return Db.query("SELECT e.*, c.name AS category_name, "
                + "(SELECT COALESCE(SUM(bi.quantity),0) FROM booking_items bi JOIN bookings b ON b.id=bi.booking_id WHERE b.event_id=e.id AND b.status='CONFIRMED') AS sold, "
                + "(SELECT COALESCE(SUM(b.total_amount),0) FROM bookings b WHERE b.event_id=e.id AND b.status='CONFIRMED') AS revenue "
                + "FROM events e JOIN event_categories c ON c.id=e.category_id WHERE e.organizer_id=? ORDER BY e.event_date DESC", orgId);
    }

    public static Map<String, Object> organizerStats(long orgId) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("events", Db.count("SELECT COUNT(*) FROM events WHERE organizer_id=?", orgId));
        m.put("upcoming", Db.count("SELECT COUNT(*) FROM events WHERE organizer_id=? AND status='APPROVED' AND event_date>=CURDATE()", orgId));
        m.put("registrations", Db.count("SELECT COUNT(*) FROM bookings b JOIN events e ON e.id=b.event_id WHERE e.organizer_id=? AND b.status='CONFIRMED'", orgId));
        m.put("tickets", Db.count("SELECT COALESCE(SUM(bi.quantity),0) FROM booking_items bi JOIN bookings b ON b.id=bi.booking_id JOIN events e ON e.id=b.event_id WHERE e.organizer_id=? AND b.status='CONFIRMED'", orgId));
        m.put("revenue", Db.scalar("SELECT COALESCE(SUM(b.total_amount),0) FROM bookings b JOIN events e ON e.id=b.event_id WHERE e.organizer_id=? AND b.status='CONFIRMED'", orgId));
        return m;
    }

    public static Map<String, Object> owned(long id, long orgId) throws SQLException {
        return Db.queryOne(CARD + "WHERE e.id=? AND e.organizer_id=?", id, orgId);
    }

    public static List<Map<String, Object>> attendees(long eventId, String q, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT b.reference,b.status,b.total_amount,b.created_at,u.name,u.email,"
                + "(SELECT GROUP_CONCAT(CONCAT(t.name,' x',bi.quantity) SEPARATOR ', ') FROM booking_items bi JOIN ticket_types t ON t.id=bi.ticket_type_id WHERE bi.booking_id=b.id) AS tickets "
                + "FROM bookings b JOIN users u ON u.id=b.user_id WHERE b.event_id=?");
        List<Object> p = new ArrayList<>();
        p.add(eventId);
        if (!q.isEmpty()) { sql.append(" AND (u.name LIKE ? OR u.email LIKE ? OR b.reference LIKE ?)"); p.add("%" + q + "%"); p.add("%" + q + "%"); p.add("%" + q + "%"); }
        if (!status.isEmpty()) { sql.append(" AND b.status=?"); p.add(status); }
        sql.append(" ORDER BY b.created_at DESC");
        return Db.query(sql.toString(), p.toArray());
    }

    public static long insert(long org, long cat, String title, String desc, String banner, String venue, boolean online,
                              LocalDate d, LocalTime s, LocalTime e, LocalDateTime dl, int cap) throws SQLException {
        return Db.insert("INSERT INTO events(organizer_id,category_id,title,description,banner_url,venue,is_online,event_date,start_time,end_time,reg_deadline,capacity,status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,'DRAFT')",
                org, cat, title, desc, banner.isEmpty() ? null : banner, venue, online, d, s, e, dl, cap);
    }

    public static int update(long id, long org, long cat, String title, String desc, String banner, String venue, boolean online,
                             LocalDate d, LocalTime s, LocalTime e, LocalDateTime dl, int cap) throws SQLException {
        return Db.update("UPDATE events SET category_id=?,title=?,description=?,banner_url=?,venue=?,is_online=?,event_date=?,start_time=?,end_time=?,reg_deadline=?,capacity=? WHERE id=? AND organizer_id=?",
                cat, title, desc, banner.isEmpty() ? null : banner, venue, online, d, s, e, dl, cap, id, org);
    }

    /** Cancels an event, cancels + (demo) refunds its bookings and notifies attendees, all in one transaction. */
    public static void cancelEvent(long id, String title) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                NotificationService.sendToAttendees(c, id, "Event cancelled: \"" + title + "\". Your booking was cancelled and refunded (demo).");
                Db.update(c, "UPDATE payments p JOIN bookings b ON b.id=p.booking_id SET p.status='REFUNDED' WHERE b.event_id=? AND p.status='SUCCESS' AND b.status IN ('PENDING','CONFIRMED')", id);
                Db.update(c, "UPDATE bookings SET status='CANCELLED' WHERE event_id=? AND status IN ('PENDING','CONFIRMED')", id);
                Db.update(c, "UPDATE events SET status='CANCELLED' WHERE id=?", id);
                c.commit();
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    // ---------------- admin ----------------
    public static List<Map<String, Object>> byStatus(String status) throws SQLException {
        String sql = "SELECT e.*, c.name AS category_name, u.name AS organizer_name, "
                + "(SELECT COUNT(*) FROM ticket_types t WHERE t.event_id=e.id) AS ticket_types "
                + "FROM events e JOIN event_categories c ON c.id=e.category_id JOIN users u ON u.id=e.organizer_id "
                + (status.isEmpty() ? "" : "WHERE e.status=? ") + "ORDER BY FIELD(e.status,'PENDING') DESC, e.event_date DESC";
        return status.isEmpty() ? Db.query(sql) : Db.query(sql, status);
    }
}
