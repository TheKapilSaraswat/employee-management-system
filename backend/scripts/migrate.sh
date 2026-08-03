#!/usr/bin/env bash
# Apply SQL migrations to the Employee Management System database.
# Usage: DATABASE_URL="postgres://user:pass@host:5432/dbname" ./migrate.sh [path/to/migration.sql]
set -euo pipefail

: "${DATABASE_URL:?Set DATABASE_URL (postgres://user:pass@host:5432/dbname) }"

if ! command -v psql >/dev/null 2>&1; then
    echo "psql not found. Install PostgreSQL client tools." >&2
    exit 1
fi

MIGRATION_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/sql"
TARGET="${1:-$MIGRATION_DIR/V1__init.sql}"

if [ ! -f "$TARGET" ]; then
    echo "Migration file not found: $TARGET" >&2
    exit 1
fi

echo "Applying $TARGET ..."
psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f "$TARGET"
echo "Migration applied successfully."
