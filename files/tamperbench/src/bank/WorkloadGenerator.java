package bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Deterministic banking-workload generator. Given the same config (seed, mix,
 * account count, amount range, clock), it emits a byte-identical transaction
 * stream. The RNG is java.util.Random, whose algorithm is specified and stable
 * across JVMs, so the stream is reproducible.
 *
 * Draw order per transaction is fixed and must not change: (1) type, (2) src
 * account, (3) dst account if TRANSFER, (4) amount. Changing this order changes
 * the stream, so it is part of the recorded methodology.
 */
public final class WorkloadGenerator {

    private final int accounts;
    private final double pCreate, pDeposit, pWithdraw; // transfer = remainder
    private final long amountMin, amountMax;
    private final long initialBalance;
    private final long clockBase, clockTick;
    private final long seed;

    private int createCounter = 0;

    public WorkloadGenerator(Properties p) {
        this.seed = Long.parseLong(p.getProperty("workload.seed").trim());
        this.accounts = Integer.parseInt(p.getProperty("workload.accounts").trim());
        this.pCreate = Double.parseDouble(p.getProperty("workload.mix.create").trim());
        this.pDeposit = Double.parseDouble(p.getProperty("workload.mix.deposit").trim());
        this.pWithdraw = Double.parseDouble(p.getProperty("workload.mix.withdraw").trim());
        this.amountMin = Long.parseLong(p.getProperty("workload.amount.min").trim());
        this.amountMax = Long.parseLong(p.getProperty("workload.amount.max").trim());
        this.initialBalance = Long.parseLong(p.getProperty("workload.initialBalance").trim());
        this.clockBase = Long.parseLong(p.getProperty("clock.base.micros").trim());
        this.clockTick = Long.parseLong(p.getProperty("clock.tick.micros").trim());
    }

    public List<Tx> generate(long count) {
        Random rng = new Random(seed);
        createCounter = 0;
        List<Tx> out = new ArrayList<>((int) count);
        for (long i = 0; i < count; i++) {
            double r = rng.nextDouble();              // draw 1: type
            String type = pickType(r);
            String src, dst;
            long amount;
            switch (type) {
                case "CREATE" -> {
                    src = "acct-c" + (createCounter++);
                    dst = "";
                    amount = initialBalance;
                }
                case "TRANSFER" -> {
                    int a = rng.nextInt(accounts);    // draw 2: src
                    int b = rng.nextInt(accounts);    // draw 3: dst
                    if (b == a) b = (b + 1) % accounts;
                    src = "acct-" + a;
                    dst = "acct-" + b;
                    amount = drawAmount(rng);         // draw 4: amount
                }
                default -> {                          // DEPOSIT or WITHDRAW
                    int a = rng.nextInt(accounts);    // draw 2: src
                    src = "acct-" + a;
                    dst = "";
                    amount = drawAmount(rng);         // draw 3: amount
                }
            }
            long ts = clockBase + i * clockTick;
            String txid = String.format("tx-%012d", i);
            out.add(new Tx(i, txid, type, src, dst, amount, ts));
        }
        return out;
    }

    private String pickType(double r) {
        if (r < pCreate) return "CREATE";
        if (r < pCreate + pDeposit) return "DEPOSIT";
        if (r < pCreate + pDeposit + pWithdraw) return "WITHDRAW";
        return "TRANSFER";
    }

    private long drawAmount(Random rng) {
        long span = amountMax - amountMin + 1;
        return amountMin + (long) (rng.nextDouble() * span);
    }
}
