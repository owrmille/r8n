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
FRONTEND_GATEWAY_CERT := $(CURDIR)/deployment/certs/gateway.crt

BOOT_JAR_TASKS := $(addprefix :,$(addsuffix -sv:bootJar,$(SERVICES)))

.PHONY: local-run-all local-stop-all \
    $(addprefix local-run-,$(SERVICES)) \
    $(addprefix local-stop-,$(SERVICES)) \
    $(addprefix docker-logs-,$(SERVICES)) \
    prebuild-jars prepare-artifacts docker-build docker-up \
    docker-certs docker-certs-force \
    docker-down docker-logs clean-artifacts ensure-log-dirs clean-logs \
    routed-request-opinion routed-request-mock \
    direct-request-opinion direct-request-mock \
    https-routed-request-opinion https-routed-request-mock \
    frontend-dev
    direct-request-opinion direct-request-mock \
    routed-request-gdpr build-opinions \
    docker-database-drop-volume-personal docker-database-drop-volume-campus \
    docker-database-run docker-database-connect \
    clean-the-fuck-out-of-this-campus-machine who-ate-all-the-space \
    test-github test-backend lint-backend test-frontend-prepare test-frontend test-e2e help

docker-up: docker-build ensure-log-dirs docker-certs
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml up -d

docker-build: prepare-artifacts
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml build

prepare-artifacts: prebuild-jars
	@set -e; \
	for svc in $(SERVICES); do \
		mkdir -p "deployment/$$svc"; \
		cp "$$(ls backend/$$svc-sv/build/libs/*.jar | grep -v -- '-plain\.jar$$' | head -n1)" "deployment/$$svc/app.jar"; \
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

local-run-all: $(addprefix local-run-,$(SERVICES))

docker-database-drop-volume-personal:
	@rm -rf ./deployment/database/data

docker-database-drop-volume-campus:
	@# yes this is the only way, since some files are owned by root, and you don't have sudo rights in campus
	@docker run --rm -v ./deployment/database:/pg alpine rm -rf /pg/data

docker-database-run:
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

routed-request-opinion:
	curl "http://localhost:8080/opinions/00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

routed-request-mock:
	curl "http://localhost:8080/opinion-lists/00000000-0000-0000-0000-000000000000/summary" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-opinion:
	curl "http://localhost:8081/opinions/00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-mock:
	curl "http://localhost:8090/opinion-lists/00000000-0000-0000-0000-000000000000/summary" -i -H "Authorization: Bearer stub-access-token-123"

routed-request-gdpr:
	curl "http://localhost:8080/users/export" -i -H "Authorization: Bearer stub-access-token-123"

https-routed-request-opinion:
	curl --cacert deployment/certs/gateway.crt "https://localhost:8080/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

https-routed-request-mock:
	curl --cacert deployment/certs/gateway.crt "https://localhost:8080/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

clean-the-fuck-out-of-this-campus-machine:
	docker system prune -f
	rm -rf ~/.local/share/docker ~/.var/app/com.slack.Slack ~/.config/Code ~/.config/Slack ~/.config/google-chrome
	mkdir -p ~/.local/share/docker/tmp && chmod 700 ~/.local/share/docker/tmp
	pkill -f GradleDaemon

who-ate-all-the-space:
	du --all --human-readable --one-file-system --max-depth=1 ~

test-github: test-backend test-frontend test-e2e

test-backend: lint-backend
	cd backend && ./gradlew test

lint-backend:
	cd backend && ./gradlew ktlintCheck

test-frontend-prepare:
	cd frontend && npm ci

test-frontend: test-frontend-prepare
	cd frontend && npm run type-check && npx oxlint && npx eslint . --max-warnings=0 && npm run test:unit -- --run && npm run build-only

test-e2e:
	cd frontend && npx playwright install --with-deps && npm run test:e2e

help:
	@echo "Available Makefile targets:"
	@echo "  docker-up                               - Build and start all services in Docker"
	@echo "  docker-down                             - Stop all Docker services"
	@echo "  docker-logs                             - Show logs for all services"
	@echo "  local-run-all                           - Start all services locally (background)"
	@echo "  local-stop-all                          - Stop all locally running services"
	@echo "  test-github                             - Run all tests (backend, frontend, e2e)"
	@echo "  test-backend                            - Run backend tests (includes lint)"
	@echo "  lint-backend                            - Run backend lint check (ktlint)"
	@echo "  test-frontend                           - Run frontend tests (lint, unit, build)"
	@echo "  test-e2e                                - Run frontend e2e tests (Playwright)"
	@echo "  docker-database-run                     - Run PostgreSQL database in Docker"
	@echo "  docker-database-connect                 - Connect to the running PostgreSQL database"
	@echo "  clean-logs                              - Remove all .log files in deployment folder"
	@echo "  clean-the-fuck-out-of-this-campus-machine - Deep clean for shared lab environments"
	@echo "  who-ate-all-the-space                   - Disk usage summary for current user"
