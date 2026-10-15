package com.eventsphere.controller;

import com.eventsphere.dao.UserDao;
import com.eventsphere.model.User;
import com.eventsphere.service.AuditService;
import com.eventsphere.service.NotificationService;
import com.eventsphere.util.PasswordUtil;
import com.eventsphere.util.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;

/** Login, registration, logout and the role-based /dashboard redirect. */
@WebServlet({"/login", "/register", "/logout", "/dashboard"})
public class AuthServlet extends BaseServlet {

    @Override
    protected void get(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        switch (rq.getServletPath()) {
            case "/dashboard" -> {
                if (u == null) redirect(rq, rs, "/login"); else redirect(rq, rs, home(u));
            }
            case "/login" -> { if (u != null) redirect(rq, rs, home(u)); else view(rq, rs, "public/login"); }
            case "/register" -> { if (u != null) redirect(rq, rs, home(u)); else view(rq, rs, "public/register"); }
            default -> redirect(rq, rs, "/home");
        }
    }

    @Override
    protected void post(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        switch (rq.getServletPath()) {
            case "/login" -> login(rq, rs);
            case "/register" -> register(rq, rs);
            case "/logout" -> {
                HttpSession s = rq.getSession(false);
                if (s != null) s.invalidate();      // session invalidation on logout
                redirect(rq, rs, "/home");
            }
            default -> rs.sendError(404);
        }
    }

    private void login(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        String email = str(rq, "email").toLowerCase();
        String pw = rq.getParameter("password") == null ? "" : rq.getParameter("password");
        Map<String, Object> row = Validator.email(email) ? UserDao.findByEmail(email) : null;
        if (row == null || !PasswordUtil.verify(pw, (String) row.get("password_hash"))) {
            rq.setAttribute("error", "Invalid email or password.");   // same message for both cases on purpose
            rq.setAttribute("email", email);
            view(rq, rs, "public/login");
            return;
        }
        if (!"ACTIVE".equals(row.get("status"))) {
            rq.setAttribute("error", "Your account is " + String.valueOf(row.get("status")).toLowerCase() + ". Please contact the administrator.");
            rq.setAttribute("email", email);
            view(rq, rs, "public/login");
            return;
        }
        rq.changeSessionId();   // prevents session fixation
        User u = new User(((Number) row.get("id")).longValue(), (String) row.get("name"), (String) row.get("email"), (String) row.get("role"));
        rq.getSession().setAttribute("user", u);
        if ("ADMIN".equals(u.getRole())) AuditService.log(u.getId(), "LOGIN", "Administrator logged in");
        redirect(rq, rs, home(u));
    }

    private void register(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        String name = str(rq, "name"), email = str(rq, "email").toLowerCase(), phone = str(rq, "phone"), role = str(rq, "role");
        String pw = rq.getParameter("password") == null ? "" : rq.getParameter("password");
        String pw2 = rq.getParameter("confirm") == null ? "" : rq.getParameter("confirm");
        String error = null;
        if (!Validator.len(name, 2, 100)) error = "Name must be 2 to 100 characters.";
        else if (!Validator.email(email)) error = "Enter a valid email address.";
        else if (!Validator.phone(phone)) error = "Enter a valid phone number (digits, +, - only).";
        else if (!Validator.strongPassword(pw)) error = "Password must be at least 8 characters and contain a letter and a digit.";
        else if (!pw.equals(pw2)) error = "Passwords do not match.";
        else if (!"ATTENDEE".equals(role) && !"ORGANIZER".equals(role)) error = "Choose a valid account type.";   // admin cannot self-register
        if (error == null) {
            try {
                long id = UserDao.create(name, email, PasswordUtil.hash(pw), phone, role);
                NotificationService.send(id, "Welcome to EventSphere, " + name + "! Your account is ready.");
                ok(rq, "Registration successful. Please log in.");
                redirect(rq, rs, "/login");
                return;
            } catch (java.sql.SQLException e) {
                if (e instanceof SQLIntegrityConstraintViolationException || e.getErrorCode() == 1062) error = "An account with this email already exists.";
                else throw e;
            }
        }
        rq.setAttribute("error", error);
        rq.setAttribute("f_name", name); rq.setAttribute("f_email", email); rq.setAttribute("f_phone", phone); rq.setAttribute("f_role", role);
        view(rq, rs, "public/register");
    }
}
