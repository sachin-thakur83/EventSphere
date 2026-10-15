# EventSphere - Online Event Management System

Java Servlets + JSP + JDBC + MySQL web application with three roles (Administrator, Organizer, Attendee).
Plain MVC, no Spring/Hibernate. Payments are a **clearly labeled demo simulation** (no real money, no card data stored).

## Honest status
- All Java sources were **compile-checked** (javac, against stubs of the servlet/Hikari/BCrypt APIs).
- The full app was **not run on Tomcat/MySQL by the author of this package**. JSP pages and SQL have not been executed yet.
  Expect to possibly fix small runtime typos on first run; use `docs/TEST_CASES.md` to verify each feature.

## Requirements
| Tool | Version |
|---|---|
| JDK | 17 (LTS) or newer |
| Maven | 3.9+ |
| Apache Tomcat | **10.1.x** (Jakarta EE 10, `jakarta.*` packages; Tomcat 9 will NOT work) |
| MySQL | 8.0.16+ (CHECK constraints are enforced) |

## Setup
1. **Create the database** (also loads demo data):
   ```
   mysql -u root -p < sql/eventsphere_db.sql
   ```
2. **Configure the connection** in `src/main/resources/db.properties` (url, user, password).
3. **Build**: `mvn clean package` -> produces `target/eventsphere.war`.
4. **Deploy**: copy the WAR to `<TOMCAT>/webapps/` and start Tomcat (`bin/startup.sh` or `bin/startup.bat`).
   IntelliJ/Eclipse: import as a Maven project, add a Tomcat 10.1 run configuration, deploy `eventsphere:war exploded`, context path `/eventsphere`.
5. Open **http://localhost:8080/eventsphere/**

On first start the app replaces the placeholder `SEED` password hashes of the demo users with real BCrypt hashes
(see `AppInitListener`). If MySQL is unreachable, Tomcat's log shows a clear message.

## Demo accounts (local testing only - change/remove before any real use)
| Role | Email | Password |
|---|---|---|
| Admin | admin@eventsphere.com | Admin@123 |
| Organizer | organizer@eventsphere.com | Organizer@123 |
| Attendee | attendee@eventsphere.com | Attendee@123 |

## Project structure
```
sql/eventsphere_db.sql            schema + sample data
src/main/java/com/eventsphere/
  model/       User (session object)
  dao/         UserDao, EventDao, BookingDao, AdminDao   (all SQL lives here)
  service/     BookingService (transactions), NotificationService, AuditService
  controller/  AuthServlet, PublicServlet, AttendeeServlet, BookingServlet,
               OrganizerServlet, AdminServlet, NotificationServlet, BaseServlet
  filter/      AuthFilter (login + role + active check), CsrfFilter
  util/        Db (HikariCP + JDBC helpers), PasswordUtil (BCrypt), CsrfUtil, Validator
  listener/    AppInitListener
src/main/webapp/
  WEB-INF/web.xml, WEB-INF/views/{common,public,admin,organizer,attendee}/*.jsp, WEB-INF/tags/eventCard.tag
  css/style.css, js/app.js
```
JSPs live under `WEB-INF` so they can only be reached through servlets. Scriptlets are disabled in `web.xml`; JSPs use EL + JSTL only.
Query results are `List<Map>` rows, which EL reads directly (`${e.title}`); only `User` is a typed model class, to keep the code short.

## Main URLs
Public: `/home /events /event?id= /about /contact /terms /privacy /login /register` -
Admin: `/admin/dashboard /admin/events /admin/users /admin/categories /admin/activity /admin/settings` -
Organizer: `/organizer/dashboard /organizer/event/new /organizer/event/manage?id=` -
Attendee: `/attendee/dashboard`, booking flow `/attendee/book -> /attendee/pay -> /attendee/booking`.

## Implemented features
- Registration, login, logout; BCrypt hashes; session fixation protection; CSRF token on every POST; role filter; active-account check on each request; no-store caching for protected pages.
- Admin: statistics from real queries + 2 Chart.js charts, event approve/reject with feedback, user search/filter/pagination and activate/deactivate/suspend, category CRUD, system settings (max tickets per booking, cancellation window), audit log, recent bookings.
- Organizer: create/edit events (draft), ticket types (price, quantity), submit for approval, cancel event, attendee list with search/status filter, announcements (-> in-app notifications), stats and chart. Ownership checked on every action.
- Attendee: search (name, category, location, date-from, free/paid, sort) with pagination, event details, multi-ticket booking, free registration, demo payment, unique booking reference (`ES-XXXXXXXX`), printable confirmation, cancellation rules, booking history, notifications, profile and password change.
- Booking safety: one transaction per booking; `SELECT ... FOR UPDATE` on event and ticket rows (no overselling); totals computed on the server from DB prices; seats are reserved when a booking is created and released on cancellation or failed payment; revenue counts only CONFIRMED bookings; one active booking per user per event; double payment blocked.

## Simulated payment - limitations
The payment page offers "Simulate successful / failed payment" buttons. It never contacts a gateway, never asks for card data, and refunds on cancellation are only a status change (`REFUNDED`). Replace `BookingService.pay()` with a real gateway (e.g. Razorpay/Stripe, using their hosted checkout and webhooks) for production.

## Known limitations
- **Forgot-password / reset flow is NOT implemented** (it needs email sending; omitted rather than done insecurely).
- Email is not sent; notifications are in-app only. To add email later: add `jakarta.mail` to the pom, create `util/MailUtil`, and call it inside `NotificationService.send(...)`; keep SMTP credentials in `db.properties`-style config, never in code.
- Pending (unpaid) bookings hold their seats until paid or cancelled; there is no automatic expiry job.
- Admin cannot edit a user's name/role or delete users (deactivate/suspend instead); no admin-created users.
- No image upload (banner is an http(s) URL); no favourites; attendee demographics chart not included.
- Event status changes to COMPLETED lazily (on startup and dashboard loads).
- Session cookie is `HttpOnly`; add `<secure>true</secure>` in `web.xml` when serving over HTTPS.
- Font Awesome and Chart.js load from CDNs, so the browser needs internet access.
- No rate limiting / account lockout on login.
