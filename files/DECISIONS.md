# Phase 1 Decision Register

The paper (Section V-A) requires several implementation decisions to be "pinned
in the artifact." Each is listed below with its justification. All values live
in `config/phase1.properties` and must stay constant across relevant runs. If a
value changes, the golden vectors in `vectors/golden_phase1.txt` change too and
must be regenerated and re-frozen.

| ID | Decision | Value | Justification |
|----|----------|-------|---------------|
| D1 | Fields entering the hash | seq, txid, type, src, dst, amount, timestamp | Exactly the set named in the paper (transaction id, account references, type, amount, timestamp, sequence index). |
| D2 | Field order | as in D1, fixed | Order must be fixed for a canonical serialization; any order works if pinned. |
| D3 | Serialization | length-prefixed UTF-8 (4-byte big-endian length per field) | Matches the paper's suggested encoding; length-prefixing is injective, so there is no delimiter-ambiguity attack (verified by AT-2). |
| D4 | Timestamp | UTC micros from a deterministic logical clock (base + seq*tick) | Reference vectors need stable timestamps. The live system uses DB commit time; conformance is checked by feeding identical field values, not wall-clock. |
| D5 | Previous-hash encoding | raw 32 bytes | Paper specifies raw bytes, not hex text. |
| D6 | Chain seed | recorded 32-byte hex | Fixed so H_0 and all vectors are reproducible. |
| D7 | SHA-256 provider | JDK `java.security.MessageDigest` | Standard-library, zero external dependency; validated against the FIPS-180 "abc" vector (AT-0). |
| D8 | Workload RNG | `java.util.Random(seed)` | Its algorithm is specified and stable across JVMs, so the stream is reproducible (AT-1). |
| D9 | Transaction mix | 5 / 25 / 25 / 45 (create/deposit/withdraw/transfer) | Taken directly from the paper's default workload. |
| D10 | Account-selection distribution | uniform | Paper cites Zipfian only as an example. Uniform is the simplest defensible baseline; Zipfian skew is a planned Phase 2 sensitivity variant. Recorded, not silent. |
| D11 | Currency unit | integer minor units | Avoids floating-point rounding in amounts and balances. |
| D12 | RNG draw order per tx | type, src, [dst if transfer], amount | Fixed; changing it changes the stream, so it is part of the recorded method. |

## Scope boundary for Phase 1

Phase 1 covers only the hardware-independent scientific core: the deterministic
workload generator, the canonical serializer, and the hash-chain build/verify
logic (Configuration A), with acceptance tests and frozen golden vectors.

It deliberately does NOT include: the Spring Boot API, PostgreSQL, the audit
triggers, the Fabric network, the externally anchored hash chain (Configuration
B), the full attack generator, or any performance measurement. Those are later
phases and must run on the pinned test bench described in the paper's Table III.

## Why no performance numbers come from this environment

This artifact was built in a sandbox with no Docker (so Fabric cannot run), no
pinned hardware (a single shared virtualized core), and an ephemeral filesystem.
Any throughput, latency, CPU, or memory figure measured here is not legitimate
for the paper and must not be reported. Only the logical-invariant tests above
are valid here; performance and the Fabric mechanism require the real bench.

---

# Phase 2 Decision Register (Configuration B)

| ID | Decision | Value | Justification |
|----|----------|-------|---------------|
| D13 | Anchor isolation | separate PostgreSQL instance/host, independent credentials | Same-instance role separation does not survive Threat Model C (a privileged attacker can `SET ROLE`, `ALTER` grants, `DROP` the guard trigger, or `TRUNCATE`). Only a separate trust domain makes Configuration B genuinely stronger than A under Model C, which is what H3 tests. |
| D14 | Anchor granularity | per-transaction | Lowest detection latency. The throughput cost is a measured quantity for the real bench, not decided here. |
| D15 | Append-only enforcement | INSERT-only role + `BEFORE UPDATE/DELETE/TRUNCATE` guard trigger | Defense in depth on the anchor instance. Primary control remains the separate trust domain. Validated against PostgreSQL 16 (see below). |

## Refinement of Threat Model C (recorded, not silent)

The paper's Model C is a privileged attacker on the transaction store. Configuration B assumes that privilege is **scoped to the transaction store**: the attacker does not hold credentials on the separate anchor instance. This assumption is stated explicitly. A strictly stronger attacker who is privileged across *both* stores would defeat B as well; that attacker is out of scope for H3 and is a candidate for future work, matching the paper's existing note on multi-domain compromise.

## DDL validation

`deploy/anchor_store.sql` was applied to a throwaway PostgreSQL 16 instance. INSERT succeeded; UPDATE, DELETE, and TRUNCATE were each rejected with `anchor_log is append-only`, and the inserted row survived. This is a functional-correctness check of the enforcement logic, not a performance measurement, and is valid regardless of hardware.

---

# Phase 3 Decision Register (runnable DB-backed system)

| ID | Decision | Value | Justification |
|----|----------|-------|---------------|
| D16 | Balance semantics | running total on `accounts`, no overdraft rejection | The ledger is an append-only event log; balances are a maintained convenience. No overdraft gate keeps the workload deterministic and avoids aborts that would confound throughput. |
| D17 | Mechanism isolation in experiments | schema reset + audit trigger toggled per run | The audit trigger is enabled only for the trigger mechanism so its overhead does not confound the hash-chain and baseline measurements. |
| D18 | Attacker access model | SQL on the transaction instance (5433) only | Models Threat Model B/C on the transaction store. The anchor instance (5434) is unreachable to the attacker, which is the property Configuration B depends on. |
| D19 | Separate-instance realization | two PostgreSQL clusters, distinct ports, data dirs, credentials | A faithful stand-in for separate hosts/trust domains within one machine. On the pinned bench these should be separate hosts. |

## Results provenance (what may appear in the paper)

- **Security/detection outcomes are environment independent** and valid for the
  paper. They are produced by real attacks and real verification.
- **Performance numbers from this environment are sandbox-specific** and must be
  re-measured on the pinned bench (Table III). They are written to
  `results/perf_sandbox.csv` and are not for direct use in the paper.
- **Fabric is not executed here** (no Docker). Its chaincode is complete and
  deployable; Fabric measurements must be produced on the bench.

## Known logical caveats (recorded, not hidden)

- A forward row-level hash chain cannot detect deletion of the final record from
  data alone (no successor link to break). Configuration B still detects it via
  the row/anchor count mismatch. Deletion experiments use interior positions;
  the tail-deletion case is noted as a limitation.
- The unauthorized-insertion result (Model E undetected across all integrity
  mechanisms) is expected: integrity mechanisms detect modification of existing
  records, not injection of correctly-authorized new ones. Authorization is a
  separate control (Fabric endorsement, or an application auth layer).
