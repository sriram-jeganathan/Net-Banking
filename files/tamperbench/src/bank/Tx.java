package bank;

/**
 * A single banking transaction record. Fields are exactly the set that enters
 * the hash (config: fields.order = seq,txid,type,src,dst,amount,timestamp).
 * amount is in integer minor currency units; timestampMicros is UTC micros.
 * dst is the empty string for non-transfer operations.
 */
public record Tx(
        long seq,
        String txid,
        String type,      // CREATE | DEPOSIT | WITHDRAW | TRANSFER
        String src,
        String dst,
        long amount,
        long timestampMicros
) {
    public Tx withAmount(long newAmount) {
        return new Tx(seq, txid, type, src, dst, newAmount, timestampMicros);
    }
}
