package com.eventsphere.controller;

import com.eventsphere.dao.BookingDao;
import com.eventsphere.model.User;
import com.eventsphere.service.BookingService;
import com.eventsphere.service.BookingService.BookingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Attendee booking flow: book -> (pay, DEMO) -> confirmation, and cancellation.
 * Protected by AuthFilter (ATTENDEE role only).
 */
@WebServlet({"/attendee/book", "/attendee/pay", "/attendee/booking", "/attendee/cancel"})
public class BookingServlet extends BaseServlet {

    @Override
    protected void get(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        String path = rq.getServletPath();
        Map<String, Object> b = BookingDao.byRef(str(rq, "ref"), u.getId());   // ownership check: only my bookings
        if (b == null) { rs.sendError(404); return; }
        long bid = ((Number) b.get("id")).longValue();
        if ("/attendee/pay".equals(path) && !"PENDING".equals(b.get("status"))) {
            redirect(rq, rs, "/attendee/booking?ref=" + b.get("reference"));
            return;
        }
        rq.setAttribute("booking", b);
        rq.setAttribute("items", BookingDao.items(bid));
        if ("/attendee/booking".equals(path)) rq.setAttribute("payment", BookingDao.payment(bid));
        view(rq, rs, "/attendee/pay".equals(path) ? "attendee/pay" : "attendee/booking");
    }

    @Override
    protected void post(HttpServletRequest rq, HttpServletResponse rs) throws Exception {
        User u = user(rq);
        try {
            switch (rq.getServletPath()) {
                case "/attendee/book" -> {
                    long eventId = lng(rq, "event_id");
                    Map<Long, Integer> qty = new HashMap<>();
                    for (String k : rq.getParameterMap().keySet()) {
                        if (!k.startsWith("qty_")) continue;
                        try {
                            int q = Integer.parseInt(rq.getParameter(k).trim());
                            if (q > 0 && q <= 1000) qty.put(Long.parseLong(k.substring(4)), q);
                        } catch (NumberFormatException ignored) { }
                    }
                    try {
                        String ref = BookingService.create(u.getId(), eventId, qty);
                        Map<String, Object> b = BookingDao.byRef(ref, u.getId());
                        if ("CONFIRMED".equals(b.get("status"))) { ok(rq, "Registration confirmed!"); redirect(rq, rs, "/attendee/booking?ref=" + ref); }
                        else { ok(rq, "Booking created. Complete the (demo) payment to confirm it."); redirect(rq, rs, "/attendee/pay?ref=" + ref); }
                    } catch (BookingException e) {
                        err(rq, e.getMessage());
                        redirect(rq, rs, "/event?id=" + eventId);
                    }
                }
                case "/attendee/pay" -> {
                    String ref = str(rq, "ref");
                    String m = str(rq, "method");
                    String method = m.equals("CARD") || m.equals("NETBANKING") ? m : "UPI";
                    boolean success = "success".equals(str(rq, "outcome"));
                    BookingService.pay(u.getId(), ref, success, method + " (demo)");
                    if (success) ok(rq, "Demo payment successful. Booking confirmed!"); else err(rq, "Demo payment failed. Your seats were released.");
                    redirect(rq, rs, "/attendee/booking?ref=" + ref);
                }
                case "/attendee/cancel" -> {
                    BookingService.cancel(u.getId(), str(rq, "ref"));
                    ok(rq, "Booking cancelled.");
                    redirect(rq, rs, "/attendee/dashboard");
                }
                default -> rs.sendError(404);
            }
        } catch (BookingException e) {
            err(rq, e.getMessage());
            redirect(rq, rs, "/attendee/dashboard");
        }
    }
}
