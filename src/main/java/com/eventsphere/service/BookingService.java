package com.eventsphere.service;

import com.eventsphere.dao.AdminDao;
import com.eventsphere.util.Db;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Booking business logic. Every public method runs in ONE database transaction.
 *
 * Overselling protection: we lock the event row and the ticket rows with SELECT ... FOR UPDATE,
 * so two users booking at the same moment are processed one after the other.
 * Seats are reserved (quantity_sold++) when a booking is created and released again when a
 * booking is cancelled or its payment fails. Revenue only counts CONFIRMED bookings.
 */
public final class BookingService {
    private static final SecureRandom RNG = new SecureRandom();
    private static final String ALPHA = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private BookingService() {}

    /** Thrown for expected business-rule problems (shown to the user as a message). */
    public static class BookingException extends Exception {
        public BookingException(String m) { super(m); }
    }

    private static String random(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(ALPHA.charAt(RNG.nextInt(ALPHA.length())));
        return sb.toString();
    }

    /** Creates a booking. Free total -> CONFIRMED immediately, otherwise PENDING (needs payment). Returns reference. */
    public static String create(long userId, long eventId, Map<Long, Integer> qty) throws SQLException, BookingException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                Map<String, Object> ev = Db.queryOne(c, "SELECT * FROM events WHERE id=? FOR UPDATE", eventId);
                if (ev == null || !"APPROVED".equals(ev.get("status"))) throw new BookingException("This event is not open for booking.");
                LocalDate date = ((java.sql.Date) ev.get("event_date")).toLocalDate();
                if (date.isBefore(LocalDate.now())) throw new BookingException("This event has already taken place.");
                Timestamp dl = (Timestamp) ev.get("reg_deadline");
                if (dl != null && dl.toLocalDateTime().isBefore(LocalDateTime.now())) throw new BookingException("The registration deadline has passed.");
                if (Db.count(c, "SELECT COUNT(*) FROM bookings WHERE user_id=? AND event_id=? AND status IN ('PENDING','CONFIRMED')", userId, eventId) > 0)
                    throw new BookingException("You already have an active booking for this event. Cancel it first if you want to book again.");

                int max = 10;
                try { max = Integer.parseInt(AdminDao.setting(c, "max_tickets_per_booking", "10")); } catch (NumberFormatException ignored) { }

                int totalQty = 0;
                BigDecimal total = BigDecimal.ZERO;
                List<Object[]> items = new ArrayList<>();
                for (Map.Entry<Long, Integer> en : new TreeMap<>(qty).entrySet()) {
                    int q = en.getValue();
                    if (q <= 0) continue;
                    Map<String, Object> t = Db.queryOne(c, "SELECT * FROM ticket_types WHERE id=? AND event_id=? FOR UPDATE", en.getKey(), eventId);
                    if (t == null) throw new BookingException("Invalid ticket type selected.");
                    int left = ((Number) t.get("quantity_total")).intValue() - ((Number) t.get("quantity_sold")).intValue();
                    if (q > left) throw new BookingException("Only " + left + " '" + t.get("name") + "' ticket(s) left.");
                    BigDecimal price = (BigDecimal) t.get("price");           // price comes from the DB, never from the browser
                    total = total.add(price.multiply(BigDecimal.valueOf(q)));
                    totalQty += q;
                    items.add(new Object[]{en.getKey(), q, price});
                }
                if (totalQty == 0) throw new BookingException("Select at least one ticket.");
                if (totalQty > max) throw new BookingException("You can book at most " + max + " tickets at a time.");
                long reserved = Db.count(c, "SELECT COALESCE(SUM(quantity_sold),0) FROM ticket_types WHERE event_id=?", eventId);
                if (reserved + totalQty > ((Number) ev.get("capacity")).intValue()) throw new BookingException("Not enough seats left for this event.");

                boolean free = total.signum() == 0;
                String ref = "ES-" + random(8);
                long bid = Db.insert(c, "INSERT INTO bookings(reference,user_id,event_id,status,total_amount) VALUES(?,?,?,?,?)",
                        ref, userId, eventId, free ? "CONFIRMED" : "PENDING", total);
                for (Object[] it : items) {
                    Db.update(c, "INSERT INTO booking_items(booking_id,ticket_type_id,quantity,unit_price) VALUES(?,?,?,?)", bid, it[0], it[1], it[2]);
                    Db.update(c, "UPDATE ticket_types SET quantity_sold=quantity_sold+? WHERE id=?", it[1], it[0]);
                }
                if (free) NotificationService.send(c, userId, "Registration confirmed for \"" + ev.get("title") + "\". Reference: " + ref);
                c.commit();
                return ref;
            } catch (SQLException | BookingException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** DEMO payment. success=true confirms the booking; false marks it FAILED and releases the seats. */
    public static void pay(long userId, String ref, boolean success, String method) throws SQLException, BookingException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                Map<String, Object> b = Db.queryOne(c, "SELECT * FROM bookings WHERE reference=? AND user_id=? FOR UPDATE", ref, userId);
                if (b == null) throw new BookingException("Booking not found.");
                // Only a PENDING booking can be paid: this prevents double processing.
                if (!"PENDING".equals(b.get("status"))) throw new BookingException("This booking has already been processed.");
                long bid = ((Number) b.get("id")).longValue();
                BigDecimal amount = (BigDecimal) b.get("total_amount");
                String title = String.valueOf(Db.scalar(c, "SELECT title FROM events WHERE id=?", b.get("event_id")));
                String txn = "SIM-" + random(10);
                if (success) {
                    Db.update(c, "UPDATE bookings SET status='CONFIRMED' WHERE id=?", bid);
                    Db.insert(c, "INSERT INTO payments(booking_id,amount,method,status,txn_ref,is_simulated) VALUES(?,?,?,'SUCCESS',?,1)", bid, amount, method, txn);
                    NotificationService.send(c, userId, "Payment received (demo) - booking confirmed for \"" + title + "\". Reference: " + ref);
                } else {
                    Db.update(c, "UPDATE bookings SET status='FAILED' WHERE id=?", bid);
                    releaseStock(c, bid);
                    Db.insert(c, "INSERT INTO payments(booking_id,amount,method,status,txn_ref,is_simulated) VALUES(?,?,?,'FAILED',?,1)", bid, amount, method, txn);
                    NotificationService.send(c, userId, "Payment failed (demo) for \"" + title + "\". Your seats were released. Reference: " + ref);
                }
                c.commit();
            } catch (SQLException | BookingException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Cancel a PENDING or CONFIRMED booking. Confirmed ones follow the cancellation window (system setting). */
    public static void cancel(long userId, String ref) throws SQLException, BookingException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                Map<String, Object> b = Db.queryOne(c, "SELECT * FROM bookings WHERE reference=? AND user_id=? FOR UPDATE", ref, userId);
                if (b == null) throw new BookingException("Booking not found.");
                String st = (String) b.get("status");
                if (!"PENDING".equals(st) && !"CONFIRMED".equals(st)) throw new BookingException("This booking cannot be cancelled.");
                Map<String, Object> ev = Db.queryOne(c, "SELECT title,event_date,start_time FROM events WHERE id=?", b.get("event_id"));
                if ("CONFIRMED".equals(st)) {
                    int hours = 24;
                    try { hours = Integer.parseInt(AdminDao.setting(c, "cancellation_hours", "24")); } catch (NumberFormatException ignored) { }
                    LocalDateTime start = LocalDateTime.of(((java.sql.Date) ev.get("event_date")).toLocalDate(), ((java.sql.Time) ev.get("start_time")).toLocalTime());
                    if (LocalDateTime.now().plusHours(hours).isAfter(start))
                        throw new BookingException("Confirmed bookings can only be cancelled up to " + hours + " hours before the event starts.");
                }
                long bid = ((Number) b.get("id")).longValue();
                releaseStock(c, bid);
                Db.update(c, "UPDATE bookings SET status='CANCELLED' WHERE id=?", bid);
                Db.update(c, "UPDATE payments SET status='REFUNDED' WHERE booking_id=? AND status='SUCCESS'", bid); // simulated refund
                NotificationService.send(c, userId, "Your booking " + ref + " for \"" + ev.get("title") + "\" was cancelled.");
                c.commit();
            } catch (SQLException | BookingException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Gives the seats of a booking back to ticket inventory. */
    private static void releaseStock(Connection c, long bookingId) throws SQLException {
        Db.update(c, "UPDATE ticket_types t JOIN booking_items bi ON bi.ticket_type_id=t.id SET t.quantity_sold=t.quantity_sold-bi.quantity WHERE bi.booking_id=?", bookingId);
    }
}
