package com.eventsphere.controller;

import com.eventsphere.dao.AdminDao;
import com.eventsphere.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** In-app notifications for any logged-in user. */
@WebServlet("/notifications")
public class NotificationServlet extends BaseServlet {

    @Override
    protected void get(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        if (u == null) { redirect(rq, rs, "/login"); return; }
        rq.setAttribute("notifications", AdminDao.notifications(u.getId(), 50));
        view(rq, rs, "common/notifications");
    }

    @Override
    protected void post(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        if (u == null) { redirect(rq, rs, "/login"); return; }
        AdminDao.markAllRead(u.getId());
        ok(rq, "All notifications marked as read.");
        redirect(rq, rs, "/notifications");
    }
}
