package bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AnchorStore backed by the separate anchor instance (port 5434). append() does
 * an INSERT into anchor_log, which the anchor-instance guard trigger keeps
 * append-only. The transaction-DB attacker cannot reach this instance, so under
 * Threat Model C the anchored digests remain authoritative.
 */
public final class PostgresAnchor implements AnchorStore {

    private final Connection conn;

    public PostgresAnchor(Connection anchorConn) {
        this.conn = anchorConn;
    }

    @Override
    public void append(long seq, byte[] digest) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO anchor_log(seq, digest) VALUES (?, ?)")) {
            ps.setLong(1, seq);
            ps.setBytes(2, digest);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("anchor append failed for seq " + seq, e);
        }
    }

    public void commit() {
        try { conn.commit(); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** The underlying anchor-instance connection (used for admin reset). */
    public Connection connection() { return conn; }

    @Override
    public byte[] get(long seq) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT digest FROM anchor_log WHERE seq = ?")) {
            ps.setLong(1, seq);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("no anchor for seq " + seq);
                return rs.getBytes(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("anchor get failed for seq " + seq, e);
        }
    }

    @Override
    public long size() {
        try (PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM anchor_log");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Return true iff a row with this seq exists in the anchor. */
    public boolean has(long seq) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM anchor_log WHERE seq = ?")) {
            ps.setLong(1, seq);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
