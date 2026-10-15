package com.eventsphere.controller;

import com.eventsphere.dao.AdminDao;
import com.eventsphere.dao.EventDao;
import com.eventsphere.model.User;
import com.eventsphere.service.AuditService;
import com.eventsphere.service.NotificationService;
import com.eventsphere.util.Db;
import com.eventsphere.util.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Organizer module: dashboard, event CRUD, ticket types, attendee list, announcements. Every action checks ownership. */
@WebServlet("/organizer/*")
public class OrganizerServlet extends BaseServlet {

    /** Loads the event only if it belongs to the logged-in organizer; otherwise sends 403. */
    private Map<String, Object> owned(HttpServletRequest rq, HttpServletResponse rs, long id) throws Exception {
        Map<String, Object> ev = id > 0 ? EventDao.owned(id, user(rq).getId()) : null;
        if (ev == null) rs.sendError(403, "This event does not belong to you.");
        return ev;
    }

    @Override
    protected void get(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        switch (path(rq)) {
            case "/", "/dashboard" -> {
                EventDao.markCompleted();
                rq.setAttribute("stats", EventDao.organizerStats(u.getId()));
                rq.setAttribute("myEvents", EventDao.forOrganizer(u.getId()));
                Map<String, String> ch = Db.chart("SELECT e.title, COALESCE(SUM(bi.quantity),0) FROM events e LEFT JOIN bookings b ON b.event_id=e.id AND b.status='CONFIRMED' LEFT JOIN booking_items bi ON bi.booking_id=b.id WHERE e.organizer_id=? GROUP BY e.id, e.title ORDER BY e.id DESC LIMIT 6", u.getId());
                rq.setAttribute("chartLabels", ch.get("labels")); rq.setAttribute("chartData", ch.get("data"));
                rq.setAttribute("notifications", AdminDao.notifications(u.getId(), 5));
                view(rq, rs, "organizer/dashboard");
            }
            case "/event/new" -> {
                rq.setAttribute("ev", new HashMap<String, Object>());
                rq.setAttribute("cats", EventDao.categories());
                view(rq, rs, "organizer/event-form");
            }
            case "/event/edit" -> {
                Map<String, Object> ev = owned(rq, rs, lng(rq, "id"));
                if (ev == null) return;
                String st = (String) ev.get("status");
                if ("CANCELLED".equals(st) || "COMPLETED".equals(st)) { err(rq, "This event can no longer be edited."); redirect(rq, rs, "/organizer/dashboard"); return; }
                java.sql.Timestamp dl = (java.sql.Timestamp) ev.get("reg_deadline");
                ev.put("deadline_str", dl == null ? "" : dl.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
                rq.setAttribute("ev", ev);
                rq.setAttribute("cats", EventDao.categories());
                view(rq, rs, "organizer/event-form");
            }
            case "/event/manage" -> {
                long id = lng(rq, "id");
                Map<String, Object> ev = owned(rq, rs, id);
                if (ev == null) return;
                String q = str(rq, "q"), st = str(rq, "st");
                if (!List.of("PENDING", "CONFIRMED", "CANCELLED", "FAILED").contains(st)) st = "";
                rq.setAttribute("ev", ev);
                rq.setAttribute("tickets", EventDao.tickets(id));
                rq.setAttribute("attendees", EventDao.attendees(id, q, st));
                rq.setAttribute("announcements", EventDao.announcements(id));
                rq.setAttribute("q", q); rq.setAttribute("st", st);
                view(rq, rs, "organizer/manage");
            }
            default -> rs.sendError(404);
        }
    }

    @Override
    protected void post(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        switch (path(rq)) {
            case "/event/save" -> saveEvent(rq, rs, u);
            case "/event/submit" -> {
                long id = lng(rq, "id");
                Map<String, Object> ev = owned(rq, rs, id);
                if (ev == null) return;
                String st = (String) ev.get("status");
                if (!"DRAFT".equals(st) && !"REJECTED".equals(st)) err(rq, "Only draft or rejected events can be submitted.");
                else if (Db.count("SELECT COUNT(*) FROM ticket_types WHERE event_id=?", id) == 0) err(rq, "Add at least one ticket type before submitting.");
                else {
                    Db.update("UPDATE events SET status='PENDING', admin_feedback=NULL WHERE id=? AND organizer_id=?", id, u.getId());
                    AuditService.log(u.getId(), "EVENT_SUBMITTED", "Event #" + id + " " + ev.get("title"));
                    ok(rq, "Event submitted for administrator approval.");
                }
                redirect(rq, rs, "/organizer/event/manage?id=" + id);
            }
            case "/event/cancel" -> {
                long id = lng(rq, "id");
                Map<String, Object> ev = owned(rq, rs, id);
                if (ev == null) return;
                String st = (String) ev.get("status");
                if ("CANCELLED".equals(st) || "COMPLETED".equals(st)) err(rq, "This event cannot be cancelled.");
                else {
                    EventDao.cancelEvent(id, (String) ev.get("title"));
                    AuditService.log(u.getId(), "EVENT_CANCELLED", "Event #" + id + " " + ev.get("title"));
                    ok(rq, "Event cancelled. Attendees were notified.");
                }
                redirect(rq, rs, "/organizer/dashboard");
            }
            case "/ticket/save" -> saveTicket(rq, rs);
            case "/ticket/delete" -> {
                long eid = lng(rq, "event_id");
                if (owned(rq, rs, eid) == null) return;
                int n = Db.update("DELETE FROM ticket_types WHERE id=? AND event_id=? AND quantity_sold=0", lng(rq, "id"), eid);
                if (n > 0) ok(rq, "Ticket type deleted."); else err(rq, "Tickets that already have bookings cannot be deleted.");
                redirect(rq, rs, "/organizer/event/manage?id=" + eid);
            }
            case "/announce" -> {
                long eid = lng(rq, "event_id");
                Map<String, Object> ev = owned(rq, rs, eid);
                if (ev == null) return;
                String title = str(rq, "title"), msg = str(rq, "message");
                if (!"APPROVED".equals(ev.get("status"))) err(rq, "Announcements can be sent only for approved (published) events.");
                else if (!Validator.len(title, 3, 120)) err(rq, "Title must be 3 to 120 characters.");
                else if (!Validator.len(msg, 5, 1000)) err(rq, "Message must be 5 to 1000 characters.");
                else {
                    int n;
                    try (Connection c = Db.get()) {
                        c.setAutoCommit(false);
                        try {
                            Db.update(c, "INSERT INTO event_announcements(event_id,title,message) VALUES(?,?,?)", eid, title, msg);
                            n = NotificationService.sendToAttendees(c, eid, "Update for \"" + ev.get("title") + "\": " + title + " - " + msg);
                            c.commit();
                        } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
                    }
                    ok(rq, "Announcement sent to " + n + " attendee(s).");
                }
                redirect(rq, rs, "/organizer/event/manage?id=" + eid);
            }
            default -> rs.sendError(404);
        }
    }

    private void saveEvent(HttpServletRequest rq, HttpServletResponse rs, User u) throws Exception {
        long id = lng(rq, "id");
        Map<String, Object> old = null;
        if (id > 0) {
            old = owned(rq, rs, id);
            if (old == null) return;
            String st = (String) old.get("status");
            if ("CANCELLED".equals(st) || "COMPLETED".equals(st)) { err(rq, "This event can no longer be edited."); redirect(rq, rs, "/organizer/dashboard"); return; }
        }
        // keep the raw input so the form can be re-shown on error
        Map<String, Object> f = new HashMap<>();
        for (String k : new String[]{"title", "description", "category_id", "banner_url", "venue", "event_date", "start_time", "end_time", "deadline_str", "capacity"})
            f.put(k, str(rq, k.equals("deadline_str") ? "reg_deadline" : k));
        boolean online = rq.getParameter("is_online") != null;
        f.put("is_online", online);
        if (id > 0) f.put("id", id);

        String title = (String) f.get("title"), desc = (String) f.get("description"), venue = (String) f.get("venue"), banner = (String) f.get("banner_url");
        String error = null;
        LocalDate date = null; LocalTime st = null, en = null; LocalDateTime dl = null; int cap = 0; long cat = -1;
        try {
            cat = Long.parseLong((String) f.get("category_id"));
            date = LocalDate.parse((String) f.get("event_date"));
            st = LocalTime.parse((String) f.get("start_time"));
            en = LocalTime.parse((String) f.get("end_time"));
            String d = (String) f.get("deadline_str");
            if (!d.isEmpty()) dl = LocalDateTime.parse(d);
            cap = Integer.parseInt((String) f.get("capacity"));
        } catch (Exception e) { error = "Please fill in a valid category, date, times and capacity."; }

        if (error == null) {
            if (!Validator.len(title, 3, 150)) error = "Title must be 3 to 150 characters.";
            else if (!Validator.len(desc, 10, 5000)) error = "Description must be 10 to 5000 characters.";
            else if (!Validator.len(venue, 3, 255)) error = "Venue / meeting information is required.";
            else if (!banner.isEmpty() && !Validator.httpUrl(banner)) error = "Banner must be a valid http(s) image URL.";
            else if (date.isBefore(LocalDate.now())) error = "Event date cannot be in the past.";
            else if (!en.isAfter(st)) error = "End time must be after start time.";
            else if (dl != null && dl.isAfter(LocalDateTime.of(date, st))) error = "Registration deadline must be before the event starts.";
            else if (cap < 1 || cap > 100000) error = "Capacity must be between 1 and 100000.";
            else if (Db.count("SELECT COUNT(*) FROM event_categories WHERE id=?", cat) == 0) error = "Choose a valid category.";
            else if (id > 0 && cap < Db.count("SELECT COALESCE(SUM(quantity_sold),0) FROM ticket_types WHERE event_id=?", id)) error = "Capacity cannot be lower than the seats already booked.";
        }
        if (error != null) {
            rq.setAttribute("error", error);
            rq.setAttribute("ev", f);
            rq.setAttribute("cats", EventDao.categories());
            view(rq, rs, "organizer/event-form");
            return;
        }
        if (id < 0) {
            id = EventDao.insert(u.getId(), cat, title, desc, banner, venue, online, date, st, en, dl, cap);
            AuditService.log(u.getId(), "EVENT_CREATED", "Event #" + id + " " + title);
            ok(rq, "Event saved as draft. Now add ticket types, then submit it for approval.");
        } else {
            EventDao.update(id, u.getId(), cat, title, desc, banner, venue, online, date, st, en, dl, cap);
            // notify attendees if an approved event changed date or venue
            if ("APPROVED".equals(old.get("status"))) {
                boolean changed = !date.equals(((java.sql.Date) old.get("event_date")).toLocalDate()) || !venue.equals(old.get("venue"));
                if (changed) {
                    try (Connection c = Db.get()) {
                        NotificationService.sendToAttendees(c, id, "Event updated: \"" + title + "\" is now on " + date + " at " + venue + ".");
                    }
                }
            }
            ok(rq, "Event updated.");
        }
        redirect(rq, rs, "/organizer/event/manage?id=" + id);
    }

    private void saveTicket(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        long eid = lng(rq, "event_id"), tid = lng(rq, "id");
        Map<String, Object> ev = owned(rq, rs, eid);
        if (ev == null) return;
        String name = str(rq, "name");
        BigDecimal price;
        int qty = num(rq, "quantity", -1);
        try { price = new BigDecimal(str(rq, "price")); } catch (NumberFormatException e) { price = BigDecimal.valueOf(-1); }
        int cap = ((Number) ev.get("capacity")).intValue();
        String error = null;
        if (!Validator.len(name, 2, 60)) error = "Ticket name must be 2 to 60 characters.";
        else if (price.signum() < 0 || price.compareTo(new BigDecimal("1000000")) > 0 || price.scale() > 2) error = "Enter a valid price (0 for free, max 2 decimals).";
        else if (qty < 1 || qty > cap) error = "Quantity must be between 1 and the event capacity (" + cap + ").";
        if (error == null) {
            try {
                if (tid > 0) {
                    // price can only change while nothing has been sold; quantity can't go below sold seats
                    int n = Db.update("UPDATE ticket_types SET name=?, price=IF(quantity_sold=0,?,price), quantity_total=? WHERE id=? AND event_id=? AND quantity_sold<=?",
                            name, price, qty, tid, eid, qty);
                    if (n == 0) error = "Quantity cannot be lower than tickets already sold (or no change was made).";
                } else {
                    Db.insert("INSERT INTO ticket_types(event_id,name,price,quantity_total) VALUES(?,?,?,?)", eid, name, price, qty);
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) error = "A ticket type with that name already exists for this event."; else throw e;
            }
        }
        if (error != null) err(rq, error); else ok(rq, "Ticket type saved.");
        redirect(rq, rs, "/organizer/event/manage?id=" + eid);
    }
}
