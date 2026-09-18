package bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Real tampering attacks, executed as SQL against the transaction instance
 * (5433). The attacker never touches the anchor instance (5434): that is the
 * boundary Configuration B relies on. Each method mutates persistent state.
 */
public final class AttackRunner {

    private final byte[] seed;
    public AttackRunner(byte[] seed) { this.seed = seed.clone(); }

    /** Model B: direct row modification. */
    public void rowModify(Connection txn, long seq) throws SQLException {
        try (PreparedStatement ps = txn.prepareStatement(
                "UPDATE transactions SET amount = amount + 1 WHERE seq = ?")) {
            ps.setLong(1, seq); ps.executeUpdate();
        }
        txn.commit();
    }

    /** Model B: transaction deletion. */
    public void deleteRow(Connection txn, long seq) throws SQLException {
        try (PreparedStatement ps = txn.prepareStatement(
                "DELETE FROM transactions WHERE seq = ?")) {
            ps.setLong(1, seq); ps.executeUpdate();
        }
        txn.commit();
    }

    /**
     * Model C on the transaction instance: modify the row's data AND recompute
     * every stored digest from seq to the end. This is the privileged rewrite.
     * It defeats Configuration A (row digests are here) but not Configuration B
     * (the anchor is on a separate instance the attacker cannot reach).
     */
    public void privilegedRewrite(Connection txn, long seq) throws SQLException {
        // Change the target row's data.
        try (PreparedStatement ps = txn.prepareStatement(
                "UPDATE transactions SET amount = amount + 1 WHERE seq = ?")) {
            ps.setLong(1, seq); ps.executeUpdate();
        }
        // Recompute the digest suffix using the attacker's full read/write access.
        byte[] prev = (seq == 0) ? seed.clone() : digestOf(txn, seq - 1);
        try (Statement st = txn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT seq,txid,type,src,dst,amount,ts_micros FROM transactions " +
                "WHERE seq >= " + seq + " ORDER BY seq")) {
            PreparedStatement upd = txn.prepareStatement(
                    "UPDATE transactions SET digest = ? WHERE seq = ?");
            while (rs.next()) {
                Tx t = new Tx(rs.getLong(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getLong(6), rs.getLong(7));
                byte[] h = HashChain.link(CanonicalSerializer.serialize(t), prev);
                upd.setBytes(1, h); upd.setLong(2, t.seq()); upd.executeUpdate();
                prev = h;
            }
            upd.close();
        }
        txn.commit();
    }

    /** Model C (triggers): tamper, then delete the audit entry it produced. */
    public void auditDelete(Connection txn, long seq) throws SQLException {
        rowModify(txn, seq); // logs an UPDATE audit entry (trigger enabled)
        try (PreparedStatement ps = txn.prepareStatement(
                "DELETE FROM audit_log WHERE seq = ? AND op = 'UPDATE'")) {
            ps.setLong(1, seq); ps.executeUpdate();
        }
        txn.commit();
    }

    /** Model C (triggers): disable the trigger, then tamper (no audit entry). */
    public void disableTrigger(Connection txn, long seq) throws SQLException {
        try (Statement st = txn.createStatement()) {
            st.execute("ALTER TABLE transactions DISABLE TRIGGER audit_trg");
        }
        try (PreparedStatement ps = txn.prepareStatement(
                "UPDATE transactions SET amount = amount + 1 WHERE seq = ?")) {
            ps.setLong(1, seq); ps.executeUpdate();
        }
        txn.commit();
    }

    /**
     * Model E: an authorized-looking injection via the application path (stolen
     * credentials). The record is correctly chained and, for Configuration B,
     * anchored, so it is indistinguishable from a legitimate transaction. This
     * tests whether the integrity mechanisms catch injection; they do not,
     * because integrity is not authorization.
     */
    public void unauthorizedInsert(Connection txn, PostgresAnchor anchor,
                                   Ledger.Mechanism m) throws SQLException {
        long maxSeq;
        try (Statement st = txn.createStatement();
             ResultSet rs = st.executeQuery("SELECT max(seq) FROM transactions")) {
            rs.next(); maxSeq = rs.getLong(1);
        }
        long seq = maxSeq + 1;
        Tx t = new Tx(seq, "tx-EVIL", "DEPOSIT", "acct-0", "", 999999, 1704067200000000L + seq);
        byte[] digest = null;
        if (m == Ledger.Mechanism.HASH_A || m == Ledger.Mechanism.HASH_B) {
            digest = HashChain.link(CanonicalSerializer.serialize(t), digestOf(txn, maxSeq));
        }
        try (PreparedStatement ps = txn.prepareStatement(
                "INSERT INTO transactions(seq,txid,type,src,dst,amount,ts_micros,digest) " +
                "VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setLong(1, t.seq()); ps.setString(2, t.txid()); ps.setString(3, t.type());
            ps.setString(4, t.src()); ps.setString(5, t.dst()); ps.setLong(6, t.amount());
            ps.setLong(7, t.timestampMicros()); ps.setBytes(8, digest);
            ps.executeUpdate();
        }
        txn.commit();
        if (m == Ledger.Mechanism.HASH_B) { anchor.append(seq, digest); anchor.commit(); }
    }

    private byte[] digestOf(Connection txn, long seq) throws SQLException {
        try (PreparedStatement ps = txn.prepareStatement(
                "SELECT digest FROM transactions WHERE seq = ?")) {
            ps.setLong(1, seq);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next(); return rs.getBytes(1);
            }
        }
    }
}
