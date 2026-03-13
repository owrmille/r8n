LOCAL_ENV_FILE := deployment/config/local.env
DOCKER_ENV_FILE := deployment/config/docker.env
DOCKER_SECRETS_ENV_FILE := deployment/secrets/docker.secrets.env
DOCKER_COMPOSE_ENV_ARGS := --env-file $(DOCKER_ENV_FILE) --env-file $(DOCKER_SECRETS_ENV_FILE)
LOAD_LOCAL_ENV = set -a; [ -f "$(LOCAL_ENV_FILE)" ] && . "$(LOCAL_ENV_FILE)"; set +a;
LOAD_DOCKER_ENV = set -a; . "$(DOCKER_ENV_FILE)"; . "$(DOCKER_SECRETS_ENV_FILE)"; set +a;
SERVICES_FILE := deployment/config/services.list
ifeq (,$(wildcard $(SERVICES_FILE)))
    $(error Missing $(SERVICES_FILE))
endif
SERVICES := $(shell awk 'NF && $$1 !~ /^\#/{printf "%s ",$$1}' "$(SERVICES_FILE)")
FRONTEND_DIR := frontend
FRONTEND_GATEWAY_CERT := $(CURDIR)/deployment/certs/internal/gateway.crt
BOOT_JAR_TASKS := $(addprefix :,$(addsuffix -sv:bootJar,$(SERVICES)))
FRONTEND_CERT_DIR := deployment/certs/edge
FRONTEND_CERT_KEY := $(FRONTEND_CERT_DIR)/localhost.key
FRONTEND_CERT_CRT := $(FRONTEND_CERT_DIR)/localhost.crt

.PHONY: help local-run-all local-stop-all \
    $(addprefix local-run-,$(SERVICES)) \
    $(addprefix local-stop-,$(SERVICES)) \
    $(addprefix docker-logs-,$(SERVICES)) \
    prebuild-jars prepare-artifacts verify-artifacts docker-build docker-up \
    docker-certs docker-certs-force internal-certs internal-certs-force edge-certs edge-certs-force \
    docker-down docker-logs clean-artifacts ensure-log-dirs clean-logs \
    https-routed-request-opinion https-routed-request-mock \
    frontend-install frontend-install-all frontend-check-node frontend-dev frontend-build \
    frontend-test frontend-test-unit frontend-test-e2e frontend-clean frontend-clean-all frontend-cert frontend-cert-clean

help: ## Show this help
	@awk 'BEGIN {FS=":.*##"} /^##@/ {printf "\n%s:\n", substr($$0,5)} /^[a-zA-Z0-9_.%-]+:.*##/ {printf "  %-32s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

##@ Docker
docker-up: docker-build ensure-log-dirs docker-certs ## Start local Docker stack (builds images, ensures logs, generates certs)
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml up -d

docker-build: verify-artifacts frontend-build ## Build Docker images
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml build

prepare-artifacts: prebuild-jars ## Copy service JARs into deployment/ folders
	@set -e; \
	for svc in $(SERVICES); do \
		mkdir -p "deployment/$$svc"; \
		build_dir="backend/$$svc-sv/build/libs"; \
		if [ -d "backend/$$svc-sv/service/build/libs" ]; then \
			build_dir="backend/$$svc-sv/service/build/libs"; \
		fi; \
		jar_path="$$(find "$$build_dir" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | head -n1)"; \
		if [ -z "$$jar_path" ]; then \
			echo "No bootJar found in $$build_dir for $$svc-sv. Run: cd backend && ./gradlew :$$svc-sv:bootJar" >&2; \
			exit 1; \
		fi; \
		cp "$$jar_path" "deployment/$$svc/app.jar"; \
	done

verify-artifacts: prepare-artifacts ## Verify deployment app.jar manifests (re-copy if invalid)
	@set -e; \
	invalid=false; \
	for svc in $(SERVICES); do \
		jar_path="deployment/$$svc/app.jar"; \
		if [ ! -f "$$jar_path" ]; then \
			echo "Missing $$jar_path"; \
			invalid=true; \
			continue; \
		fi; \
		if ! unzip -p "$$jar_path" META-INF/MANIFEST.MF 2>/dev/null | grep -Eq '^(Main-Class|Start-Class):'; then \
			echo "Invalid bootJar manifest in $$jar_path"; \
			invalid=true; \
		fi; \
	done; \
	if [ "$$invalid" = "true" ]; then \
		echo "Rebuilding artifacts..."; \
		$(MAKE) --no-print-directory prepare-artifacts; \
		for svc in $(SERVICES); do \
			jar_path="deployment/$$svc/app.jar"; \
			if ! unzip -p "$$jar_path" META-INF/MANIFEST.MF 2>/dev/null | grep -Eq '^(Main-Class|Start-Class):'; then \
				echo "Invalid bootJar manifest after rebuild in $$jar_path"; \
				exit 1; \
			fi; \
		done; \
	fi

prebuild-jars: ## Build backend bootJar artifacts
	cd backend && ./gradlew $(BOOT_JAR_TASKS)

ensure-log-dirs: ## Create log directories under deployment/
	@for svc in $(SERVICES); do \
		mkdir -p "deployment/$$svc/logs"; \
	done

##@ Certificates
docker-certs: internal-certs edge-certs ## Generate internal + edge TLS certs (skips if valid)

docker-certs-force: internal-certs-force edge-certs-force ## Force regenerate internal + edge TLS certs

internal-certs: ## Generate internal TLS certs (skips if valid)
	@$(LOAD_DOCKER_ENV) \
	chmod +x ./deployment/scripts/generate-internal-certs.sh && \
	./deployment/scripts/generate-internal-certs.sh

internal-certs-force: ## Force regenerate internal TLS certs
	@$(LOAD_DOCKER_ENV) \
	chmod +x ./deployment/scripts/generate-internal-certs.sh && \
	FORCE_REGEN_CERTS=true ./deployment/scripts/generate-internal-certs.sh

docker-down: ## Stop Docker stack
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml down

docker-logs: ## Tail logs for all services
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml logs -f $(SERVICES)

$(addprefix docker-logs-,$(SERVICES)): docker-logs-%: ## Tail logs for one service
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml logs -f $*

##@ Maintenance
clean-logs: ## Remove deployment log files
	find deployment -type f -name '*.log' -delete

clean-artifacts: ## Remove deployment service JARs
	@for svc in $(SERVICES); do \
		rm -f "deployment/$$svc/app.jar"; \
	done

clean: clean-artifacts clean-logs frontend-clean ## Remove backend artifacts/logs and frontend cache

fclean: clean frontend-clean-all ## Remove clean plus frontend node_modules and certs

re: fclean docker-build ## Full rebuild (clean + docker-build)

##@ Local Backend
local-run-all: $(addprefix local-run-,$(SERVICES)) ## Run all backend services locally

docker-database-drop-volume-personal: ## Delete local Docker DB volume (personal)
	@rm -rf ./deployment/database/data

docker-database-drop-volume-campus: ## Delete local Docker DB volume (campus)
	@# yes this is the only way, since some files are owned by root, and you don't have sudo rights in campus
	@docker run --rm -v ./deployment/database:/pg alpine rm -rf /pg/data

docker-run-database: ## Start only the database container
	chmod a+x deployment/database/init/01_create_schemas.sh
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml up -d database

$(addprefix local-run-,$(SERVICES)): local-run-%: ## Run one backend service locally
	@echo "Starting $*-sv..."
	@$(LOAD_LOCAL_ENV) \
	cd backend && (./gradlew :$*-sv:bootRun --args='--spring.profiles.active=local' > $*.log 2>&1 & \
	echo $$! > /tmp/$*.pid)

build-opinions: ## Build opinions service with tests
	cd backend && ./gradlew :opinions-sv:build | tee build.log

local-stop-all: $(addprefix local-stop-,$(SERVICES)) ## Stop all local backend services

docker-database-connect: ## Connect to local database via psql
	$(LOAD_LOCAL_ENV) \
	docker exec -it database psql -U $$DATABASE_USERNAME -d $$DATABASE_NAME

$(addprefix local-stop-,$(SERVICES)): local-stop-%: ## Stop one local backend service
	-@set -e; \
	if [ -f /tmp/$*.pid ]; then \
		kill $$(cat /tmp/$*.pid) 2>/dev/null || true; \
		rm -f /tmp/$*.pid; \
	fi; \
	$(LOAD_LOCAL_ENV) \
	port=""; \
	case "$*" in \
		gateway) port="$$GATEWAY_PORT" ;; \
		opinions) port="$$SERVICES_OPINIONS_PORT" ;; \
		mock) port="$$SERVICES_MOCK_PORT" ;; \
	esac; \
	if [ -n "$$port" ] && command -v lsof >/dev/null 2>&1; then \
		pids="$$(lsof -ti tcp:$$port 2>/dev/null || true)"; \
		[ -z "$$pids" ] || kill $$pids 2>/dev/null || true; \
	fi

##@ Frontend
frontend-dev: frontend-install ## Start Vite dev server
	cd $(FRONTEND_DIR) && NODE_EXTRA_CA_CERTS="$(FRONTEND_GATEWAY_CERT)" npm run dev

frontend-install: frontend-check-node ## Install frontend dependencies
	cd $(FRONTEND_DIR) && npm ci

frontend-install-all: frontend-install ## Install deps and Playwright browsers
	cd $(FRONTEND_DIR) && npx playwright install

frontend-check-node: ## Check Node.js version (attempts nvm if too old)
	@bash -lc '\
	req="22.13.0"; \
	check_node() { node -e "const req=[22,13,0]; const cur=process.versions.node.split(\".\").map(Number); const ok = cur[0]>req[0] || (cur[0]==req[0] && (cur[1]>req[1] || (cur[1]==req[1] && cur[2]>=req[2]))); if (!ok) process.exit(1);" >/dev/null 2>&1; }; \
	if command -v node >/dev/null 2>&1; then \
		if check_node; then \
			echo "Node version OK: $$(node -v)"; \
			exit 0; \
		fi; \
		echo "Node version too old: $$(node -v). Required >= $$req."; \
	else \
		echo "Node not found. Required >= $$req."; \
	fi; \
	if [ -n "$$NVM_DIR" ] && [ -s "$$NVM_DIR/nvm.sh" ]; then \
		. "$$NVM_DIR/nvm.sh"; \
	elif [ -s "$$HOME/.nvm/nvm.sh" ]; then \
		. "$$HOME/.nvm/nvm.sh"; \
	else \
		echo "nvm not found. Please install Node $$req manually."; \
		exit 1; \
	fi; \
	echo "Attempting to install/use Node $$req via nvm..."; \
	nvm install $$req; \
	nvm use $$req; \
	if check_node; then \
		echo "Node version OK: $$(node -v)"; \
		exit 0; \
	fi; \
	echo "Node still < $$req after nvm. Please install manually."; \
	exit 1; \
	'

frontend-build: frontend-check-node ## Build frontend dist (installs deps if missing)
	@cd $(FRONTEND_DIR) && ( [ -d node_modules ] || npm ci ) && npm run build

frontend-test-unit: frontend-check-node ## Run frontend unit tests
	cd $(FRONTEND_DIR) && npm run test:unit

frontend-test: frontend-test-unit frontend-test-e2e ## Run all frontend tests

frontend-test-e2e: frontend-check-node ## Run frontend E2E tests
	cd $(FRONTEND_DIR) && npm run test:e2e

frontend-clean: ## Remove frontend build output, cache, and test artifacts
	rm -rf $(FRONTEND_DIR)/dist $(FRONTEND_DIR)/node_modules/.vite $(FRONTEND_DIR)/playwright-report $(FRONTEND_DIR)/test-results $(FRONTEND_DIR)/coverage

frontend-clean-all: frontend-clean frontend-cert-clean ## Remove frontend build output, cache, node_modules, and certs
	rm -rf $(FRONTEND_DIR)/node_modules

edge-certs: ## Generate edge (frontend) HTTPS certs
	@./deployment/scripts/generate-edge-certs.sh

edge-certs-force: ## Force regenerate edge (frontend) HTTPS certs
	@FORCE_REGEN_CERTS=true ./deployment/scripts/generate-edge-certs.sh

frontend-cert: edge-certs ## Generate frontend HTTPS certs

frontend-cert-clean: ## Remove generated frontend certs
	rm -rf deployment/certs/edge

##@ Smoke Tests
https-routed-request-opinion: ## HTTPS gateway request to opinions
	curl --cacert deployment/certs/internal/gateway.crt "https://localhost:8080/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

https-routed-request-mock: ## HTTPS gateway request to mock
	curl --cacert deployment/certs/internal/gateway.crt "https://localhost:8080/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

##@ Misc
clean-the-fuck-out-of-this-campus-machine: ## Remove large local caches (campus machine only)
	rm -rf ~/.local/share/docker ~/.var/app/com.slack.Slack ~/.config/Code ~/.config/Slack ~/.config/google-chrome && mkdir -p ~/.local/share/docker/tmp && chmod 700 ~/.local/share/docker/tmp

who-ate-all-the-space: ## Show top-level disk usage in home
	du --all --human-readable --one-file-system --max-depth=1 ~
