package bank;

import java.security.MessageDigest;
import java.util.List;

/**
 * Configuration B: the hash chain whose authoritative digests live in a
 * separate-trust-domain append-only anchor. Chain construction is identical to
 * Configuration A (same seed, same serializer, same link function); the only
 * difference is where the digests used for verification come from. Under Threat
 * Model C, an attacker who rewrites transaction rows cannot rewrite the anchor,
 * so verification recomputes the chain from the (tampered) rows and compares
 * against the untouched anchor, detecting the change.
 */
public final class HashChainB {

    private final byte[] seed;

    public HashChainB(byte[] seed) {
        this.seed = seed.clone();
    }

    /** Build the chain and write each digest to the append-only anchor. */
    public void buildAndAnchor(List<Tx> txs, AnchorStore anchor) {
        byte[] prev = seed.clone();
        for (int i = 0; i < txs.size(); i++) {
            byte[] h = HashChain.link(CanonicalSerializer.serialize(txs.get(i)), prev);
            anchor.append(i, h);
            prev = h;
        }
    }

    /**
     * Verify the (possibly tampered) transaction rows against the anchor.
     * Returns the index of the first row whose recomputed digest disagrees with
     * the anchored digest, or -1 if all rows verify.
     */
    public int verifyAgainstAnchor(List<Tx> txs, AnchorStore anchor) {
        byte[] prev = seed.clone();
        for (int i = 0; i < txs.size(); i++) {
            byte[] h = HashChain.link(CanonicalSerializer.serialize(txs.get(i)), prev);
            if (!MessageDigest.isEqual(h, anchor.get(i))) {
                return i;
            }
            prev = h;
        }
        return -1;
    }
}
