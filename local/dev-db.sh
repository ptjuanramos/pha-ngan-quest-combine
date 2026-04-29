#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

set -a
source "$SCRIPT_DIR/../backend/.env"
set +a

docker compose -f "$SCRIPT_DIR/docker-compose.yml" "$@"