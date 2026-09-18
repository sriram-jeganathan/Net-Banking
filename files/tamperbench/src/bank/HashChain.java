package bank;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * SHA-256 hash chain. H_0 = SHA256(sigma(T_0) || seed);
 * H_i = SHA256(sigma(T_i) || H_{i-1}). The previous-hash is concatenated as
 * raw 32 bytes (not hex text), per the config.
 *
 * Config A semantics: the digest is stored alongside the transaction, so a
 * privileged database attacker (Threat Model C) can rewrite both. Verification
 * (Algorithm 1 in the paper) recomputes the chain from the seed and compares
 * each recomputed digest against the stored one.
 */
public final class HashChain {

    /** A stored ledger entry: the transaction plus its stored digest (Config A). */
    public record Entry(Tx tx, byte[] digest) {}

    private final byte[] seed;

    public HashChain(byte[] seed) {
        this.seed = seed.clone();
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static byte[] hash(byte[] serialized, byte[] prev) {
        MessageDigest md = sha256();
        md.update(serialized);
        md.update(prev);
        return md.digest();
    }

    /**
     * Public link function reused by Configuration B. Identical to the internal
     * hashing, exposed so both configurations share one implementation and
     * cannot silently diverge.
     */
    public static byte[] link(byte[] serialized, byte[] prev) {
        return hash(serialized, prev);
    }

    /** Build the stored ledger (Config A) for a transaction sequence. */
    public List<Entry> build(List<Tx> txs) {
        List<Entry> ledger = new ArrayList<>(txs.size());
        byte[] prev = seed.clone();
        for (Tx t : txs) {
            byte[] h = hash(CanonicalSerializer.serialize(t), prev);
            ledger.add(new Entry(t, h));
            prev = h;
        }
        return ledger;
    }

    /**
     * Verify a stored ledger. Returns the index of the first entry whose
     * recomputed digest disagrees with its stored digest, or -1 if the whole
     * chain verifies. Matches Algorithm 1: prev is the freshly recomputed
     * digest, so a data change with an unchanged stored digest is caught.
     */
    public int verify(List<Entry> ledger) {
        byte[] prev = seed.clone();
        for (int i = 0; i < ledger.size(); i++) {
            Entry e = ledger.get(i);
            byte[] h = hash(CanonicalSerializer.serialize(e.tx()), prev);
            if (!MessageDigest.isEqual(h, e.digest())) {
                return i;
            }
            prev = h;
        }
        return -1;
    }

    public static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    public static byte[] fromHex(String s) {
        byte[] out = new byte[s.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }
        return out;
    }
}
