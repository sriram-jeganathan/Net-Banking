package bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC connections to the two separate PostgreSQL instances. The transaction
 * instance (txn) holds the ledger; the anchor instance (anchor) holds the
 * Configuration B append-only anchor. They are distinct processes with distinct
 * credentials: the transaction-DB attacker operates on txn only and has no
 * access to anchor. That separation is the whole point of Configuration B.
 */
public final class Db {

    public static final String TXN_URL = env("TXN_URL",
            "jdbc:postgresql://127.0.0.1:5433/bankdb?user=bankapp");
    public static final String ANCHOR_URL = env("ANCHOR_URL",
            "jdbc:postgresql://127.0.0.1:5434/anchordb?user=anchoradmin");

    private static String env(String key, String dflt) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? dflt : v;
    }

    private Db() {}

    public static Connection txn() throws SQLException {
        Connection c = DriverManager.getConnection(TXN_URL);
        c.setAutoCommit(false);
        return c;
    }

    public static Connection anchor() throws SQLException {
        Connection c = DriverManager.getConnection(ANCHOR_URL);
        c.setAutoCommit(false);
        return c;
    }
}
