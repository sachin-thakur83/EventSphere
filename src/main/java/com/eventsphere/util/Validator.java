package com.eventsphere.util;

import java.util.regex.Pattern;

/** Small server-side validation helpers. */
public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\- ]{7,15}$");
    private Validator() {}

    public static boolean email(String s) { return s != null && s.length() <= 150 && EMAIL.matcher(s).matches(); }
    public static boolean phone(String s) { return s == null || s.isEmpty() || PHONE.matcher(s).matches(); }
    public static boolean len(String s, int min, int max) { return s != null && s.length() >= min && s.length() <= max; }
    /** At least 8 characters with a letter and a digit. */
    public static boolean strongPassword(String p) {
        return p != null && p.length() >= 8 && p.length() <= 72 && p.matches(".*[A-Za-z].*") && p.matches(".*[0-9].*");
    }
    public static boolean httpUrl(String s) { return s != null && s.length() <= 500 && s.matches("^https?://[^\\s\"'<>]+$"); }
}
