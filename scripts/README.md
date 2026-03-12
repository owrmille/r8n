# Certificate Generation (Local)

This project uses a self-signed certificate for local HTTPS only.
The certificate is generated on your machine and mounted into the Nginx container at runtime.
Do not commit certificates or private keys to git.

## Certificate Details

The generated certificate is:
- Self-signed X.509 (OpenSSL)
- RSA 2048-bit key
- SHA-256 signature
- Valid for 365 days
- Subject CN: `localhost`
- SANs: `localhost`, `127.0.0.1`, `::1`

## Generate

Option 1 (recommended):

- `make frontend-cert`

Option 2 (direct script):

- `./scripts/gen-frontend-cert.sh`

This creates:
- `deployment/certs/edge/localhost.crt`
- `deployment/certs/edge/localhost.key`
- `deployment/certs/edge/openssl.cnf`

## Use

After generating certs:

- `make docker-up`

Then open:
- `https://localhost:8443`

Optional:
- `http://localhost:8088` redirects to HTTPS (only if you map the HTTP port)

## Notes

- Browsers will warn because the cert is self-signed. Proceed anyway for local use.
- If you change hostnames, update the script and regenerate.
