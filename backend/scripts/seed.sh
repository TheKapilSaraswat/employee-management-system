#!/usr/bin/env bash
# Seed reference data into the Employee Management System database.
# Usage: DATABASE_URL="postgres://user:pass@host:5432/dbname" ./seed.sh
set -euo pipefail

: "${DATABASE_URL:?Set DATABASE_URL (postgres://user:pass@host:5432/dbname) }"

if ! command -v psql >/dev/null 2>&1; then
    echo "psql not found. Install PostgreSQL client tools." >&2
    exit 1
fi

SEED_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/sql/seed.sql"

echo "Applying $SEED_FILE ..."
psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f "$SEED_FILE"
echo "Seed applied successfully."
