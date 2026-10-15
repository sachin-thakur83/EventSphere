package com.eventsphere.controller;

import com.eventsphere.dao.BookingDao;
import com.eventsphere.dao.EventDao;
import com.eventsphere.model.User;
import com.eventsphere.util.Db;
import com.eventsphere.util.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.time.LocalDate;
import java.util.Map;

/** Public website: home, explore, event details, about, contact, terms, privacy. */
@WebServlet({"/home", "/events", "/event", "/about", "/contact", "/terms", "/privacy"})
public class PublicServlet extends BaseServlet {
    private static final int PAGE_SIZE = 9;

    @Override
    protected void get(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        switch (rq.getServletPath()) {
            case "/home" -> {
                rq.setAttribute("featured", EventDao.featured(3));
                rq.setAttribute("upcoming", EventDao.search("", -1, "", "", "", "date", 1, 6).get("rows"));
                rq.setAttribute("categories", EventDao.categoriesWithCount());
                view(rq, rs, "public/home");
            }
            case "/events" -> explore(rq, rs);
            case "/event" -> detail(rq, rs);
            case "/about" -> view(rq, rs, "public/about");
            case "/contact" -> view(rq, rs, "public/contact");
            case "/terms" -> { rq.setAttribute("doc", "terms"); view(rq, rs, "public/legal"); }
            case "/privacy" -> { rq.setAttribute("doc", "privacy"); view(rq, rs, "public/legal"); }
            default -> rs.sendError(404);
        }
    }

    private void explore(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        String q = str(rq, "q"), loc = str(rq, "loc"), from = str(rq, "from"), price = str(rq, "price"), sort = str(rq, "sort");
        long cat = lng(rq, "cat");
        int page = Math.max(1, num(rq, "page", 1));
        try { if (!from.isEmpty()) LocalDate.parse(from); } catch (Exception e) { from = ""; }
        Map<String, Object> r = EventDao.search(q, cat, loc, from, price, sort, page, PAGE_SIZE);
        long total = (Long) r.get("total");
        rq.setAttribute("events", r.get("rows"));
        rq.setAttribute("total", total);
        rq.setAttribute("page", page);
        rq.setAttribute("pages", Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE));
        rq.setAttribute("categories", EventDao.categories());
        rq.setAttribute("q", q); rq.setAttribute("cat", cat > 0 ? String.valueOf(cat) : "");
        rq.setAttribute("loc", loc); rq.setAttribute("from", from); rq.setAttribute("price", price); rq.setAttribute("sort", sort);
        view(rq, rs, "public/events");
    }

    private void detail(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        long id = lng(rq, "id");
        Map<String, Object> ev = id > 0 ? EventDao.byId(id) : null;
        User u = user(rq);
        if (ev != null && !"APPROVED".equals(ev.get("status"))) {
            // unapproved events are visible only to their organizer and to admins
            boolean allowed = u != null && ("ADMIN".equals(u.getRole()) || u.getId() == ((Number) ev.get("organizer_id")).longValue());
            if (!allowed) ev = null;
        }
        if (ev == null) { rs.sendError(404); return; }
        rq.setAttribute("ev", ev);
        rq.setAttribute("tickets", EventDao.tickets(id));
        rq.setAttribute("announcements", EventDao.announcements(id));
        if (u != null && "ATTENDEE".equals(u.getRole())) rq.setAttribute("myBooking", BookingDao.activeFor(u.getId(), id));
        view(rq, rs, "public/event");
    }

    @Override
    protected void post(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        if (!"/contact".equals(rq.getServletPath())) { rs.sendError(404); return; }
        String name = str(rq, "name"), email = str(rq, "email"), subject = str(rq, "subject"), msg = str(rq, "message");
        String error = null;
        if (!Validator.len(name, 2, 100)) error = "Please enter your name.";
        else if (!Validator.email(email)) error = "Enter a valid email address.";
        else if (!Validator.len(subject, 3, 150)) error = "Subject must be 3 to 150 characters.";
        else if (!Validator.len(msg, 10, 2000)) error = "Message must be 10 to 2000 characters.";
        if (error != null) {
            rq.setAttribute("error", error);
            rq.setAttribute("f_name", name); rq.setAttribute("f_email", email); rq.setAttribute("f_subject", subject); rq.setAttribute("f_message", msg);
            view(rq, rs, "public/contact");
            return;
        }
        Db.update("INSERT INTO contact_messages(name,email,subject,message) VALUES(?,?,?,?)", name, email, subject, msg);
        ok(rq, "Thank you! Your message has been sent.");
        redirect(rq, rs, "/contact");
    }
}
