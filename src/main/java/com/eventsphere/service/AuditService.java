package com.eventsphere.service;

import com.eventsphere.util.Db;
import java.sql.SQLException;

/** Writes important actions to audit_logs (shown on the admin Activity page). */
public final class AuditService {
    private AuditService() {}

    public static void log(Long userId, String action, String details) throws SQLException {
        Db.update("INSERT INTO audit_logs(user_id,action,details) VALUES(?,?,?)", userId, action,
                details != null && details.length() > 490 ? details.substring(0, 490) : details);
    }
}
