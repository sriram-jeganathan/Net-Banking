#!/usr/bin/env bash
# End-to-end reproducible run. Runs entirely in USER SPACE: no root, no sudo,
# no postgres system user. Two PostgreSQL clusters are created under the project
# directory and started on localhost ports. Override with env vars if needed:
#   PGBIN=/path/to/pg/bin  JAR=/path/to/postgresql.jar
#   TXN_PORT=5433 ANC_PORT=5434 PGBASE=/writable/dir
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"

# ---- locate PostgreSQL server binaries ----
find_pgbin() {
  if command -v initdb >/dev/null 2>&1 && command -v pg_ctl >/dev/null 2>&1; then
    dirname "$(command -v initdb)"; return 0
  fi
  for d in /usr/lib/postgresql/*/bin /usr/pgsql-*/bin \
           /opt/homebrew/opt/postgresql*/bin /usr/local/opt/postgresql*/bin \
           /Library/PostgreSQL/*/bin; do
    [ -x "$d/initdb" ] && { echo "$d"; return 0; }
  done
  return 1
}
PGBIN="${PGBIN:-$(find_pgbin || true)}"
if [ -z "${PGBIN:-}" ] || [ ! -x "$PGBIN/initdb" ]; then
  echo "ERROR: PostgreSQL server binaries (initdb/pg_ctl) not found."
  echo "  Debian/Ubuntu: sudo apt-get install postgresql"
  echo "  Fedora/RHEL:   sudo dnf install postgresql-server"
  echo "  macOS:         brew install postgresql"
  echo "  Or set PGBIN=/path/to/postgresql/bin and re-run."
  exit 1
fi

# ---- locate (or fetch) the JDBC driver ----
find_jar() {
  for j in "$ROOT"/lib/postgresql*.jar /usr/share/java/postgresql.jar \
           /usr/share/java/postgresql-*.jar; do
    [ -f "$j" ] && { echo "$j"; return 0; }
  done
  return 1
}
JAR="${JAR:-$(find_jar || true)}"
if [ -z "${JAR:-}" ]; then
  echo "JDBC driver not found; trying to download to $ROOT/lib ..."
  mkdir -p "$ROOT/lib"
  URL="https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar"
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$URL" -o "$ROOT/lib/postgresql.jar" || true
  elif command -v wget >/dev/null 2>&1; then wget -q "$URL" -O "$ROOT/lib/postgresql.jar" || true; fi
  JAR="$(find_jar || true)"
fi
if [ -z "${JAR:-}" ]; then
  echo "ERROR: PostgreSQL JDBC driver not available."
  echo "  Debian/Ubuntu: sudo apt-get install libpostgresql-jdbc-java"
  echo "  Or place a postgresql*.jar in $ROOT/lib/"
  exit 1
fi

# ---- user-space data + socket dirs ----
PGBASE="${PGBASE:-$ROOT/.pg}"
TXN_DIR="$PGBASE/txn"; ANC_DIR="$PGBASE/anchor"; SOCK="$PGBASE/sock"
TXN_PORT="${TXN_PORT:-5433}"; ANC_PORT="${ANC_PORT:-5434}"
mkdir -p "$SOCK"
export TXN_URL="jdbc:postgresql://127.0.0.1:${TXN_PORT}/bankdb?user=bankapp"
export ANCHOR_URL="jdbc:postgresql://127.0.0.1:${ANC_PORT}/anchordb?user=anchoradmin"

echo "== stopping any existing project clusters =="
[ -d "$TXN_DIR" ] && "$PGBIN/pg_ctl" -D "$TXN_DIR" stop >/dev/null 2>&1 || true
[ -d "$ANC_DIR" ] && "$PGBIN/pg_ctl" -D "$ANC_DIR" stop >/dev/null 2>&1 || true

echo "== initializing clusters under $PGBASE =="
rm -rf "$TXN_DIR" "$ANC_DIR"; mkdir -p "$TXN_DIR" "$ANC_DIR"
"$PGBIN/initdb" -D "$TXN_DIR" -A trust -U bankapp     >/dev/null
"$PGBIN/initdb" -D "$ANC_DIR" -A trust -U anchoradmin >/dev/null

echo "== starting clusters (txn:$TXN_PORT anchor:$ANC_PORT) =="
"$PGBIN/pg_ctl" -D "$TXN_DIR" -w -l "$PGBASE/txn.log" \
  -o "-p $TXN_PORT -k \"$SOCK\" -c listen_addresses=127.0.0.1" start
"$PGBIN/pg_ctl" -D "$ANC_DIR" -w -l "$PGBASE/anchor.log" \
  -o "-p $ANC_PORT -k \"$SOCK\" -c listen_addresses=127.0.0.1" start

PSQL="$PGBIN/psql"
echo "== creating databases and applying schema =="
"$PSQL" -h 127.0.0.1 -p "$TXN_PORT" -U bankapp     -d postgres -c 'CREATE DATABASE bankdb;'   >/dev/null 2>&1 || true
"$PSQL" -h 127.0.0.1 -p "$ANC_PORT" -U anchoradmin -d postgres -c 'CREATE DATABASE anchordb;' >/dev/null 2>&1 || true
"$PSQL" -h 127.0.0.1 -p "$TXN_PORT" -U bankapp     -d bankdb   -f "$ROOT/db/01_transaction_schema.sql" >/dev/null
"$PSQL" -h 127.0.0.1 -p "$ANC_PORT" -U anchoradmin -d anchordb -f "$ROOT/deploy/anchor_store.sql"      >/dev/null

echo "== compiling =="
rm -rf "$ROOT/out"; mkdir -p "$ROOT/out"
javac -cp "$JAR" -d "$ROOT/out" "$ROOT"/src/bank/*.java

echo "== running experiment =="
cd "$ROOT"
java -cp "out:$JAR" bank.Experiment

echo "== stopping clusters =="
"$PGBIN/pg_ctl" -D "$TXN_DIR" stop >/dev/null 2>&1 || true
"$PGBIN/pg_ctl" -D "$ANC_DIR" stop >/dev/null 2>&1 || true
echo "== results in $ROOT/results/ =="
ls -1 "$ROOT/results"
