-- ============================================================
-- EventSphere database script
-- Run:  mysql -u root -p < eventsphere_db.sql
-- Demo users are inserted with password_hash = 'SEED'. On first startup the
-- application (AppInitListener) replaces it with a real BCrypt hash.
-- ============================================================
DROP DATABASE IF EXISTS eventsphere_db;
CREATE DATABASE eventsphere_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eventsphere_db;

-- Roles are a fixed set of three, so an ENUM column is used instead of a separate roles table.
CREATE TABLE users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  phone         VARCHAR(20),
  role          ENUM('ADMIN','ORGANIZER','ATTENDEE') NOT NULL,
  status        ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_users_role (role)
) ENGINE=InnoDB;

CREATE TABLE event_categories (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(60) NOT NULL UNIQUE,
  icon        VARCHAR(40) NOT NULL DEFAULT 'fa-calendar'
) ENGINE=InnoDB;

CREATE TABLE events (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  organizer_id   BIGINT NOT NULL,
  category_id    BIGINT NOT NULL,
  title          VARCHAR(150) NOT NULL,
  description    TEXT NOT NULL,
  banner_url     VARCHAR(500),
  venue          VARCHAR(255) NOT NULL,
  is_online      TINYINT(1) NOT NULL DEFAULT 0,
  event_date     DATE NOT NULL,
  start_time     TIME NOT NULL,
  end_time       TIME NOT NULL,
  reg_deadline   DATETIME NULL,
  capacity       INT NOT NULL,
  status         ENUM('DRAFT','PENDING','APPROVED','REJECTED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'DRAFT',
  admin_feedback VARCHAR(500),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ev_org FOREIGN KEY (organizer_id) REFERENCES users(id),
  CONSTRAINT fk_ev_cat FOREIGN KEY (category_id) REFERENCES event_categories(id),
  CONSTRAINT chk_capacity CHECK (capacity > 0),
  INDEX idx_ev_status_date (status, event_date),
  INDEX idx_ev_org (organizer_id)
) ENGINE=InnoDB;

CREATE TABLE ticket_types (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id       BIGINT NOT NULL,
  name           VARCHAR(60) NOT NULL,
  price          DECIMAL(10,2) NOT NULL DEFAULT 0,
  quantity_total INT NOT NULL,
  quantity_sold  INT NOT NULL DEFAULT 0,   -- includes seats reserved by PENDING bookings
  CONSTRAINT fk_tt_ev FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
  CONSTRAINT uq_tt UNIQUE (event_id, name),
  CONSTRAINT chk_tt_price CHECK (price >= 0),
  CONSTRAINT chk_tt_qty CHECK (quantity_sold >= 0 AND quantity_sold <= quantity_total)
) ENGINE=InnoDB;

CREATE TABLE bookings (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  reference    VARCHAR(20) NOT NULL UNIQUE,
  user_id      BIGINT NOT NULL,
  event_id     BIGINT NOT NULL,
  status       ENUM('PENDING','CONFIRMED','CANCELLED','FAILED') NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_bk_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_bk_ev FOREIGN KEY (event_id) REFERENCES events(id),
  INDEX idx_bk_user (user_id),
  INDEX idx_bk_event_status (event_id, status)
) ENGINE=InnoDB;

CREATE TABLE booking_items (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id     BIGINT NOT NULL,
  ticket_type_id BIGINT NOT NULL,
  quantity       INT NOT NULL,
  unit_price     DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_bi_bk FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  CONSTRAINT fk_bi_tt FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id),
  CONSTRAINT uq_bi UNIQUE (booking_id, ticket_type_id),
  CONSTRAINT chk_bi_qty CHECK (quantity > 0)
) ENGINE=InnoDB;

-- DEMO ONLY: payments are simulated. No card numbers / CVV are ever stored.
CREATE TABLE payments (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id   BIGINT NOT NULL,
  amount       DECIMAL(10,2) NOT NULL,
  method       VARCHAR(30) NOT NULL,
  status       ENUM('SUCCESS','FAILED','REFUNDED') NOT NULL,
  txn_ref      VARCHAR(30) NOT NULL UNIQUE,
  is_simulated TINYINT(1) NOT NULL DEFAULT 1,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pay_bk FOREIGN KEY (booking_id) REFERENCES bookings(id),
  INDEX idx_pay_booking (booking_id)
) ENGINE=InnoDB;

CREATE TABLE notifications (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT NOT NULL,
  message    VARCHAR(500) NOT NULL,
  is_read    TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_nt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_nt_user (user_id, is_read)
) ENGINE=InnoDB;

CREATE TABLE event_announcements (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id   BIGINT NOT NULL,
  title      VARCHAR(120) NOT NULL,
  message    VARCHAR(1000) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_an_ev FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE contact_messages (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  email      VARCHAR(150) NOT NULL,
  subject    VARCHAR(150) NOT NULL,
  message    VARCHAR(2000) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE system_settings (
  setting_key   VARCHAR(60) PRIMARY KEY,
  setting_value VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT NULL,
  action     VARCHAR(60) NOT NULL,
  details    VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_al_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
  INDEX idx_al_created (created_at)
) ENGINE=InnoDB;

-- ---------------- Sample data ----------------
INSERT INTO system_settings VALUES
 ('site_name','EventSphere'),('support_email','support@eventsphere.local'),
 ('max_tickets_per_booking','10'),('cancellation_hours','24');

INSERT INTO users (id,name,email,password_hash,phone,role) VALUES
 (1,'Admin User','admin@eventsphere.com','SEED','9000000001','ADMIN'),
 (2,'Olivia Organizer','organizer@eventsphere.com','SEED','9000000002','ORGANIZER'),
 (3,'Arjun Attendee','attendee@eventsphere.com','SEED','9000000003','ATTENDEE');

INSERT INTO event_categories (id,name,icon) VALUES
 (1,'Technology','fa-laptop-code'),(2,'Business','fa-briefcase'),(3,'Music','fa-music'),
 (4,'Education','fa-graduation-cap'),(5,'Sports','fa-person-running'),
 (6,'Arts & Culture','fa-palette'),(7,'Health & Wellness','fa-heart-pulse');

INSERT INTO events (id,organizer_id,category_id,title,description,banner_url,venue,is_online,event_date,start_time,end_time,reg_deadline,capacity,status) VALUES
 (1,2,1,'Tech Innovation Summit 2026','A full-day summit with keynotes on AI, cloud and cybersecurity, plus startup demos and networking.',NULL,'Bengaluru International Exhibition Centre, Bengaluru',0,DATE_ADD(CURDATE(),INTERVAL 21 DAY),'09:30','17:30',DATE_ADD(NOW(),INTERVAL 18 DAY),300,'APPROVED'),
 (2,2,2,'Startup Pitch Night (Online)','Ten early-stage founders pitch to a panel of investors. Free to attend, join from anywhere.',NULL,'Online - meeting link is emailed after registration',1,DATE_ADD(CURDATE(),INTERVAL 10 DAY),'18:00','20:00',DATE_ADD(NOW(),INTERVAL 9 DAY),200,'APPROVED'),
 (3,2,3,'Indie Music Fest','An open-air evening with six indie bands, food stalls and art installations.',NULL,'Jawaharlal Nehru Stadium Grounds, Chennai',0,DATE_ADD(CURDATE(),INTERVAL 35 DAY),'16:00','22:30',DATE_ADD(NOW(),INTERVAL 30 DAY),1000,'APPROVED'),
 (4,2,4,'Python Workshop for Beginners','Hands-on workshop: variables, loops, functions and a small project. Bring your laptop.',NULL,'Room 204, Tech Park, Pune',0,DATE_ADD(CURDATE(),INTERVAL 14 DAY),'10:00','16:00',DATE_ADD(NOW(),INTERVAL 12 DAY),60,'APPROVED'),
 (5,2,5,'City Marathon 5K','A friendly 5K fun run for all ages. Includes t-shirt, medal and refreshments.',NULL,'Marine Drive, Mumbai',0,DATE_ADD(CURDATE(),INTERVAL 28 DAY),'06:00','09:00',DATE_ADD(NOW(),INTERVAL 25 DAY),500,'APPROVED'),
 (6,2,4,'Design Thinking Bootcamp','Two-hour intensive on user research and rapid prototyping. Waiting for admin approval.',NULL,'Innovation Lab, Hyderabad',0,DATE_ADD(CURDATE(),INTERVAL 40 DAY),'11:00','13:00',DATE_ADD(NOW(),INTERVAL 38 DAY),40,'PENDING'),
 (7,2,6,'Art and Culture Expo','Draft event: exhibition of regional artists and craft makers.',NULL,'Town Hall, Kolkata',0,DATE_ADD(CURDATE(),INTERVAL 50 DAY),'10:00','19:00',NULL,250,'DRAFT');

INSERT INTO ticket_types (id,event_id,name,price,quantity_total,quantity_sold) VALUES
 (1,1,'General',499.00,150,2),(2,1,'VIP',1499.00,50,0),
 (3,2,'Free Pass',0.00,200,0),
 (4,3,'General',799.00,800,0),(5,3,'VIP',2499.00,100,0),
 (6,4,'Standard',299.00,60,0),
 (7,5,'Runner',199.00,500,0),
 (8,6,'Standard',999.00,40,0),
 (9,7,'Entry',150.00,250,0);

-- One confirmed demo booking for the demo attendee
INSERT INTO bookings (id,reference,user_id,event_id,status,total_amount) VALUES (1,'ES-DEMO0001',3,1,'CONFIRMED',998.00);
INSERT INTO booking_items (booking_id,ticket_type_id,quantity,unit_price) VALUES (1,1,2,499.00);
INSERT INTO payments (booking_id,amount,method,status,txn_ref,is_simulated) VALUES (1,998.00,'UPI','SUCCESS','SIM-DEMO0001',1);
INSERT INTO notifications (user_id,message) VALUES
 (3,'Welcome to EventSphere! Your demo booking ES-DEMO0001 is confirmed.'),
 (2,'Your event "Tech Innovation Summit 2026" was approved.');
INSERT INTO audit_logs (user_id,action,details) VALUES (1,'SEED','Database initialised with demo data');
