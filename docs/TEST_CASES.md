# EventSphere - Test cases (manual)
Start with a freshly imported `eventsphere_db.sql`. "Expected" = what should happen.

| # | Area | Steps | Expected |
|---|---|---|---|
| T1 | Registration | /register, valid data, role Attendee | Redirect to login with success message; user row exists with BCrypt hash |
| T2 | Registration | Same email again | "An account with this email already exists." |
| T3 | Registration | Password `abc` / mismatched confirm | Validation error, form keeps entered values |
| T4 | Login | Demo attendee credentials | Lands on /attendee/dashboard |
| T5 | Login | Wrong password | "Invalid email or password." (same text for unknown email) |
| T6 | Login | Admin suspends attendee, attendee tries to log in | Login refused: account suspended |
| T7 | Roles | Logged in as attendee, open /admin/dashboard | 403 error page |
| T8 | Roles | Logged out, open /organizer/dashboard | Redirect to /login |
| T9 | Roles | Organizer opens `/organizer/event/manage?id=<event not owned>` | 403 (ownership check). Register a 2nd organizer to test |
| T10 | CSRF | Submit any POST form after deleting the `_csrf` hidden field (browser dev tools) | 403 |
| T11 | Organizer | Create event (valid), add ticket type, "Submit for approval" | Status DRAFT -> PENDING |
| T12 | Organizer | Submit event with no ticket types | "Add at least one ticket type..." |
| T13 | Organizer | Event with end time before start time | Validation error |
| T14 | Approval | Admin approves pending event | Status APPROVED, organizer gets notification, event shown on /events, audit log entry |
| T15 | Approval | Admin rejects without feedback | Error: feedback required |
| T16 | Approval | Admin rejects with feedback | REJECTED; organizer sees feedback and can edit and resubmit |
| T17 | Booking (free) | Attendee books "Startup Pitch Night" (Free Pass) x2 | CONFIRMED immediately, reference ES-XXXXXXXX, notification created |
| T18 | Booking (paid) | Book General x2 for Tech Summit | PENDING booking, redirected to demo payment page |
| T19 | Payment | Click "Simulate successful payment" | CONFIRMED, payment row SUCCESS with is_simulated=1, revenue increases |
| T20 | Payment | Click "Simulate failed payment" | FAILED, seats released (ticket `quantity_sold` back), no revenue |
| T21 | Payment | Re-open `/attendee/pay?ref=` of a confirmed booking | Redirected to the booking page; POST again -> "already processed" |
| T22 | Inventory | Event has 1 ticket left; two attendees book at the same time | Only one succeeds; other sees "Only 0 ... left" |
| T23 | Inventory | Request more tickets than left (edit qty in HTML) | Rejected by server |
| T24 | Inventory | Tamper price in the request | Ignored; total uses DB price |
| T25 | Duplicate | Book same event twice while first is active | "You already have an active booking..." |
| T26 | Cancellation | Cancel a confirmed booking for an event > 24 h away | CANCELLED, seats returned, payment REFUNDED (simulated), not counted in revenue |
| T27 | Cancellation | Set cancellation window to 720 h in Settings, cancel confirmed booking | Refused with window message |
| T28 | Deadline | Set `reg_deadline` in the past for an event (SQL), try to book | "The registration deadline has passed." |
| T29 | Organizer | Add announcement on an approved event | Confirmed attendees get notification; shown on event page |
| T30 | Organizer | Cancel an event with bookings | Event CANCELLED, all bookings CANCELLED, attendees notified |
| T31 | Search | /events with q, category, location, date, price filters and sort | Matching events; pagination links keep filters |
| T32 | Dashboards | Compare dashboard numbers with SQL `COUNT/SUM` | Values match (no hard-coded numbers) |
| T33 | XSS | Register with name `<script>alert(1)</script>` and view admin users | Shown as text, no script runs |
| T34 | Logout | Log out, press browser Back | Protected page is not shown from cache; session invalid |
| T35 | Profile | Change name and password (correct current password) | Saved; login works with new password |
| T36 | Contact | Submit contact form (valid / invalid) | Row in `contact_messages` / validation message |
