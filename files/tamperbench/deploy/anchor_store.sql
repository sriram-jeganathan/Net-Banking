-- Configuration B anchor store (Decision D13/D14/D15).
--
-- CRITICAL: run this on a PostgreSQL instance in a SEPARATE trust domain from
-- the transaction database, with credentials the transaction-DB attacker
-- (Threat Model C) does not hold. Same-instance role separation does NOT
-- survive a privileged attacker (SET ROLE, ALTER, DROP TRIGGER, TRUNCATE), so
-- it is rejected. The separate trust domain is the primary control; the role
-- grant and guard trigger below are defense in depth.

CREATE TABLE anchor_log (
    seq         BIGINT PRIMARY KEY,
    digest      BYTEA  NOT NULL,
    anchored_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Append-only guard: reject UPDATE, DELETE, and TRUNCATE at statement level.
CREATE OR REPLACE FUNCTION anchor_guard() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'anchor_log is append-only: % rejected', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER anchor_no_mutation
    BEFORE UPDATE OR DELETE OR TRUNCATE ON anchor_log
    FOR EACH STATEMENT EXECUTE FUNCTION anchor_guard();

-- Writer role: INSERT and SELECT only. UPDATE/DELETE/TRUNCATE withheld.
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anchor_writer') THEN
        CREATE ROLE anchor_writer LOGIN PASSWORD 'CHANGE_ME_AT_DEPLOY';
    END IF;
END $$;
REVOKE ALL ON anchor_log FROM PUBLIC;
GRANT INSERT, SELECT ON anchor_log TO anchor_writer;
