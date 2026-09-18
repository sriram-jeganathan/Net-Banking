-- Transaction instance (port 5433, database bankdb). Holds the append-only
-- ledger, account balances, and the audit infrastructure for the trigger
-- mechanism. The hash-chain digest column (Configuration A) lives here and is
-- therefore within reach of a privileged Model C attacker on this instance.

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    id      TEXT PRIMARY KEY,
    balance BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE transactions (
    seq       BIGINT PRIMARY KEY,
    txid      TEXT   NOT NULL,
    type      TEXT   NOT NULL,
    src       TEXT   NOT NULL,
    dst       TEXT   NOT NULL,
    amount    BIGINT NOT NULL,
    ts_micros BIGINT NOT NULL,
    digest    BYTEA            -- Config A digest; NULL for baseline
);

CREATE TABLE audit_log (
    audit_id  BIGSERIAL PRIMARY KEY,
    op        TEXT   NOT NULL,
    seq       BIGINT,
    old_row   JSONB,
    new_row   JSONB,
    at_micros BIGINT NOT NULL,
    db_role   TEXT   NOT NULL DEFAULT current_user
);

CREATE OR REPLACE FUNCTION audit_fn() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log(op, seq, new_row, at_micros)
        VALUES ('INSERT', NEW.seq, to_jsonb(NEW),
                (extract(epoch from clock_timestamp()) * 1000000)::bigint);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_log(op, seq, old_row, new_row, at_micros)
        VALUES ('UPDATE', NEW.seq, to_jsonb(OLD), to_jsonb(NEW),
                (extract(epoch from clock_timestamp()) * 1000000)::bigint);
        RETURN NEW;
    ELSE
        INSERT INTO audit_log(op, seq, old_row, at_micros)
        VALUES ('DELETE', OLD.seq, to_jsonb(OLD),
                (extract(epoch from clock_timestamp()) * 1000000)::bigint);
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_trg
    AFTER INSERT OR UPDATE OR DELETE ON transactions
    FOR EACH ROW EXECUTE FUNCTION audit_fn();

-- The audit trigger is toggled per experiment (enabled only for the trigger
-- mechanism) so that audit overhead does not confound the other mechanisms.
ALTER TABLE transactions DISABLE TRIGGER audit_trg;
