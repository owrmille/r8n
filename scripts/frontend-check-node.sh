#!/usr/bin/env bash
set -euo pipefail

REQ_VERSION="${FRONTEND_NODE_VERSION:-22.13.0}"

ver_ge() {
  IFS=. read -r r1 r2 r3 <<<"$1"
  IFS=. read -r c1 c2 c3 <<<"$2"
  r1="${r1:-0}"; r2="${r2:-0}"; r3="${r3:-0}"
  c1="${c1:-0}"; c2="${c2:-0}"; c3="${c3:-0}"
  if [ "$c1" -gt "$r1" ]; then return 0; fi
  if [ "$c1" -lt "$r1" ]; then return 1; fi
  if [ "$c2" -gt "$r2" ]; then return 0; fi
  if [ "$c2" -lt "$r2" ]; then return 1; fi
  if [ "$c3" -ge "$r3" ]; then return 0; fi
  return 1
}

check_node() {
  local cur
  cur="$(node -v 2>/dev/null | sed 's/^v//')"
  [ -n "$cur" ] && ver_ge "$REQ_VERSION" "$cur"
}

check_node_nvm() {
  local cur
  cur="$(nvm exec "$REQ_VERSION" node -v 2>/dev/null | tail -n1 | sed 's/^v//')"
  [ -n "$cur" ] && ver_ge "$REQ_VERSION" "$cur"
}

if command -v node >/dev/null 2>&1; then
  if check_node; then
    echo "Node version OK: $(node -v)"
    exit 0
  fi
  echo "Node version too old: $(node -v). Required >= ${REQ_VERSION}."
else
  echo "Node not found. Required >= ${REQ_VERSION}."
fi

if [ -n "${NVM_DIR:-}" ] && [ -s "${NVM_DIR}/nvm.sh" ]; then
  # shellcheck source=/dev/null
  . "${NVM_DIR}/nvm.sh"
elif [ -s "${HOME}/.nvm/nvm.sh" ]; then
  # shellcheck source=/dev/null
  . "${HOME}/.nvm/nvm.sh"
else
  echo "nvm not found. Please install Node ${REQ_VERSION} manually."
  exit 1
fi

echo "Attempting to install/use Node ${REQ_VERSION} via nvm..."
if ! nvm which "${REQ_VERSION}" >/dev/null 2>&1; then
  nvm install "${REQ_VERSION}"
fi
nvm use "${REQ_VERSION}"
hash -r

node_path="$(nvm which "${REQ_VERSION}" 2>/dev/null || true)"
if [ -n "$node_path" ]; then
  node_bin_dir="$(dirname "$node_path")"
  PATH="${node_bin_dir}:${PATH}"
fi

if check_node_nvm; then
  echo "Node version OK: v${REQ_VERSION} (via nvm exec)"
  exit 0
fi

if check_node; then
  echo "Node version OK: $(node -v)"
  exit 0
fi

echo "Node still < ${REQ_VERSION} after nvm. Please install manually."
exit 1
