# Make Targets

Production (Docker):

- `make docker-up` start local production stack (includes frontend, generates certs if missing)
- `make docker-down` stop local production stack
- `make docker-build` rebuild images and artifacts
- `make docker-logs` tail logs for all services

Backend (dev only):

- `make local-run-all` run all backend services locally
- `make local-stop-all` stop all backend services
- `make local-run-<svc>` run one backend service (`gateway`, `opinions`, `mock`)
- `make local-stop-<svc>` stop one backend service

Frontend (dev only):

- `make frontend-install` install frontend dependencies
- `make frontend-install-all` install deps and Playwright browsers
- `make frontend-dev` start Vite dev server
- `make frontend-test-unit` run unit tests (Vitest)
- `make frontend-test-e2e` run E2E tests (Playwright)
- `make frontend-clean` remove frontend build output and cache
- `make frontend-clean-all` remove build output, cache, node_modules, certs

Maintenance:

- `make clean-artifacts` remove backend JARs in `deployment/*/app.jar`
- `make clean-logs` remove `deployment/**/*.log`
- `make frontend-build` build production bundle (`dist/`)

Certificates:

- `make frontend-cert` generate local HTTPS certs (OpenSSL)
- `make frontend-cert-clean` remove generated certs
