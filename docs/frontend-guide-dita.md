# Frontend Guide (Dita)

This guide explains the frontend workflow, the difference between dev and build, and how to clean artifacts.

## What Vite Does

Vite is the frontend tooling used here. It has two main modes:

1. Dev server for local development with hot reload.
2. Build step that produces optimized static files for production.

## Dev vs Build

Dev:
- Command: `npm run dev` (or `make frontend-dev`)
- Starts a local server at `http://localhost:5173`
- Fast, unoptimized assets, hot reload
- Does not create `dist/`

Build:
- Command: `npm run build` (or `make frontend-build`)
- Produces optimized static files in `frontend/dist`
- No server is started
- Output is meant to be served by Nginx or another static server

## What Is dist

`dist/` stands for distribution. It contains the compiled HTML, CSS, and JS files that are ready for production.
This folder is generated and should not be committed to git.

## Clean Build Artifacts

To remove the build output and Vite cache:

- `make frontend-clean`

This deletes:
- `frontend/dist`
- `frontend/node_modules/.vite`

## Typical Workflow

1. Install deps: `make frontend-install`
2. Run dev server: `make frontend-dev`
3. Build production bundle: `make frontend-build`
4. Clean artifacts: `make frontend-clean`

## Local Production With Nginx + HTTPS (OpenSSL)

This project uses Nginx to serve the built frontend and proxy `/api` to the gateway.
HTTPS is provided by a self-signed certificate generated with OpenSSL.

### Generate Local Certs

Run:

- `make frontend-cert`

This creates:
- `deployment/certs/localhost.crt`
- `deployment/certs/localhost.key`

Implementation:
- `scripts/gen-frontend-cert.sh`

Note: Browsers will show a warning because the certificate is self-signed. You can proceed anyway for local use.

### Start the Stack

Run:

- `make docker-up`

Then open:
- `https://localhost`

HTTP (`http://localhost`) will redirect to HTTPS.
