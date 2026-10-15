package com.eventsphere.dao;

import com.eventsphere.util.Db;
import java.sql.SQLException;
import java.util.*;

/** All SQL about users. */
public final class UserDao {
    private UserDao() {}

    public static Map<String, Object> findByEmail(String email) throws SQLException {
        return Db.queryOne("SELECT * FROM users WHERE email=?", email);
    }

    public static Map<String, Object> byId(long id) throws SQLException {
        return Db.queryOne("SELECT id,name,email,phone,role,status,created_at FROM users WHERE id=?", id);
    }

    public static long create(String name, String email, String hash, String phone, String role) throws SQLException {
        return Db.insert("INSERT INTO users(name,email,password_hash,phone,role) VALUES(?,?,?,?,?)", name, email, hash, phone.isEmpty() ? null : phone, role);
    }

    public static void setStatus(long id, String status) throws SQLException {
        Db.update("UPDATE users SET status=? WHERE id=? AND role<>'ADMIN'", status, id);
    }

    public static void updateProfile(long id, String name, String phone) throws SQLException {
        Db.update("UPDATE users SET name=?, phone=? WHERE id=?", name, phone.isEmpty() ? null : phone, id);
    }

    public static void updatePassword(long id, String hash) throws SQLException {
        Db.update("UPDATE users SET password_hash=? WHERE id=?", hash, id);
    }

    /** Search + filter + pagination. Returns {"rows": List, "total": Long}. */
    public static Map<String, Object> search(String q, String role, int page, int size) throws SQLException {
        StringBuilder w = new StringBuilder(" WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (!q.isEmpty()) { w.append(" AND (name LIKE ? OR email LIKE ?)"); p.add("%" + q + "%"); p.add("%" + q + "%"); }
        if (!role.isEmpty()) { w.append(" AND role=?"); p.add(role); }
        long total = Db.count("SELECT COUNT(*) FROM users" + w, p.toArray());
        p.add(size);
        p.add((page - 1) * size);
        List<Map<String, Object>> rows = Db.query("SELECT id,name,email,role,status,created_at FROM users" + w + " ORDER BY id DESC LIMIT ? OFFSET ?", p.toArray());
        Map<String, Object> r = new HashMap<>();
        r.put("rows", rows);
        r.put("total", total);
        return r;
    }
}
