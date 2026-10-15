package com.eventsphere.controller;

import com.eventsphere.dao.*;
import com.eventsphere.model.User;
import com.eventsphere.service.AuditService;
import com.eventsphere.service.NotificationService;
import com.eventsphere.util.Db;
import com.eventsphere.util.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.sql.SQLException;
import java.util.*;

/** Administrator module: dashboard, event approvals, users, categories, settings, activity log. */
@WebServlet("/admin/*")
public class AdminServlet extends BaseServlet {
    private static final int PAGE_SIZE = 10;

    @Override
    protected void get(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        switch (path(rq)) {
            case "/", "/dashboard" -> {
                EventDao.markCompleted();
                rq.setAttribute("stats", AdminDao.stats());
                Map<String, String> sc = Db.chart("SELECT status, COUNT(*) FROM events GROUP BY status");
                rq.setAttribute("statusLabels", sc.get("labels")); rq.setAttribute("statusData", sc.get("data"));
                Map<String, String> cc = Db.chart("SELECT c.name, COUNT(b.id) FROM event_categories c LEFT JOIN events e ON e.category_id=c.id LEFT JOIN bookings b ON b.event_id=e.id AND b.status='CONFIRMED' GROUP BY c.id, c.name ORDER BY c.name");
                rq.setAttribute("catLabels", cc.get("labels")); rq.setAttribute("catData", cc.get("data"));
                rq.setAttribute("pendingList", EventDao.byStatus("PENDING"));
                rq.setAttribute("recentUsers", AdminDao.recentUsers(5));
                rq.setAttribute("recentBookings", BookingDao.recent(5));
                view(rq, rs, "admin/dashboard");
            }
            case "/events" -> {
                String st = str(rq, "status");
                if (!List.of("DRAFT", "PENDING", "APPROVED", "REJECTED", "CANCELLED", "COMPLETED").contains(st)) st = "";
                rq.setAttribute("status", st);
                rq.setAttribute("events", EventDao.byStatus(st));
                view(rq, rs, "admin/events");
            }
            case "/users" -> {
                String q = str(rq, "q"), role = str(rq, "role");
                if (!List.of("ADMIN", "ORGANIZER", "ATTENDEE").contains(role)) role = "";
                int page = Math.max(1, num(rq, "page", 1));
                Map<String, Object> r = UserDao.search(q, role, page, PAGE_SIZE);
                long total = (Long) r.get("total");
                rq.setAttribute("users", r.get("rows")); rq.setAttribute("total", total);
                rq.setAttribute("page", page); rq.setAttribute("pages", Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE));
                rq.setAttribute("q", q); rq.setAttribute("role", role);
                view(rq, rs, "admin/users");
            }
            case "/categories" -> { rq.setAttribute("cats", AdminDao.categoriesWithEvents()); view(rq, rs, "admin/categories"); }
            case "/settings" -> { rq.setAttribute("settings", AdminDao.settings()); view(rq, rs, "admin/settings"); }
            case "/activity" -> {
                rq.setAttribute("logs", AdminDao.auditLogs(100));
                rq.setAttribute("bookings", BookingDao.recent(20));
                view(rq, rs, "admin/activity");
            }
            default -> rs.sendError(404);
        }
    }

    @Override
    protected void post(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User admin = user(rq);
        switch (path(rq)) {
            case "/events/review" -> review(rq, rs, admin);
            case "/users/status" -> {
                long id = lng(rq, "id");
                String st = str(rq, "status");
                Map<String, Object> target = UserDao.byId(id);
                if (target == null || "ADMIN".equals(target.get("role"))) err(rq, "That user cannot be changed.");
                else if (!List.of("ACTIVE", "INACTIVE", "SUSPENDED").contains(st)) err(rq, "Invalid status.");
                else {
                    UserDao.setStatus(id, st);
                    AuditService.log(admin.getId(), "USER_STATUS", target.get("email") + " set to " + st);
                    ok(rq, "User " + target.get("name") + " is now " + st.toLowerCase() + ".");
                }
                redirect(rq, rs, "/admin/users");
            }
            case "/categories/add" -> {
                String name = str(rq, "name"), icon = str(rq, "icon");
                if (!icon.matches("^fa-[a-z0-9-]{2,35}$")) icon = "fa-calendar";
                if (!Validator.len(name, 2, 60)) err(rq, "Category name must be 2 to 60 characters.");
                else {
                    try {
                        AdminDao.addCategory(name, icon);
                        AuditService.log(admin.getId(), "CATEGORY_ADD", name);
                        ok(rq, "Category added.");
                    } catch (SQLException e) {
                        if (e.getErrorCode() == 1062) err(rq, "That category already exists."); else throw e;
                    }
                }
                redirect(rq, rs, "/admin/categories");
            }
            case "/categories/delete" -> {
                int n = AdminDao.deleteCategory(lng(rq, "id"));
                if (n > 0) { AuditService.log(admin.getId(), "CATEGORY_DELETE", "id " + lng(rq, "id")); ok(rq, "Category deleted."); }
                else err(rq, "Category is in use by events and cannot be deleted.");
                redirect(rq, rs, "/admin/categories");
            }
            case "/settings" -> {
                String site = str(rq, "site_name"), mail = str(rq, "support_email");
                int maxT = num(rq, "max_tickets_per_booking", -1), hours = num(rq, "cancellation_hours", -1);
                if (!Validator.len(site, 2, 60)) err(rq, "Site name must be 2 to 60 characters.");
                else if (!Validator.email(mail)) err(rq, "Enter a valid support email.");
                else if (maxT < 1 || maxT > 20) err(rq, "Max tickets per booking must be between 1 and 20.");
                else if (hours < 0 || hours > 720) err(rq, "Cancellation window must be between 0 and 720 hours.");
                else {
                    AdminDao.saveSetting("site_name", site);
                    AdminDao.saveSetting("support_email", mail);
                    AdminDao.saveSetting("max_tickets_per_booking", String.valueOf(maxT));
                    AdminDao.saveSetting("cancellation_hours", String.valueOf(hours));
                    AuditService.log(admin.getId(), "SETTINGS", "System settings updated");
                    ok(rq, "Settings saved.");
                }
                redirect(rq, rs, "/admin/settings");
            }
            default -> rs.sendError(404);
        }
    }

    private void review(HttpServletRequest rq, HttpServletResponse rs, User admin) throws Exception {
        long id = lng(rq, "id");
        String decision = str(rq, "decision"), feedback = str(rq, "feedback");
        Map<String, Object> ev = EventDao.byId(id);
        if (ev == null || !"PENDING".equals(ev.get("status"))) err(rq, "Only pending events can be reviewed.");
        else if ("reject".equals(decision) && feedback.isEmpty()) err(rq, "Please give feedback when rejecting an event.");
        else if (feedback.length() > 500) err(rq, "Feedback is too long (max 500 characters).");
        else {
            boolean approve = "approve".equals(decision);
            Db.update("UPDATE events SET status=?, admin_feedback=? WHERE id=?", approve ? "APPROVED" : "REJECTED", feedback.isEmpty() ? null : feedback, id);
            long org = ((Number) ev.get("organizer_id")).longValue();
            NotificationService.send(org, "Your event \"" + ev.get("title") + "\" was " + (approve ? "approved and is now published." : "rejected. Feedback: " + feedback));
            AuditService.log(admin.getId(), approve ? "EVENT_APPROVED" : "EVENT_REJECTED", "Event #" + id + " " + ev.get("title"));
            ok(rq, "Event " + (approve ? "approved." : "rejected."));
        }
        redirect(rq, rs, "/admin/events?status=PENDING");
    }
}
