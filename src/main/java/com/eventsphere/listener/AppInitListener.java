package com.eventsphere.listener;

import com.eventsphere.dao.EventDao;
import com.eventsphere.util.Db;
import com.eventsphere.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Runs at startup: opens the DB pool and sets real BCrypt hashes for the demo accounts. */
@WebListener
public class AppInitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent e) {
        try {
            Db.init();
            String[][] demo = {
                    {"admin@eventsphere.com", "Admin@123"},
                    {"organizer@eventsphere.com", "Organizer@123"},
                    {"attendee@eventsphere.com", "Attendee@123"}};
            for (String[] d : demo)
                Db.update("UPDATE users SET password_hash=? WHERE email=? AND password_hash='SEED'", PasswordUtil.hash(d[1]), d[0]);
            EventDao.markCompleted();
            e.getServletContext().log("EventSphere started: database connected.");
        } catch (Exception ex) {
            e.getServletContext().log("EventSphere could not connect to the database. Check db.properties and that MySQL is running.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) { Db.close(); }
}
