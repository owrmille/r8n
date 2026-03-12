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
    prebuild-jars prepare-artifacts docker-build docker-up \
    docker-certs docker-certs-force \
    docker-down docker-logs clean-artifacts ensure-log-dirs clean-logs \
    https-routed-request-opinion https-routed-request-mock \
    frontend-install frontend-install-all frontend-check-node frontend-dev frontend-build \
    frontend-test-unit frontend-test-e2e frontend-clean frontend-clean-all frontend-cert frontend-cert-clean

help:
	@cat docs/make-help.md

docker-up: docker-build ensure-log-dirs docker-certs
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml up -d

docker-build: prepare-artifacts
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml build

prepare-artifacts: prebuild-jars
	@set -e; \
	for svc in $(SERVICES); do \
		mkdir -p "deployment/$$svc"; \
		build_dir="backend/$$svc-sv/build/libs"; \
		if [ -d "backend/$$svc-sv/service/build/libs" ]; then \
			build_dir="backend/$$svc-sv/service/build/libs"; \
		fi; \
		jar_path="$$(find "$$build_dir" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n1)"; \
		if [ -z "$$jar_path" ]; then \
			echo "No bootJar found in $$build_dir for $$svc-sv. Run: cd backend && ./gradlew :$$svc-sv:bootJar" >&2; \
			exit 1; \
		fi; \
		cp "$$jar_path" "deployment/$$svc/app.jar"; \
	done

prebuild-jars:
	cd backend && ./gradlew $(BOOT_JAR_TASKS)

ensure-log-dirs:
	@for svc in $(SERVICES); do \
		mkdir -p "deployment/$$svc/logs"; \
	done

docker-certs:
	@$(LOAD_DOCKER_ENV) \
	chmod +x ./deployment/scripts/generate-certs.sh && \
	./deployment/scripts/generate-certs.sh

docker-certs-force:
	@$(LOAD_DOCKER_ENV) \
	chmod +x ./deployment/scripts/generate-certs.sh && \
	FORCE_REGEN_CERTS=true ./deployment/scripts/generate-certs.sh

docker-down:
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml down

docker-logs:
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml logs -f $(SERVICES)

$(addprefix docker-logs-,$(SERVICES)): docker-logs-%:
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml logs -f $*

clean-logs:
	find deployment -type f -name '*.log' -delete

clean-artifacts:
	@for svc in $(SERVICES); do \
		rm -f "deployment/$$svc/app.jar"; \
	done

clean: clean-artifacts clean-logs frontend-clean

fclean: clean frontend-clean-all

re: fclean docker-build

local-run-all: $(addprefix local-run-,$(SERVICES))

docker-database-drop-volume-personal:
	@rm -rf ./deployment/database/data

docker-database-drop-volume-campus:
	@# yes this is the only way, since some files are owned by root, and you don't have sudo rights in campus
	@docker run --rm -v ./deployment/database:/pg alpine rm -rf /pg/data

docker-run-database:
	chmod a+x deployment/database/init/01_create_schemas.sh
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml up -d database

$(addprefix local-run-,$(SERVICES)): local-run-%:
	@echo "Starting $*-sv..."
	@$(LOAD_LOCAL_ENV) \
	cd backend && (./gradlew :$*-sv:bootRun --args='--spring.profiles.active=local' > $*.log 2>&1 & \
	echo $$! > /tmp/$*.pid)

build-opinions:
	cd backend && ./gradlew :opinions-sv:build | tee build.log

local-stop-all: $(addprefix local-stop-,$(SERVICES))

docker-database-connect:
	$(LOAD_LOCAL_ENV) \
	docker exec -it database psql -U $$DATABASE_USERNAME -d $$DATABASE_NAME

$(addprefix local-stop-,$(SERVICES)): local-stop-%:
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

frontend-dev:
	cd $(FRONTEND_DIR) && NODE_EXTRA_CA_CERTS="$(FRONTEND_GATEWAY_CERT)" npm run dev

frontend-install:
	cd $(FRONTEND_DIR) && npm ci

frontend-install-all: frontend-install
	cd $(FRONTEND_DIR) && npx playwright install

frontend-check-node:
	@node -e 'const [M,m]=process.versions.node.split(".").map(Number); if (M<22 || (M===22 && m<13)) {console.error("Node >=22.13.0 required. Run: cd frontend && nvm install && nvm use"); process.exit(1)}'
	@echo "Node version OK: $$(node -v)"

frontend-build:
	@cd $(FRONTEND_DIR) && ( [ -d node_modules ] || npm ci ) && npm run build

frontend-test-unit:
	cd $(FRONTEND_DIR) && npm run test:unit

frontend-test-e2e:
	cd $(FRONTEND_DIR) && npm run test:e2e

frontend-clean:
	rm -rf $(FRONTEND_DIR)/dist $(FRONTEND_DIR)/node_modules/.vite

frontend-clean-all: frontend-clean frontend-cert-clean
	rm -rf $(FRONTEND_DIR)/node_modules

frontend-cert:
	@./scripts/gen-frontend-cert.sh

frontend-cert-clean:
	rm -rf deployment/certs/edge

https-routed-request-opinion:
	curl --cacert deployment/certs/internal/gateway.crt "https://localhost:8080/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

https-routed-request-mock:
	curl --cacert deployment/certs/internal/gateway.crt "https://localhost:8080/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

clean-the-fuck-out-of-this-campus-machine:
	rm -rf ~/.local/share/docker ~/.var/app/com.slack.Slack ~/.config/Code ~/.config/Slack ~/.config/google-chrome && mkdir -p ~/.local/share/docker/tmp && chmod 700 ~/.local/share/docker/tmp

who-ate-all-the-space:
	du --all --human-readable --one-file-system --max-depth=1 ~
