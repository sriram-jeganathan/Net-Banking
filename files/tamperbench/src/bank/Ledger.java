package bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Applies the banking workload to the transaction instance with real account
 * balances, and, depending on the mechanism, computes and stores the hash-chain
 * digest and/or anchors it. The four mechanisms:
 *   BASELINE  no integrity metadata.
 *   HASH_A    digest stored in the transaction row (same trust boundary).
 *   HASH_B    digest stored in the row AND anchored to the separate instance.
 *   TRIGGERS  audit trigger enabled; every change is logged.
 */
public final class Ledger {

    public enum Mechanism { BASELINE, HASH_A, HASH_B, TRIGGERS }

    private final byte[] seed;

    public Ledger(byte[] seed) { this.seed = seed.clone(); }

    /** Reset the transaction instance and pre-create the account population. */
    public void resetTxn(Connection txn, int accounts, Mechanism m) throws SQLException {
        try (Statement st = txn.createStatement()) {
            st.execute("TRUNCATE transactions, audit_log");
            st.execute("DELETE FROM accounts");
            if (m == Mechanism.TRIGGERS) {
                st.execute("ALTER TABLE transactions ENABLE TRIGGER audit_trg");
            } else {
                st.execute("ALTER TABLE transactions DISABLE TRIGGER audit_trg");
            }
        }
        try (PreparedStatement ps = txn.prepareStatement(
                "INSERT INTO accounts(id, balance) VALUES (?, 0)")) {
            for (int i = 0; i < accounts; i++) {
                ps.setString(1, "acct-" + i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        txn.commit();
    }

    /** Reset the anchor instance (admin action, distinct from the attacker). */
    public void resetAnchor(Connection anchor) throws SQLException {
        try (Statement st = anchor.createStatement()) {
            st.execute("ALTER TABLE anchor_log DISABLE TRIGGER anchor_no_mutation");
            st.execute("TRUNCATE anchor_log");
            st.execute("ALTER TABLE anchor_log ENABLE TRIGGER anchor_no_mutation");
        }
        anchor.commit();
    }

    /**
     * Load the workload. Returns nothing; the ledger lives in the databases.
     * Latencies (nanoseconds per applied transaction) are appended to `lat` when
     * non-null, for the performance harness.
     */
    public void load(Connection txn, PostgresAnchor anchor, Mechanism m,
                     List<Tx> txs, long[] lat) throws SQLException {
        PreparedStatement ins = txn.prepareStatement(
                "INSERT INTO transactions(seq,txid,type,src,dst,amount,ts_micros,digest) " +
                "VALUES (?,?,?,?,?,?,?,?)");
        PreparedStatement create = txn.prepareStatement(
                "INSERT INTO accounts(id,balance) VALUES (?,?) ON CONFLICT (id) DO NOTHING");
        PreparedStatement credit = txn.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE id = ?");
        PreparedStatement debit = txn.prepareStatement(
                "UPDATE accounts SET balance = balance - ? WHERE id = ?");

        byte[] prev = seed.clone();
        int i = 0;
        for (Tx t : txs) {
            long t0 = System.nanoTime();
            byte[] digest = null;
            if (m == Mechanism.HASH_A || m == Mechanism.HASH_B) {
                digest = HashChain.link(CanonicalSerializer.serialize(t), prev);
                prev = digest;
            }
            ins.setLong(1, t.seq());
            ins.setString(2, t.txid());
            ins.setString(3, t.type());
            ins.setString(4, t.src());
            ins.setString(5, t.dst());
            ins.setLong(6, t.amount());
            ins.setLong(7, t.timestampMicros());
            ins.setBytes(8, digest);
            ins.executeUpdate();

            switch (t.type()) {
                case "CREATE" -> { create.setString(1, t.src()); create.setLong(2, t.amount()); create.executeUpdate(); }
                case "DEPOSIT" -> { credit.setLong(1, t.amount()); credit.setString(2, t.src()); credit.executeUpdate(); }
                case "WITHDRAW" -> { debit.setLong(1, t.amount()); debit.setString(2, t.src()); debit.executeUpdate(); }
                case "TRANSFER" -> {
                    debit.setLong(1, t.amount()); debit.setString(2, t.src()); debit.executeUpdate();
                    credit.setLong(1, t.amount()); credit.setString(2, t.dst()); credit.executeUpdate();
                }
                default -> throw new IllegalStateException("unknown type " + t.type());
            }
            if (m == Mechanism.HASH_B) anchor.append(t.seq(), digest);

            if ((i % 500) == 499) { txn.commit(); if (anchor != null && m == Mechanism.HASH_B) anchor.commit(); }
            if (lat != null && i < lat.length) lat[i] = System.nanoTime() - t0;
            i++;
        }
        txn.commit();
        if (m == Mechanism.HASH_B) anchor.commit();
        ins.close(); create.close(); credit.close(); debit.close();
    }
}
