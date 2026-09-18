package bank;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Reads back ledger state and verifies integrity for each mechanism. */
public final class Verifier {

    public record Row(Tx tx, byte[] digest) {}
    public record Result(boolean detected, long firstBadSeq) {}

    private final byte[] seed;

    public Verifier(byte[] seed) { this.seed = seed.clone(); }

    public List<Row> readRows(Connection txn) throws SQLException {
        List<Row> rows = new ArrayList<>();
        try (Statement st = txn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT seq,txid,type,src,dst,amount,ts_micros,digest " +
                "FROM transactions ORDER BY seq")) {
            while (rs.next()) {
                Tx t = new Tx(rs.getLong(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getLong(6), rs.getLong(7));
                rows.add(new Row(t, rs.getBytes(8)));
            }
        }
        return rows;
    }

    /** Config A: recompute chain, compare to the digest stored in each row. */
    public Result verifyHashA(Connection txn) throws SQLException {
        byte[] prev = seed.clone();
        for (Row r : readRows(txn)) {
            byte[] h = HashChain.link(CanonicalSerializer.serialize(r.tx()), prev);
            if (!MessageDigest.isEqual(h, r.digest())) return new Result(true, r.tx().seq());
            prev = h;
        }
        return new Result(false, -1);
    }

    /** Config B: recompute chain, compare to the anchored digest; also catch
     *  deletions via a row/anchor count mismatch. */
    public Result verifyHashB(Connection txn, PostgresAnchor anchor) throws SQLException {
        List<Row> rows = readRows(txn);
        byte[] prev = seed.clone();
        for (Row r : rows) {
            byte[] h = HashChain.link(CanonicalSerializer.serialize(r.tx()), prev);
            if (!anchor.has(r.tx().seq())) return new Result(true, r.tx().seq());
            if (!MessageDigest.isEqual(h, anchor.get(r.tx().seq())))
                return new Result(true, r.tx().seq());
            prev = h;
        }
        if (rows.size() != anchor.size()) return new Result(true, -1); // deletion
        return new Result(false, -1);
    }

    /** Triggers: the legitimate workload only INSERTs, so any UPDATE or DELETE
     *  audit entry is evidence of tampering. */
    public Result verifyTriggers(Connection txn) throws SQLException {
        try (PreparedStatement ps = txn.prepareStatement(
                "SELECT count(*) FROM audit_log WHERE op IN ('UPDATE','DELETE')");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new Result(rs.getLong(1) > 0, -1);
        }
    }
}
