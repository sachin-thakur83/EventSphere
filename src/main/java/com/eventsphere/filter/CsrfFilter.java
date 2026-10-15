package com.eventsphere.filter;

import com.eventsphere.util.CsrfUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Puts the CSRF token in every request and rejects POSTs with a missing/wrong token. */
@WebFilter("/*")
public class CsrfFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        String p = r.getServletPath();
        if (p.startsWith("/css") || p.startsWith("/js") || p.startsWith("/images")) { chain.doFilter(req, res); return; }
        r.setAttribute("csrf", CsrfUtil.token(r.getSession()));
        if ("POST".equals(r.getMethod()) && !CsrfUtil.valid(r)) {
            ((HttpServletResponse) res).sendError(403, "Invalid or missing security token. Please reload the page and try again.");
            return;
        }
        chain.doFilter(req, res);
    }
}
