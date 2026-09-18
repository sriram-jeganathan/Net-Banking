# Hyperledger Fabric mechanism

## Status in this artifact

The chaincode in `chaincode/banking.go` is a complete, deployable implementation
of the same banking operations used by the PostgreSQL mechanisms. It was **not
executed in the build sandbox** because Fabric requires Docker to run peers and
the ordering service, and the sandbox has no Docker. This is a real
implementation, not a stub; it runs on any host with Docker and the Fabric
samples. Nothing about the Fabric mechanism is faked, and no Fabric numbers are
reported anywhere in the results.

## Deploy on a Docker host

Prerequisites: Docker, Docker Compose, Go 1.21+, and the Fabric samples with the
`test-network` (two organizations, one peer each, a Raft ordering service).

```
# from fabric-samples/test-network
./network.sh up createChannel -c bankchannel -s couchdb
./network.sh deployCC -c bankchannel -ccn banking \
    -ccp /path/to/tamperbench/fabric/chaincode -ccl go \
    -ccep "AND('Org1MSP.peer','Org2MSP.peer')"
```

The endorsement policy `AND('Org1MSP.peer','Org2MSP.peer')` requires both
organizations to endorse, which is what makes single-peer tampering ineffective:
a change to one peer's local state is not endorsed by the other organization and
is rejected at validation.

## Experiments to run on the bench

These map to the paper's threat model and metrics and produce the Fabric column
that this sandbox cannot:

1. **Model E (unauthorized transaction).** Submit a transaction with an identity
   outside the endorsement policy and confirm it is rejected. Record the
   detection/rejection outcome for the security matrix.
2. **Model D (single-peer modification).** Directly edit one peer's CouchDB
   world-state document, then query through the ordering/validation path and via
   the other org's peer. Confirm the agreed ledger is unchanged and the tampered
   peer is detectable on state reconciliation. Record the outcome.
3. **Performance.** Drive the same workload generator (export the transaction
   stream from the Java harness) through the Fabric gateway SDK and measure TPS
   and latency, decomposed into endorsement, ordering, and validation cost as
   described in the paper's fairness analysis.

Record all Fabric measurements on the same pinned bench (Table III) used for the
PostgreSQL mechanisms so the comparison is on equal hardware.
