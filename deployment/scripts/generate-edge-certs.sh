#!/bin/sh
set -eu

CERT_DIR="${CERT_DIR:-deployment/certs/edge}"
CERT_KEY="${CERT_DIR}/localhost.key"
CERT_CRT="${CERT_DIR}/localhost.crt"
CERT_CONF="${CERT_DIR}/openssl.cnf"

mkdir -p "${CERT_DIR}"

if [ "${FORCE_REGEN_CERTS:-false}" = "true" ]; then
  rm -f "${CERT_KEY}" "${CERT_CRT}" "${CERT_CONF}"
fi

if [ -f "${CERT_KEY}" ] && [ -f "${CERT_CRT}" ]; then
  echo "Certificate already exists: ${CERT_CRT}"
  exit 0
fi

cat > "${CERT_CONF}" <<'EOF'
[req]
default_bits = 2048
prompt = no
default_md = sha256
distinguished_name = dn
x509_extensions = v3_req

[dn]
C = XX
ST = Local
L = Local
O = r8n
OU = dev
CN = localhost

[v3_req]
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "${CERT_KEY}" -out "${CERT_CRT}" \
  -config "${CERT_CONF}"

echo "Generated ${CERT_CRT} and ${CERT_KEY}"
