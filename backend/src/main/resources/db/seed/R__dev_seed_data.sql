-- Sample data for local development (loaded only by the "dev" profile).
--
-- Repeatable migration (R__): Flyway runs it after all versioned migrations and again whenever
-- this file changes. Every INSERT is idempotent (ON CONFLICT DO NOTHING), so re-running never
-- duplicates rows. A versioned seed (e.g. V100__seed) would block future schema migrations V2, V3...
--
-- All test accounts use the password: Senha@123

-- ---------------------------------------------------------------- users
INSERT INTO users (email, password_hash, role) VALUES
    ('admin@medagenda.local',          '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'ADMIN'),
    ('recepcao@medagenda.local',       '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'RECEPTIONIST'),
    ('medico@medagenda.local',         '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'DOCTOR'),
    ('bruno.lima@medagenda.local',     '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'DOCTOR'),
    ('carla.mendes@medagenda.local',   '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'DOCTOR'),
    ('daniel.rocha@medagenda.local',   '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'DOCTOR'),
    ('elisa.martins@medagenda.local',  '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'DOCTOR'),
    ('fabio.souza@medagenda.local',    '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'DOCTOR'),
    ('paciente@medagenda.local',       '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'PATIENT'),
    ('maria.silva@medagenda.local',    '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'PATIENT'),
    ('pedro.alves@medagenda.local',    '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'PATIENT'),
    ('lucia.ferreira@medagenda.local', '$2a$10$0MgSZ.cqQJ6N4J8NTlxK6.cjSswxym5aBcxvTWcWQvabH9f4Ti0fe', 'PATIENT')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- specialties / insurances
INSERT INTO specialties (name) VALUES
    ('Cardiologia'), ('Clínica Geral'), ('Dermatologia'), ('Ginecologia'),
    ('Neurologia'), ('Oftalmologia'), ('Ortopedia'), ('Pediatria')
ON CONFLICT DO NOTHING;

-- Fictitious insurance names
INSERT INTO health_insurances (name, active) VALUES
    ('Vida Plena Saúde', TRUE),
    ('Bem Cuidar', TRUE),
    ('Saúde Integral', TRUE),
    ('Previna Mais', TRUE),
    ('Conviva (descontinuado)', FALSE)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- doctors
INSERT INTO doctors (name, crm, specialty_id, user_id)
SELECT d.name, d.crm, s.id, u.id
FROM (VALUES
    ('Dra. Ana Cardoso',     '123456/SP', 'Cardiologia',   'medico@medagenda.local'),
    ('Dr. Bruno Lima',       '234567/SP', 'Dermatologia',  'bruno.lima@medagenda.local'),
    ('Dra. Carla Mendes',    '345678/RJ', 'Pediatria',     'carla.mendes@medagenda.local'),
    ('Dr. Daniel Rocha',     '456789/MG', 'Ortopedia',     'daniel.rocha@medagenda.local'),
    ('Dra. Elisa Martins',   '567890/SP', 'Clínica Geral', 'elisa.martins@medagenda.local'),
    ('Dr. Fábio Souza',      '678901/PR', 'Cardiologia',   'fabio.souza@medagenda.local')
) AS d (name, crm, specialty, email)
JOIN specialties s ON s.name = d.specialty
JOIN users u ON u.email = d.email
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- availabilities
INSERT INTO doctor_availabilities (doctor_id, day_of_week, start_time, end_time, slot_duration_minutes)
SELECT doc.id, a.day, a.start_time::time, a.end_time::time, a.duration
FROM (VALUES
    ('123456/SP', 'MONDAY',    '08:00', '12:00', 30),
    ('123456/SP', 'TUESDAY',   '08:00', '12:00', 30),
    ('123456/SP', 'WEDNESDAY', '08:00', '12:00', 30),
    ('123456/SP', 'WEDNESDAY', '14:00', '18:00', 30),
    ('123456/SP', 'THURSDAY',  '08:00', '12:00', 30),
    ('123456/SP', 'FRIDAY',    '08:00', '12:00', 30),
    ('234567/SP', 'TUESDAY',   '13:00', '17:00', 40),
    ('234567/SP', 'THURSDAY',  '13:00', '17:00', 40),
    ('345678/RJ', 'MONDAY',    '09:00', '17:00', 30),
    ('345678/RJ', 'WEDNESDAY', '09:00', '17:00', 30),
    ('345678/RJ', 'FRIDAY',    '09:00', '13:00', 30),
    ('456789/MG', 'TUESDAY',   '08:00', '12:00', 20),
    ('456789/MG', 'THURSDAY',  '08:00', '12:00', 20),
    ('456789/MG', 'SATURDAY',  '08:00', '11:00', 20),
    ('567890/SP', 'MONDAY',    '13:00', '19:00', 20),
    ('567890/SP', 'TUESDAY',   '13:00', '19:00', 20),
    ('567890/SP', 'WEDNESDAY', '13:00', '19:00', 20),
    ('567890/SP', 'THURSDAY',  '13:00', '19:00', 20),
    ('567890/SP', 'FRIDAY',    '13:00', '19:00', 20),
    ('678901/PR', 'MONDAY',    '14:00', '18:00', 45),
    ('678901/PR', 'FRIDAY',    '14:00', '18:00', 45)
) AS a (crm, day, start_time, end_time, duration)
JOIN doctors doc ON doc.crm = a.crm
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- patients (fictitious, CPFs with valid check digits)
INSERT INTO patients (name, cpf, phone, birth_date, health_insurance_id, user_id)
SELECT p.name, p.cpf, p.phone, p.birth_date::date, h.id, u.id
FROM (VALUES
    ('João Pereira',   '12345678909', '11987654321', '1988-04-12', 'Vida Plena Saúde', 'paciente@medagenda.local'),
    ('Maria Silva',    '98765432100', '11976543210', '1995-09-30', 'Bem Cuidar',       'maria.silva@medagenda.local'),
    ('Pedro Alves',    '11144477735', '21965432109', '1972-01-05', NULL,               'pedro.alves@medagenda.local'),
    ('Lúcia Ferreira', '24681357928', '3134567890',  '2015-06-21', 'Saúde Integral',   'lucia.ferreira@medagenda.local')
) AS p (name, cpf, phone, birth_date, insurance, email)
JOIN users u ON u.email = p.email
LEFT JOIN health_insurances h ON h.name = p.insurance
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- appointments
-- Dates are relative to the day the seed runs, so the demo always has history and upcoming
-- appointments. Every slot falls inside the doctor's availability.
WITH ref AS (
    SELECT current_date - (extract(isodow FROM current_date)::int - 1) - 7 AS last_monday,
           current_date + (8 - extract(isodow FROM current_date)::int)     AS next_monday
)
INSERT INTO appointments (patient_id, doctor_id, scheduled_at, status, notes)
SELECT pat.id, doc.id, a.scheduled_at, a.status, a.notes
FROM ref
CROSS JOIN LATERAL (VALUES
    ('12345678909', '123456/SP', ref.last_monday + time '09:00',     'COMPLETED',
        'Pressão 12x8. Solicitado ecocardiograma de rotina.'),
    ('12345678909', '234567/SP', ref.last_monday + 1 + time '14:20', 'NO_SHOW',   NULL),
    ('98765432100', '567890/SP', ref.last_monday + 2 + time '13:00', 'COMPLETED',
        'Check-up anual sem alterações.'),
    ('12345678909', '123456/SP', ref.next_monday + time '09:00',     'SCHEDULED', NULL),
    ('98765432100', '123456/SP', ref.next_monday + time '09:30',     'SCHEDULED', NULL),
    ('11144477735', '123456/SP', ref.next_monday + 1 + time '10:00', 'SCHEDULED', NULL),
    ('24681357928', '345678/RJ', ref.next_monday + 2 + time '10:30', 'SCHEDULED', NULL),
    ('11144477735', '456789/MG', ref.next_monday + 3 + time '08:40', 'CANCELLED', NULL)
) AS a (cpf, crm, scheduled_at, status, notes)
JOIN patients pat ON pat.cpf = a.cpf
JOIN doctors doc ON doc.crm = a.crm
-- Cancelled rows are outside the partial unique indexes, so ON CONFLICT alone would not dedupe them
WHERE NOT EXISTS (SELECT 1 FROM appointments x WHERE x.patient_id = pat.id AND x.scheduled_at = a.scheduled_at)
ON CONFLICT DO NOTHING;

-- Today's agenda for the reception screen (only on weekdays, inside Dr. Ana's morning window)
INSERT INTO appointments (patient_id, doctor_id, scheduled_at, status)
SELECT pat.id, doc.id, current_date + a.slot, 'SCHEDULED'
FROM (VALUES
    ('98765432100', time '10:30'),
    ('11144477735', time '11:00'),
    ('24681357928', time '11:30')
) AS a (cpf, slot)
JOIN patients pat ON pat.cpf = a.cpf
JOIN doctors doc ON doc.crm = '123456/SP'
WHERE extract(isodow FROM current_date) BETWEEN 1 AND 5
ON CONFLICT DO NOTHING;
