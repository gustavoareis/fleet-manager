-- Accent-insensitive search: typing "fabio" must find "Fábio", "jose" must find "José".
-- "unaccent" ships with PostgreSQL's contrib modules and is a trusted extension (PG 13+),
-- so the database owner can create it without superuser rights.
CREATE EXTENSION IF NOT EXISTS unaccent;
