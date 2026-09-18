package bank;

/**
 * Append-only anchor store for Configuration B. The interface has no update or
 * delete operation by design: it can only append and read. Deployed on a
 * PostgreSQL instance in a separate trust domain (see deploy/anchor_store.sql),
 * this holds the authoritative chain digests that a Threat Model C attacker on
 * the transaction store cannot alter.
 */
public interface AnchorStore {

    /** Append the digest for seq. Must reject any second write to the same seq. */
    void append(long seq, byte[] digest);

    /** Return the anchored digest for seq. */
    byte[] get(long seq);

    /** Number of anchored entries. */
    long size();
}
