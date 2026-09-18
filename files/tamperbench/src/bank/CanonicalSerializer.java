package bank;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Canonical serializer sigma. Format: length-prefixed-utf8-v1.
 * For each field, in the fixed order seq,txid,type,src,dst,amount,timestamp,
 * emit a 4-byte big-endian length of the field's UTF-8 bytes, then the bytes.
 * Numeric fields use their canonical decimal string form. Length-prefixing
 * makes the encoding injective: no two distinct field tuples can collide, so
 * there is no delimiter-ambiguity attack.
 */
public final class CanonicalSerializer {

    private CanonicalSerializer() {}

    public static byte[] serialize(Tx t) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        put(out, Long.toString(t.seq()));
        put(out, t.txid());
        put(out, t.type());
        put(out, t.src());
        put(out, t.dst());
        put(out, Long.toString(t.amount()));
        put(out, Long.toString(t.timestampMicros()));
        return out.toByteArray();
    }

    private static void put(ByteArrayOutputStream out, String field) {
        byte[] b = field.getBytes(StandardCharsets.UTF_8);
        // 4-byte big-endian length prefix
        out.write((b.length >>> 24) & 0xFF);
        out.write((b.length >>> 16) & 0xFF);
        out.write((b.length >>> 8) & 0xFF);
        out.write(b.length & 0xFF);
        out.write(b, 0, b.length);
    }
}
