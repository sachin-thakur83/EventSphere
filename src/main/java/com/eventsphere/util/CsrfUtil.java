package com.eventsphere.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** CSRF protection: a random token lives in the session and must come back in every POST form (_csrf). */
public final class CsrfUtil {
    private static final SecureRandom RNG = new SecureRandom();
    private CsrfUtil() {}

    public static String token(HttpSession s) {
        String t = (String) s.getAttribute("csrf");
        if (t == null) {
            byte[] b = new byte[24];
            RNG.nextBytes(b);
            t = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            s.setAttribute("csrf", t);
        }
        return t;
    }

    public static boolean valid(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        if (s == null) return false;
        String expected = (String) s.getAttribute("csrf");
        String got = r.getParameter("_csrf");
        return expected != null && got != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), got.getBytes(StandardCharsets.UTF_8));
    }
}
