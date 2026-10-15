# EventSphere - Online Event Management System: Project Report

## 1. Abstract
EventSphere is a web-based event management platform that connects administrators, event organizers and attendees. Organizers create events and ticket types, administrators approve events and oversee the platform, and attendees discover events, book tickets and receive updates. It is built with Java Servlets, JSP, JDBC and MySQL following the MVC pattern, with role-based access control, secure password hashing and transactional ticket booking.

## 2. Objectives
- Provide separate, role-protected dashboards for three user types.
- Let organizers publish events and sell tickets with an approval workflow.
- Make booking reliable: no overselling, correct revenue, safe cancellation.
- Apply core web-security practices (BCrypt, prepared statements, CSRF tokens, output escaping, access control).

## 3. Scope
In scope: user registration/login, event creation and approval, ticket types, search and booking, simulated payment, booking confirmation, cancellation, in-app notifications, announcements, statistics, audit log, system settings.
Out of scope: real payment gateway, email delivery, password-reset by email, image upload, mobile app.

## 4. Technology stack
Java 17, Servlets/JSP (Jakarta EE 10), JSTL, JDBC with HikariCP, MySQL 8, Apache Tomcat 10.1, Maven, BCrypt (jBCrypt), HTML5/CSS3/JavaScript, Chart.js, Font Awesome.

## 5. System architecture
Browser -> Filters (CSRF, Auth/Role) -> Controller servlets -> Service (business rules, transactions) -> DAO (JDBC/SQL) -> MySQL. Controllers forward request attributes to JSP views under `WEB-INF/views`, which render using EL/JSTL. See README for package layout.

## 6. Database design (`eventsphere_db`)
users, event_categories, events, ticket_types, bookings, booking_items, payments, notifications, event_announcements, contact_messages, system_settings, audit_logs.
Relationships: users 1-N events (organizer); event_categories 1-N events; events 1-N ticket_types; users 1-N bookings; events 1-N bookings; bookings 1-N booking_items N-1 ticket_types; bookings 1-N payments; users 1-N notifications; events 1-N event_announcements.
Constraints: unique email, unique booking reference, unique (event, ticket name), CHECKs for price >= 0 and 0 <= quantity_sold <= quantity_total, foreign keys, indexes on status/date/user columns. Roles are an ENUM column (fixed set of three).

## 7. Modules
- **Authentication & security:** registration, login, logout, filters, CSRF, validation.
- **Administrator:** dashboard, approvals, users, categories, settings, activity.
- **Organizer:** events, tickets, attendees, announcements, statistics.
- **Attendee:** search, booking, demo payment, confirmations, history, notifications, profile.
- **Booking engine:** `BookingService` - create/pay/cancel in single transactions using `SELECT ... FOR UPDATE` to serialize concurrent bookings.

## 8. Testing
Manual test cases are in `docs/TEST_CASES.md` (registration, login, role permissions, approval, booking, inventory, cancellation, security). Java sources were compile-checked; end-to-end execution on Tomcat/MySQL must be performed using those test cases.

## 9. Limitations
See README "Known limitations" (no password reset, no email, simulated payments, no pending-booking expiry).

## 10. Conclusion
EventSphere demonstrates a complete multi-role, database-driven Java web application with attention to data integrity and security. Future work: real payment gateway, email notifications, password reset, image uploads, expiry of unpaid bookings and richer analytics.
