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

-- -----------------------------------------------------------------
-- 7) TURNOS (Sistema de citas personalizadas)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS turnos (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    instructor_id BIGINT,
    room_id BIGINT,
    fecha_hora TIMESTAMPTZ NOT NULL,
    duracion TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    tipo_turno VARCHAR(100),
    observaciones TEXT,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_pattern VARCHAR(50),
    recurring_end_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_turnos_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_turnos_instructor FOREIGN KEY (instructor_id) REFERENCES instructors(id),
    CONSTRAINT fk_turnos_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT chk_turnos_status CHECK (status IN ('BOOKED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'))
);

-- Índices para optimizar consultas de turnos
CREATE INDEX IF NOT EXISTS idx_turnos_customer_id ON turnos(customer_id);
CREATE INDEX IF NOT EXISTS idx_turnos_instructor_id ON turnos(instructor_id);
CREATE INDEX IF NOT EXISTS idx_turnos_room_id ON turnos(room_id);
CREATE INDEX IF NOT EXISTS idx_turnos_fecha_hora ON turnos(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_turnos_status ON turnos(status);

-- -----------------------------------------------------------------
-- 8) PLANES DE ENTRENAMIENTO
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS planes_entrenamiento (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    nivel_dificultad VARCHAR(20),
    duracion_semanas INTEGER,
    objetivo VARCHAR(100),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    instructor_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_planes_entrenamiento_instructor FOREIGN KEY (instructor_id) REFERENCES instructors(id),
    CONSTRAINT chk_planes_entrenamiento_nivel CHECK (nivel_dificultad IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT chk_planes_entrenamiento_objetivo CHECK (objetivo IN ('PÉRDIDA DE PESO', 'GANANCIA DE MÚSCULO', 'RESISTENCIA', 'FLEXIBILIDAD', 'FUNCIONAL', 'COMPETICIÓN'))
);

-- Índices para optimizar consultas de planes
CREATE INDEX IF NOT EXISTS idx_planes_entrenamiento_instructor_id ON planes_entrenamiento(instructor_id);
CREATE INDEX IF NOT EXISTS idx_planes_entrenamiento_is_public ON planes_entrenamiento(is_public);
CREATE INDEX IF NOT EXISTS idx_planes_entrenamiento_is_active ON planes_entrenamiento(is_active);
CREATE INDEX IF NOT EXISTS idx_planes_entrenamiento_nivel_dificultad ON planes_entrenamiento(nivel_dificultad);
CREATE INDEX IF NOT EXISTS idx_planes_entrenamiento_objetivo ON planes_entrenamiento(objetivo);

-- -----------------------------------------------------------------
-- 9) VIDEOS DE ENTRENAMIENTO
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS videos_entrenamiento (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    url_video VARCHAR(500),
    archivo_video VARCHAR(500),
    duracion_segundos INTEGER,
    nivel_dificultad VARCHAR(20),
    musculos_trabajados VARCHAR(200),
    equipamiento_necesario VARCHAR(300),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    orden INTEGER,
    instructor_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_videos_entrenamiento_instructor FOREIGN KEY (instructor_id) REFERENCES instructors(id),
    CONSTRAINT chk_videos_entrenamiento_nivel CHECK (nivel_dificultad IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO'))
);

-- Índices para optimizar consultas de videos
CREATE INDEX IF NOT EXISTS idx_videos_entrenamiento_instructor_id ON videos_entrenamiento(instructor_id);
CREATE INDEX IF NOT EXISTS idx_videos_entrenamiento_is_public ON videos_entrenamiento(is_public);
CREATE INDEX IF NOT EXISTS idx_videos_entrenamiento_is_active ON videos_entrenamiento(is_active);
CREATE INDEX IF NOT EXISTS idx_videos_entrenamiento_nivel_dificultad ON videos_entrenamiento(nivel_dificultad);

-- -----------------------------------------------------------------
-- 10) EJERCICIOS DE PLAN
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ejercicios_plan (
    id BIGSERIAL PRIMARY KEY,
    plan_entrenamiento_id BIGINT NOT NULL,
    nombre_ejercicio VARCHAR(150) NOT NULL,
    descripcion TEXT,
    series INTEGER,
    repeticiones VARCHAR(50),
    peso_sugerido VARCHAR(100),
    descanso_segundos INTEGER,
    musculos_trabajados VARCHAR(200),
    equipamiento VARCHAR(200),
    orden INTEGER,
    dificultad VARCHAR(20),
    video_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ejercicios_plan_plan FOREIGN KEY (plan_entrenamiento_id) REFERENCES planes_entrenamiento(id) ON DELETE CASCADE,
    CONSTRAINT fk_ejercicios_plan_video FOREIGN KEY (video_id) REFERENCES videos_entrenamiento(id) ON DELETE SET NULL,
    CONSTRAINT chk_ejercicios_plan_dificultad CHECK (dificultad IN ('FÁCIL', 'MEDIO', 'DIFÍCIL'))
);

-- Índices para optimizar consultas de ejercicios
CREATE INDEX IF NOT EXISTS idx_ejercicios_plan_plan_id ON ejercicios_plan(plan_entrenamiento_id);
CREATE INDEX IF NOT EXISTS idx_ejercicios_plan_video_id ON ejercicios_plan(video_id);
CREATE INDEX IF NOT EXISTS idx_ejercicios_plan_orden ON ejercicios_plan(plan_entrenamiento_id, orden);

-- -----------------------------------------------------------------
-- 11) NOTIFICACIONES DE MEMBRESÍA
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notificaciones_membresia (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    membership_id BIGINT NOT NULL,
    tipo_notificacion VARCHAR(50) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT,
    fecha_vencimiento TIMESTAMPTZ,
    dias_restantes INTEGER,
    is_enviada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_envio TIMESTAMPTZ,
    canal_envio VARCHAR(20),
    is_leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_lectura TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notificaciones_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_notificaciones_membership FOREIGN KEY (membership_id) REFERENCES memberships(id),
    CONSTRAINT chk_notificaciones_tipo CHECK (tipo_notificacion IN ('VENCIMIENTO_5_DIAS', 'VENCIMIENTO_10_DIAS', 'VENCIDA', 'RENOVACION_EXITOSA')),
    CONSTRAINT chk_notificaciones_canal CHECK (canal_envio IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP'))
);

-- Índices para optimizar consultas de notificaciones
CREATE INDEX IF NOT EXISTS idx_notificaciones_customer_id ON notificaciones_membresia(customer_id);
CREATE INDEX IF NOT EXISTS idx_notificaciones_membership_id ON notificaciones_membresia(membership_id);
CREATE INDEX IF NOT EXISTS idx_notificaciones_tipo ON notificaciones_membresia(tipo_notificacion);
CREATE INDEX IF NOT EXISTS idx_notificaciones_is_enviada ON notificaciones_membresia(is_enviada);
CREATE INDEX IF NOT EXISTS idx_notificaciones_is_leida ON notificaciones_membresia(is_leida);

-- -----------------------------------------------------------------
-- TRIGGERS PARA ACTUALIZAR updated_at AUTOMÁTICAMENTE
-- -----------------------------------------------------------------

-- Función para actualizar updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para las nuevas tablas
CREATE TRIGGER trigger_update_turnos_updated_at
    BEFORE UPDATE ON turnos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_update_planes_entrenamiento_updated_at
    BEFORE UPDATE ON planes_entrenamiento
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_update_videos_entrenamiento_updated_at
    BEFORE UPDATE ON videos_entrenamiento
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_update_ejercicios_plan_updated_at
    BEFORE UPDATE ON ejercicios_plan
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_update_notificaciones_membresia_updated_at
    BEFORE UPDATE ON notificaciones_membresia
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();