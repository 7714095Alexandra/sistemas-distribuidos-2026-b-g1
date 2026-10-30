#!/usr/bin/env bash
set -euo pipefail

: "${INVENTORY_BASE_URL:?Set INVENTORY_BASE_URL to the deployed inventory-service origin}"
: "${NON_ADMIN_TOKEN:?Set INVENTORY_NON_ADMIN_TOKEN to a real JWT without ADMIN}"
: "${ADMIN_TOKEN:?Set INVENTORY_ADMIN_TOKEN to a real valid ADMIN JWT}"
: "${DATABASE_URL:?Set INVENTORY_DATABASE_URL for the isolated inventory database}"
: "${PERSISTENCE_CHECK_SQL_BEFORE:?Set SQL returning the pre-test count using :'probe_code'}"
: "${PERSISTENCE_CHECK_SQL_AFTER:?Set SQL returning the post-test count using :'probe_code'}"

command -v curl >/dev/null || { echo "::error::curl is required"; exit 1; }
command -v psql >/dev/null || { echo "::error::psql is required"; exit 1; }

code="cp4-${GITHUB_RUN_ID:-local}-$(date +%s)"
payload=$(printf '{"code":"%s","name":"CP4 authorization probe","category":"security","shade":null,"purchasePrice":10.00,"salePrice":20.00,"initialStock":1}' "$code")
response=$(mktemp)
trap 'rm -f "$response"' EXIT

count_before=$(psql "$DATABASE_URL" --tuples-only --no-align \
  --set=probe_code="$code" --command "$PERSISTENCE_CHECK_SQL_BEFORE")
test "$count_before" = 0 || {
  echo "::error::Persistence precondition was not zero for the unique probe identifier"
  exit 1
}

status=$(
  curl --silent --show-error --output "$response" --write-out '%{http_code}' \
    --request POST "${INVENTORY_BASE_URL%/}/api/v1/inventory/products" \
    --header "Authorization: Bearer ${NON_ADMIN_TOKEN}" \
    --header 'Content-Type: application/json' \
    --data "$payload"
)
test "$status" = 403 || {
  echo "::error::Expected 403 for an authenticated non-ADMIN identity; received $status"
  exit 1
}

count=$(psql "$DATABASE_URL" --tuples-only --no-align \
  --set=probe_code="$code" --command "$PERSISTENCE_CHECK_SQL_AFTER")
test "$count" = 0 || {
  echo "::error::A rejected non-ADMIN request persisted product code $code"
  exit 1
}

admin_status=$(
  curl --silent --show-error --output "$response" --write-out '%{http_code}' \
    --request POST "${INVENTORY_BASE_URL%/}/api/v1/inventory/products" \
    --header "Authorization: Bearer ${ADMIN_TOKEN}" \
    --header 'Content-Type: application/json' \
    --data "$payload"
)
test "$admin_status" = 201 || {
  echo "::error::Expected 201 for the valid ADMIN control; received $admin_status"
  exit 1
}

admin_count=$(psql "$DATABASE_URL" --tuples-only --no-align \
  --set=probe_code="$code" --command "$PERSISTENCE_CHECK_SQL_AFTER")
test "$admin_count" = 1 || {
  echo "::error::The valid ADMIN control did not persist exactly one product"
  exit 1
}

echo "Authorization probe passed: non-ADMIN 403 with no persistence; ADMIN 201 with one persisted product."
