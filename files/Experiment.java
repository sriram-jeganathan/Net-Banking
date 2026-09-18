package bank;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import bank.Ledger.Mechanism;

/**
 * Runs the artifact end to end against the two live PostgreSQL instances and
 * writes real result files. Security/detection outcomes are environment
 * independent and valid for the paper. Performance numbers are real but
 * sandbox specific and must be re-measured on the pinned bench (Table III).
 */
public final class Experiment {

    static int accounts;
    static byte[] seed;
    static WorkloadGenerator gen;

    public static void main(String[] args) throws Exception {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("config/phase1.properties")) { p.load(in); }
        seed = HashChain.fromHex(p.getProperty("chain.seed.hex").trim());
        accounts = Integer.parseInt(p.getProperty("workload.accounts").trim());
        gen = new WorkloadGenerator(p);
        Files.createDirectories(Path.of("results"));

        try (Connection txn = Db.txn(); Connection anc = Db.anchor()) {
            PostgresAnchor anchor = new PostgresAnchor(anc);
            conformance(txn, anchor);
            security(txn, anchor);
            performance(txn, anchor);
        }
        System.out.println("\nDONE. Real result files written to results/.");
    }

    // ---- Conformance: the live DB chain must reproduce the frozen oracle ----
    static void conformance(Connection txn, PostgresAnchor anchor) throws Exception {
        Ledger ledger = new Ledger(seed);
        ledger.resetTxn(txn, accounts, Mechanism.HASH_A);
        List<Tx> fixed = new ArrayList<>();
        fixed.add(new Tx(0, "tx-000000000000", "CREATE",   "acct-c0", "",       10000000, 1704067200000000L));
        fixed.add(new Tx(1, "tx-000000000001", "DEPOSIT",  "acct-7",  "",          50000, 1704067200001000L));
        fixed.add(new Tx(2, "tx-000000000002", "TRANSFER", "acct-7",  "acct-42",   12500, 1704067200002000L));
        ledger.load(txn, anchor, Mechanism.HASH_A, fixed, null);
        List<String> got = new ArrayList<>();
        for (Verifier.Row r : new Verifier(seed).readRows(txn)) got.add(HashChain.hex(r.digest()));
        List<String> golden = Files.readAllLines(Path.of("vectors/golden_phase1.txt"));
        boolean ok = got.equals(golden);
        System.out.println("[" + (ok ? "PASS" : "FAIL") +
                "] Conformance: live PostgreSQL Config A chain reproduces frozen golden vectors");
        if (!ok) { System.out.println("  got=" + got + "\n  want=" + golden); System.exit(1); }
    }

    // ---- Security: run the attack matrix, record real detection outcomes ----
    static final int NSEC = 500;
    static final long[] SEQS = {80, 160, 240, 320, 400};

    static void security(Connection txn, PostgresAnchor anchor) throws Exception {
        System.out.println("\n== Security experiments (real attacks) ==");
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"mechanism","attack","reps","detections","detection_rate",
                "attacks_succeeded","attack_success_rate"});

        for (Mechanism m : Mechanism.values()) {
            for (String atk : attacksFor(m)) {
                int det = 0, succ = 0, reps = SEQS.length;
                for (long seq : SEQS) {
                    boolean detected = runCase(txn, anchor, m, atk, seq);
                    if (detected) det++; else succ++; // undetected tamper = success
                }
                double dr = (double) det / reps, asr = (double) succ / reps;
                rows.add(new String[]{m.name(), atk, "" + reps, "" + det,
                        String.format("%.2f", dr), "" + succ, String.format("%.2f", asr)});
                System.out.printf("  %-9s %-18s DR=%.0f%%  ASR=%.0f%%%n",
                        m, atk, dr * 100, asr * 100);
            }
        }
        writeCsv("results/security.csv", rows);
        writeMatrix(rows);
    }

    static List<String> attacksFor(Mechanism m) {
        return switch (m) {
            case BASELINE -> List.of("rowModify", "deleteRow", "unauthorizedInsert");
            case HASH_A, HASH_B -> List.of("rowModify", "deleteRow", "privilegedRewrite", "unauthorizedInsert");
            case TRIGGERS -> List.of("rowModify", "deleteRow", "auditDelete", "disableTrigger", "unauthorizedInsert");
        };
    }

    static boolean runCase(Connection txn, PostgresAnchor anchor, Mechanism m,
                           String atk, long seq) throws Exception {
        Ledger ledger = new Ledger(seed);
        ledger.resetTxn(txn, accounts, m);
        ledger.resetAnchor(anchor.connection());
        ledger.load(txn, anchor, m, gen.generate(NSEC), null);

        AttackRunner atkr = new AttackRunner(seed);
        switch (atk) {
            case "rowModify" -> atkr.rowModify(txn, seq);
            case "deleteRow" -> atkr.deleteRow(txn, seq);
            case "privilegedRewrite" -> atkr.privilegedRewrite(txn, seq);
            case "auditDelete" -> atkr.auditDelete(txn, seq);
            case "disableTrigger" -> atkr.disableTrigger(txn, seq);
            case "unauthorizedInsert" -> atkr.unauthorizedInsert(txn, anchor, m);
            default -> throw new IllegalStateException(atk);
        }
        Verifier v = new Verifier(seed);
        return switch (m) {
            case BASELINE -> false;                    // no integrity mechanism
            case HASH_A -> v.verifyHashA(txn).detected();
            case HASH_B -> v.verifyHashB(txn, anchor).detected();
            case TRIGGERS -> v.verifyTriggers(txn).detected();
        };
    }


    // ---- Performance: real but sandbox-specific throughput/latency ----
    static final int NPERF = 5000;

    static void performance(Connection txn, PostgresAnchor anchor) throws Exception {
        System.out.println("\n== Performance (SANDBOX numbers, not the paper bench) ==");
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"mechanism","n","tps","mean_ms","p50_ms","p95_ms","p99_ms"});
        List<Tx> load = gen.generate(NPERF);
        for (Mechanism m : Mechanism.values()) {
            Ledger ledger = new Ledger(seed);
            ledger.resetTxn(txn, accounts, m);
            ledger.resetAnchor(anchor.connection());
            long[] lat = new long[NPERF];
            long t0 = System.nanoTime();
            ledger.load(txn, anchor, m, load, lat);
            double secs = (System.nanoTime() - t0) / 1e9;
            double tps = NPERF / secs;
            long[] s = lat.clone(); Arrays.sort(s);
            rows.add(new String[]{m.name(), "" + NPERF, String.format("%.0f", tps),
                    ms(mean(lat)), ms(s[(int)(0.50*NPERF)]), ms(s[(int)(0.95*NPERF)]), ms(s[(int)(0.99*NPERF)])});
            System.out.printf("  %-9s TPS=%.0f  mean=%sms p95=%sms p99=%sms%n",
                    m, tps, ms(mean(lat)), ms(s[(int)(0.95*NPERF)]), ms(s[(int)(0.99*NPERF)]));
        }
        writeCsv("results/perf_sandbox.csv", rows);
    }

    static double mean(long[] a){ double s=0; for(long x:a) s+=x; return s/a.length; }
    static String ms(double nanos){ return String.format("%.3f", nanos/1e6); }

    static void writeCsv(String path, List<String[]> rows) throws Exception {
        try (PrintWriter w = new PrintWriter(path)) {
            for (String[] r : rows) w.println(String.join(",", r));
        }
        System.out.println("  wrote " + path);
    }

    static void writeMatrix(List<String[]> rows) throws Exception {
        try (PrintWriter w = new PrintWriter("results/security_matrix.md")) {
            w.println("# Security-outcome matrix (real runs)\n");
            w.println("Detection rate per mechanism and attack. Single-peer modification is");
            w.println("Fabric-only and not runnable in this environment (marked N/A).\n");
            w.println("| Mechanism | Attack | Detection rate | Attack success rate |");
            w.println("|---|---|---|---|");
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                w.printf("| %s | %s | %.0f%% | %.0f%% |%n",
                        r[0], r[1], Double.parseDouble(r[4]) * 100, Double.parseDouble(r[6]) * 100);
            }
            w.println("| FABRIC | singlePeerModification | N/A (requires Docker) | N/A |");
        }
        System.out.println("  wrote results/security_matrix.md");
    }
}
