-- =================================================================
-- INDUSTRIA: GYM / FITNESS
-- Tablas específicas del dominio de gimnasio
-- =================================================================

-- -----------------------------------------------------------------
-- 1) Catálogo: tipos de clase, instructores, salas
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS class_types (
                                           id BIGSERIAL PRIMARY KEY,
                                           name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    tags TEXT[],                       -- ej: {'yoga','dance'}
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS instructors (
                                           id BIGSERIAL PRIMARY KEY,
                                           name VARCHAR(120) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(50),
    bio TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );
CREATE INDEX IF NOT EXISTS ix_instructors_active ON instructors (is_active);

CREATE TABLE IF NOT EXISTS rooms (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(150),
    capacity_default INTEGER NOT NULL DEFAULT 20,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );

-- -----------------------------------------------------------------
-- 2) Clases (plantillas) y Sesiones (instancias)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS classes (
                                       id BIGSERIAL PRIMARY KEY,
                                       class_type_id BIGINT NOT NULL,
                                       instructor_id BIGINT,                  -- nullable en la entidad
                                       room_id BIGINT NOT NULL,
                                       rrule TEXT NOT NULL,                   -- RFC5545
                                       start_time_local TIME NOT NULL,
                                       duration_minutes INTEGER NOT NULL DEFAULT 60,
                                       capacity INTEGER,                      -- si NULL usa rooms.capacity_default
                                       start_date DATE NOT NULL,
                                       end_date DATE,                         -- NULL = sin fin
                                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                       created_by_user_id BIGINT,
                                       updated_by_user_id BIGINT,
                                       CONSTRAINT fk_class_type      FOREIGN KEY (class_type_id)   REFERENCES class_types (id)        ON DELETE RESTRICT,
    CONSTRAINT fk_class_instructor FOREIGN KEY (instructor_id)  REFERENCES instructors (id)        ON DELETE RESTRICT,
    CONSTRAINT fk_class_room      FOREIGN KEY (room_id)         REFERENCES rooms (id)              ON DELETE RESTRICT,
    CONSTRAINT fk_class_created_by FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL,
    CONSTRAINT fk_class_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );
CREATE INDEX IF NOT EXISTS ix_classes_active ON classes (is_active);

CREATE TABLE IF NOT EXISTS class_sessions (
                                              id BIGSERIAL PRIMARY KEY,
                                              class_id BIGINT NOT NULL,
                                              start_at TIMESTAMP NOT NULL,
                                              end_at   TIMESTAMP NOT NULL,
                                              status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED, DONE, CANCELLED
    capacity INTEGER NOT NULL,
    booked_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_class   FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE CASCADE,
    CONSTRAINT ck_session_status  CHECK (status IN ('SCHEDULED','DONE','CANCELLED')),
    CONSTRAINT uq_session_unique  UNIQUE (class_id, start_at)
    );
CREATE INDEX IF NOT EXISTS ix_sessions_time   ON class_sessions (start_at);
CREATE INDEX IF NOT EXISTS ix_sessions_status ON class_sessions (status);

-- -----------------------------------------------------------------
-- 3) Planes de suscripción
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subscription_plans (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    access_mode VARCHAR(30) NOT NULL DEFAULT 'UNLIMITED',         -- UNLIMITED | N_PER_WEEK | N_PER_MONTH
    visits_allowed INTEGER,                                       -- NULL si UNLIMITED
    reset_day_of_week SMALLINT,                                   -- 1..7 solo si N_PER_WEEK
    billing_period_months INTEGER NOT NULL DEFAULT 1,
    product_id BIGINT NOT NULL,                                   -- producto a facturar
    allowed_class_tags TEXT[],
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_product    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT ck_plan_access_mode CHECK (access_mode IN ('UNLIMITED','N_PER_WEEK','N_PER_MONTH')),
    CONSTRAINT ck_plan_week_fields CHECK (
(access_mode = 'N_PER_WEEK'  AND visits_allowed IS NOT NULL AND reset_day_of_week BETWEEN 1 AND 7) OR
(access_mode = 'N_PER_MONTH' AND visits_allowed IS NOT NULL AND reset_day_of_week IS NULL) OR
(access_mode = 'UNLIMITED'   AND visits_allowed IS NULL     AND reset_day_of_week IS NULL)
    )
    );
CREATE INDEX IF NOT EXISTS ix_subscription_plans_active ON subscription_plans (is_active);

-- -----------------------------------------------------------------
-- 4) Membresías
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS memberships (
                                           id BIGSERIAL PRIMARY KEY,
                                           customer_id BIGINT NOT NULL,
                                           plan_id BIGINT NOT NULL,
                                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',                  -- ACTIVE, PAUSED, EXPIRED, CANCELLED, TRIAL
    start_date DATE NOT NULL,
    end_date DATE,
    next_billing_date DATE,
    autopay BOOLEAN NOT NULL DEFAULT FALSE,
    preferred_payment_method VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT,
    updated_by_user_id BIGINT,
    CONSTRAINT fk_membership_customer   FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_membership_plan       FOREIGN KEY (plan_id)     REFERENCES subscription_plans (id) ON DELETE RESTRICT,
    CONSTRAINT fk_membership_created_by FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL,
    CONSTRAINT fk_membership_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL,
    CONSTRAINT ck_membership_status CHECK (status IN ('ACTIVE','PAUSED','EXPIRED','CANCELLED','TRIAL'))
    );
CREATE INDEX IF NOT EXISTS ix_memberships_customer     ON memberships (customer_id);
CREATE INDEX IF NOT EXISTS ix_memberships_status       ON memberships (status);
CREATE INDEX IF NOT EXISTS ix_memberships_next_billing ON memberships (next_billing_date);

-- -----------------------------------------------------------------
-- 5) Usos/consumos de membresía (auditoría de visitas)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS membership_usage_events (
                                                       id BIGSERIAL PRIMARY KEY,
                                                       membership_id BIGINT NOT NULL,
                                                       session_id BIGINT,                          -- puede ser NULL para ajuste manual
                                                       event_date DATE NOT NULL DEFAULT CURRENT_DATE,
                                                       units INTEGER NOT NULL DEFAULT 1,
                                                       reason VARCHAR(50),                          -- CHECK_IN, NO_SHOW, MANUAL_ADJUSTMENT, ...
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT,
    CONSTRAINT fk_usage_membership FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE CASCADE,
    CONSTRAINT fk_usage_session    FOREIGN KEY (session_id)    REFERENCES class_sessions (id) ON DELETE SET NULL,
    CONSTRAINT fk_usage_user       FOREIGN KEY (created_by_user_id) REFERENCES public.system_users (id) ON DELETE SET NULL
    );
CREATE INDEX IF NOT EXISTS ix_usage_membership ON membership_usage_events (membership_id, event_date);

-- -----------------------------------------------------------------
-- 6) Reservas
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
                                        id BIGSERIAL PRIMARY KEY,
                                        session_id BIGINT NOT NULL,
                                        customer_id BIGINT NOT NULL,
                                        membership_id BIGINT,                        -- NULL = drop-in
                                        status VARCHAR(20) NOT NULL DEFAULT 'BOOKED', -- BOOKED, WAITLISTED, CHECKED_IN, CANCELLED, NO_SHOW
    booked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    waitlist_position INTEGER,
    channel VARCHAR(30),                         -- APP, FRONT_DESK, PHONE, BACKOFFICE
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_session    FOREIGN KEY (session_id)   REFERENCES class_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_customer   FOREIGN KEY (customer_id)  REFERENCES customers (id)      ON DELETE RESTRICT,
    CONSTRAINT fk_booking_membership FOREIGN KEY (membership_id) REFERENCES memberships (id)    ON DELETE SET NULL,
    CONSTRAINT ck_booking_status CHECK (status IN ('BOOKED','WAITLISTED','CHECKED_IN','CANCELLED','NO_SHOW')),
    CONSTRAINT uq_booking_unique UNIQUE (session_id, customer_id)
    );
CREATE INDEX IF NOT EXISTS ix_bookings_session    ON bookings (session_id, status);
CREATE INDEX IF NOT EXISTS ix_bookings_membership ON bookings (membership_id);
