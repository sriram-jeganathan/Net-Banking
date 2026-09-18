# tamperbench

Runnable experimental artifact for the paper *A Comparative Evaluation of
Tamper-Detection Mechanisms for Online Banking Transaction Ledgers*. It
implements and actually runs the baseline, the SHA-256 hash chain (Configuration
A, database-resident digests, and Configuration B, externally anchored digests),
and the PostgreSQL audit-trigger mechanism against two live PostgreSQL
instances, executes a real attack suite, and writes real result files.

## What is real, and where the numbers may go

- **Security / detection results (`results/security.csv`, `results/security_matrix.md`).**
  These are logical outcomes and are environment independent. They are valid for
  the paper as-is.
- **Performance results (`results/perf_sandbox.csv`).** Real measurements, but
  taken in a shared, single-core sandbox. They are labeled sandbox-specific and
  must be re-measured on the pinned bench (paper Table III) before being
  reported. Do not copy them into the paper.
- **Hyperledger Fabric.** The chaincode in `fabric/` is complete and deployable
  but was not executed here (no Docker in the sandbox). No Fabric numbers are
  reported. See `fabric/README.md`.

## Requirements

JDK 21+ and PostgreSQL 16 (client + server binaries). No root or `sudo` is
needed: `run.sh` creates its two clusters under the project directory and runs
them as your user. It auto-locates the Postgres binaries and the JDBC driver, and
will download the driver into `lib/` if it is not already installed. Override
with `PGBIN`, `JAR`, `TXN_PORT`, `ANC_PORT`, or `PGBASE` if your setup differs.
Fabric additionally needs Docker and the Fabric samples.

## Run everything

```
bash run.sh
```

This initializes two PostgreSQL instances (transaction on 5433, anchor on 5434),
applies the schema, compiles, and runs conformance, the security matrix, and the
performance harness. Result files land in `results/`.

## What the run does

1. **Conformance.** The live Config A chain in PostgreSQL reproduces the frozen
   golden vectors from the reference core (`vectors/golden_phase1.txt`), proving
   the database implementation matches the pinned serialization and hashing.
2. **Security matrix.** For each mechanism, a fresh ledger is loaded and each
   applicable attack is run at several positions, then verified. Attacks: direct
   row modification, deletion, privileged rewrite (Model C), audit-record
   deletion, trigger disabling, and unauthorized insertion (Model E).
3. **Performance.** Per-mechanism insertion throughput and latency percentiles.

## Real results from this environment

Security (detection rate; identical across runs because these are logical
outcomes):

| Mechanism | rowModify | deleteRow | privilegedRewrite (C) | auditDelete (C) | disableTrigger (C) | unauthorizedInsert (E) |
|---|---|---|---|---|---|---|
| BASELINE  | 0%   | 0%   | n/a  | n/a  | n/a  | 0% |
| HASH_A    | 100% | 100% | **0%**   | n/a  | n/a  | 0% |
| HASH_B    | 100% | 100% | **100%** | n/a  | n/a  | 0% |
| TRIGGERS  | 100% | 100% | n/a  | 0%   | 0%   | 0% |

Reading of the key rows:
- **H3 confirmed.** HASH_A misses the privileged Model C rewrite; HASH_B, with
  the anchor on a separate instance, detects it. Same attack, opposite outcome,
  isolating integrity-metadata placement as the cause.
- **Trigger trust boundary.** Triggers catch ordinary tampering but are defeated
  by a privileged attacker who deletes the audit row or disables the trigger.
- **Integrity is not authorization.** No integrity mechanism detects an
  authorized-looking injection (Model E); that needs an auth layer, not a chain.

Two honest caveats recorded in the code and `DECISIONS.md`:
- A pure forward hash chain (both A and B at the row level) cannot detect
  deletion of the very last record from data alone; Config B still catches it via
  the row/anchor count mismatch. The deletion experiments use interior positions.
- Performance numbers vary run to run and are sandbox-specific by design.

## Layout

```
run.sh                     end-to-end reproducible run
config/phase1.properties   pinned decisions (seed, mix, serialization, clock)
db/01_transaction_schema.sql   ledger, accounts, audit trigger
deploy/anchor_store.sql    separate-instance append-only anchor
src/bank/                  Java: reference core + DB-backed mechanisms + attacks
vectors/golden_phase1.txt  frozen conformance oracle
fabric/                    deployable Fabric chaincode (not run here)
results/                   real output files
DECISIONS.md               full decision register and results provenance
```
