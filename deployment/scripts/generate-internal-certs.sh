#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CERTS_INTERNAL_DIR="${ROOT_DIR}/deployment/certs/internal"
SERVICES_FILE="${ROOT_DIR}/deployment/config/services.list"
FORCE_REGEN="${FORCE_REGEN_CERTS:-false}"

if [ ! -f "${SERVICES_FILE}" ]; then
  echo "Missing required ${SERVICES_FILE}" >&2
  exit 1
fi

KEYSTORE_PASSWORD="${TLS_KEYSTORE_PASSWORD:?}"
TRUSTSTORE_PASSWORD="${TLS_TRUSTSTORE_PASSWORD:?}"
ALIAS="${SERVER_SSL_KEY_ALIAS:?}"
DAYS="${TLS_CERT_DAYS:?}"

read_services() {
  awk 'NF && $1 !~ /^#/{print $1}' "${SERVICES_FILE}"
}

keystore_valid() {
  local path="$1"
  local password="$2"
  keytool -list -storetype PKCS12 -keystore "${path}" -storepass "${password}" >/dev/null 2>&1
}

all_internal_certs_present_and_valid() {
  local service

  for service in $(read_services); do
    [ -f "${CERTS_INTERNAL_DIR}/keystore-${service}.p12" ] || return 1
    [ -f "${CERTS_INTERNAL_DIR}/${service}.crt" ] || return 1
    keystore_valid "${CERTS_INTERNAL_DIR}/keystore-${service}.p12" "${KEYSTORE_PASSWORD}" || return 1
  done

  [ -f "${CERTS_INTERNAL_DIR}/truststore.p12" ] || return 1
  keystore_valid "${CERTS_INTERNAL_DIR}/truststore.p12" "${TRUSTSTORE_PASSWORD}" || return 1
}

internal_skip=false
if [ "${FORCE_REGEN}" != "true" ] && all_internal_certs_present_and_valid; then
  internal_skip=true
fi

if [ "${internal_skip}" = "true" ]; then
  echo "Internal TLS files already exist and are valid, skipping generation."
  exit 0
fi

rm -rf "${CERTS_INTERNAL_DIR}"
mkdir -p "${CERTS_INTERNAL_DIR}"

generate_keystore() {
  local service="$1"
  local dname="CN=${service}, OU=r8n, O=r8n, L=Local, ST=Local, C=DE"
  local san="dns:${service},dns:localhost,ip:127.0.0.1"
  local svc
  for svc in $(read_services); do
    [ "${svc}" = "${service}" ] && continue
    san="${san},dns:${svc}"
  done
  local keystore="${CERTS_INTERNAL_DIR}/keystore-${service}.p12"
  local certfile="${CERTS_INTERNAL_DIR}/${service}.crt"

  keytool -genkeypair \
    -alias "${ALIAS}" \
    -keyalg RSA \
    -keysize 2048 \
    -validity "${DAYS}" \
    -dname "${dname}" \
    -ext "SAN=${san}" \
    -storetype PKCS12 \
    -keystore "${keystore}" \
    -storepass "${KEYSTORE_PASSWORD}" \
    -keypass "${KEYSTORE_PASSWORD}" \
    -noprompt

  keytool -exportcert \
    -alias "${ALIAS}" \
    -keystore "${keystore}" \
    -storetype PKCS12 \
    -storepass "${KEYSTORE_PASSWORD}" \
    -rfc \
    -file "${certfile}"
}

for service in $(read_services); do
  generate_keystore "${service}"
done

# Truststore contains all service certificates so any service can perform outbound HTTPS calls.
for cert in "${CERTS_INTERNAL_DIR}"/*.crt; do
  alias_name="$(basename "${cert}" .crt)"
  keytool -importcert \
    -alias "${alias_name}" \
    -file "${cert}" \
    -keystore "${CERTS_INTERNAL_DIR}/truststore.p12" \
    -storetype PKCS12 \
    -storepass "${TRUSTSTORE_PASSWORD}" \
    -noprompt
done

echo "Generated internal TLS files in ${CERTS_INTERNAL_DIR}"
