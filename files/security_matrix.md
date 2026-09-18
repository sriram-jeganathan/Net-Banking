# Security-outcome matrix (real runs)

Detection rate per mechanism and attack. Single-peer modification is
Fabric-only and not runnable in this environment (marked N/A).

| Mechanism | Attack | Detection rate | Attack success rate |
|---|---|---|---|
| BASELINE | rowModify | 0% | 100% |
| BASELINE | deleteRow | 0% | 100% |
| BASELINE | unauthorizedInsert | 0% | 100% |
| HASH_A | rowModify | 100% | 0% |
| HASH_A | deleteRow | 100% | 0% |
| HASH_A | privilegedRewrite | 0% | 100% |
| HASH_A | unauthorizedInsert | 0% | 100% |
| HASH_B | rowModify | 100% | 0% |
| HASH_B | deleteRow | 100% | 0% |
| HASH_B | privilegedRewrite | 100% | 0% |
| HASH_B | unauthorizedInsert | 0% | 100% |
| TRIGGERS | rowModify | 100% | 0% |
| TRIGGERS | deleteRow | 100% | 0% |
| TRIGGERS | auditDelete | 0% | 100% |
| TRIGGERS | disableTrigger | 0% | 100% |
| TRIGGERS | unauthorizedInsert | 0% | 100% |
| FABRIC | singlePeerModification | N/A (requires Docker) | N/A |
