package com.eventsphere.controller;

import com.eventsphere.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Shared helpers for all controllers. Subclasses override get()/post(); unexpected errors become the friendly 500 page. */
public abstract class BaseServlet extends HttpServlet {

    protected void get(HttpServletRequest rq, HttpServletResponse rs) throws Exception { rs.sendError(404); }
    protected void post(HttpServletRequest rq, HttpServletResponse rs) throws Exception { rs.sendError(404); }

    @Override
    protected final void doGet(HttpServletRequest rq, HttpServletResponse rs) throws ServletException, IOException {
        try { get(rq, rs); }
        catch (ServletException | IOException e) { throw e; }
        catch (Exception e) { throw new ServletException(e); }
    }

    @Override
    protected final void doPost(HttpServletRequest rq, HttpServletResponse rs) throws ServletException, IOException {
        rq.setCharacterEncoding("UTF-8");
        try { post(rq, rs); }
        catch (ServletException | IOException e) { throw e; }
        catch (Exception e) { throw new ServletException(e); }
    }

    protected User user(HttpServletRequest rq) {
        HttpSession s = rq.getSession(false);
        return s == null ? null : (User) s.getAttribute("user");
    }

    protected void ok(HttpServletRequest rq, String msg) { rq.getSession().setAttribute("flashOk", msg); }
    protected void err(HttpServletRequest rq, String msg) { rq.getSession().setAttribute("flashErr", msg); }

    protected void view(HttpServletRequest rq, HttpServletResponse rs, String name) throws ServletException, IOException {
        rq.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(rq, rs);
    }

    protected void redirect(HttpServletRequest rq, HttpServletResponse rs, String path) throws IOException {
        rs.sendRedirect(rq.getContextPath() + path);
    }

    protected long lng(HttpServletRequest rq, String name) {
        try { return Long.parseLong(rq.getParameter(name).trim()); } catch (Exception e) { return -1; }
    }

    protected int num(HttpServletRequest rq, String name, int def) {
        try { return Integer.parseInt(rq.getParameter(name).trim()); } catch (Exception e) { return def; }
    }

    protected String str(HttpServletRequest rq, String name) {
        String v = rq.getParameter(name);
        return v == null ? "" : v.trim();
    }

    protected String path(HttpServletRequest rq) {
        String p = rq.getPathInfo();
        return p == null ? "/" : p;
    }

    protected String home(User u) {
        return switch (u.getRole()) {
            case "ADMIN" -> "/admin/dashboard";
            case "ORGANIZER" -> "/organizer/dashboard";
            default -> "/attendee/dashboard";
        };
    }
}
