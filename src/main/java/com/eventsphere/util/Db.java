package com.eventsphere.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Database helper: connection pool (HikariCP) + small JDBC helpers.
 * All queries use PreparedStatement with ? placeholders (prevents SQL injection)
 * and try-with-resources (no leaked connections).
 * Query results are returned as List of Map (column name -> value), which JSP/EL can read directly.
 */
public final class Db {
    private static HikariDataSource ds;

    private Db() {}

    public static synchronized void init() {
        if (ds != null) return;
        Properties p = new Properties();
        try (InputStream in = Db.class.getResourceAsStream("/db.properties")) {
            p.load(in);
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Cannot read db.properties", e);
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setJdbcUrl(p.getProperty("db.url"));
        cfg.setUsername(p.getProperty("db.user"));
        cfg.setPassword(p.getProperty("db.password"));
        cfg.setMaximumPoolSize(10);
        ds = new HikariDataSource(cfg);
    }

    public static synchronized void close() {
        if (ds != null) { ds.close(); ds = null; }
    }

    public static Connection get() throws SQLException {
        if (ds == null) init();
        return ds.getConnection();
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }

    private static Object norm(Object o) {
        if (o instanceof LocalDateTime t) return Timestamp.valueOf(t);
        if (o instanceof LocalDate d) return java.sql.Date.valueOf(d);
        if (o instanceof LocalTime t) return Time.valueOf(t);
        return o;
    }

    // ---------- queries ----------
    public static List<Map<String, Object>> query(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) m.put(md.getColumnLabel(i), norm(rs.getObject(i)));
                    rows.add(m);
                }
                return rows;
            }
        }
    }

    public static List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        try (Connection c = get()) { return query(c, sql, params); }
    }

    public static Map<String, Object> queryOne(Connection c, String sql, Object... params) throws SQLException {
        List<Map<String, Object>> r = query(c, sql, params);
        return r.isEmpty() ? null : r.get(0);
    }

    public static Map<String, Object> queryOne(String sql, Object... params) throws SQLException {
        try (Connection c = get()) { return queryOne(c, sql, params); }
    }

    /** First column of first row (or null). */
    public static Object scalar(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getObject(1) : null; }
        }
    }

    public static Object scalar(String sql, Object... params) throws SQLException {
        try (Connection c = get()) { return scalar(c, sql, params); }
    }

    public static long count(Connection c, String sql, Object... params) throws SQLException {
        Object o = scalar(c, sql, params);
        return o == null ? 0 : ((Number) o).longValue();
    }

    public static long count(String sql, Object... params) throws SQLException {
        try (Connection c = get()) { return count(c, sql, params); }
    }

    // ---------- updates ----------
    public static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    public static int update(String sql, Object... params) throws SQLException {
        try (Connection c = get()) { return update(c, sql, params); }
    }

    /** INSERT and return the generated id. */
    public static long insert(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }

    public static long insert(String sql, Object... params) throws SQLException {
        try (Connection c = get()) { return insert(c, sql, params); }
    }

    /** For charts: query with 2 columns (label, number) -> {"labels":"a|b","data":"1,2"}. */
    public static Map<String, String> chart(String sql, Object... params) throws SQLException {
        StringBuilder l = new StringBuilder(), d = new StringBuilder();
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (l.length() > 0) { l.append('|'); d.append(','); }
                    l.append(String.valueOf(rs.getString(1)).replace("|", "/"));
                    d.append(rs.getLong(2));
                }
            }
        }
        Map<String, String> m = new HashMap<>();
        m.put("labels", l.toString());
        m.put("data", d.toString());
        return m;
    }
}
