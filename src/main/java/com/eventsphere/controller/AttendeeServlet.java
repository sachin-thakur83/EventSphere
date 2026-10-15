package com.eventsphere.controller;

import com.eventsphere.dao.AdminDao;
import com.eventsphere.dao.BookingDao;
import com.eventsphere.dao.EventDao;
import com.eventsphere.dao.UserDao;
import com.eventsphere.model.User;
import com.eventsphere.util.PasswordUtil;
import com.eventsphere.util.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.util.*;

/** Attendee dashboard and profile. */
@WebServlet("/attendee/*")
public class AttendeeServlet extends BaseServlet {

    @Override
    protected void get(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        if (!"/dashboard".equals(path(rq))) { rs.sendError(404); return; }
        List<Map<String, Object>> all = BookingDao.byUser(u.getId());
        List<Map<String, Object>> upcoming = new ArrayList<>();
        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
        for (Map<String, Object> b : all) {
            String st = (String) b.get("status");
            if (("CONFIRMED".equals(st) || "PENDING".equals(st)) && !((java.sql.Date) b.get("event_date")).before(today)) upcoming.add(b);
        }
        rq.setAttribute("stats", BookingDao.attendeeStats(u.getId()));
        rq.setAttribute("upcoming", upcoming);
        rq.setAttribute("history", all);
        rq.setAttribute("notifications", AdminDao.notifications(u.getId(), 5));
        rq.setAttribute("recs", EventDao.recommend(u.getId(), 3));
        rq.setAttribute("profile", UserDao.byId(u.getId()));
        view(rq, rs, "attendee/dashboard");
    }

    @Override
    protected void post(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        if (!"/profile".equals(path(rq))) { rs.sendError(404); return; }
        String name = str(rq, "name"), phone = str(rq, "phone");
        String cur = rq.getParameter("current") == null ? "" : rq.getParameter("current");
        String np = rq.getParameter("newpass") == null ? "" : rq.getParameter("newpass");
        String error = null;
        if (!Validator.len(name, 2, 100)) error = "Name must be 2 to 100 characters.";
        else if (!Validator.phone(phone)) error = "Enter a valid phone number.";
        else if (!np.isEmpty()) {
            Map<String, Object> row = UserDao.findByEmail(u.getEmail());
            if (!PasswordUtil.verify(cur, (String) row.get("password_hash"))) error = "Current password is incorrect.";
            else if (!Validator.strongPassword(np)) error = "New password must be at least 8 characters with a letter and a digit.";
        }
        if (error != null) { err(rq, error); redirect(rq, rs, "/attendee/dashboard#profile"); return; }
        UserDao.updateProfile(u.getId(), name, phone);
        if (!np.isEmpty()) UserDao.updatePassword(u.getId(), PasswordUtil.hash(np));
        rq.getSession().setAttribute("user", new User(u.getId(), name, u.getEmail(), u.getRole()));
        ok(rq, "Profile updated.");
        redirect(rq, rs, "/attendee/dashboard");
    }
}
