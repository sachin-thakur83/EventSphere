package com.eventsphere.filter;

import com.eventsphere.model.User;
import com.eventsphere.util.Db;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Authentication + role-based authorization.
 * /admin/* -> ADMIN, /organizer/* -> ORGANIZER, /attendee/* -> ATTENDEE.
 * Also re-checks that the account is still ACTIVE on every request.
 */
@WebFilter({"/admin/*", "/organizer/*", "/attendee/*"})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse s = (HttpServletResponse) res;
        HttpSession session = r.getSession(false);
        User u = session == null ? null : (User) session.getAttribute("user");
        if (u == null) {
            r.getSession().setAttribute("flashErr", "Please log in to continue.");
            s.sendRedirect(r.getContextPath() + "/login");
            return;
        }
        String path = r.getServletPath();
        String need = path.startsWith("/admin") ? "ADMIN" : path.startsWith("/organizer") ? "ORGANIZER" : "ATTENDEE";
        if (!need.equals(u.getRole())) { s.sendError(403, "You do not have permission to view this page."); return; }
        try {
            Map<String, Object> row = Db.queryOne("SELECT status FROM users WHERE id=?", u.getId());
            if (row == null || !"ACTIVE".equals(row.get("status"))) {
                session.invalidate();
                r.getSession().setAttribute("flashErr", "Your account is not active. Contact the administrator.");
                s.sendRedirect(r.getContextPath() + "/login");
                return;
            }
        } catch (SQLException e) { throw new ServletException(e); }
        s.setHeader("Cache-Control", "no-store"); // back button must not show pages after logout
        chain.doFilter(req, res);
    }
}
