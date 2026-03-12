LOCAL_ENV_FILE := deployment/config/local.env
DOCKER_ENV_FILE := deployment/config/docker.env
DOCKER_ENV_FILE_LOCAL := deployment/config/docker.local.env
LOAD_LOCAL_ENV = set -a; [ -f "$(LOCAL_ENV_FILE)" ] && . "$(LOCAL_ENV_FILE)"; set +a;
DOCKER_ENV_VARS := \
	GATEWAY_HOST \
	GATEWAY_CONTAINER_PORT \
	GATEWAY_HOST_PORT \
	INTERSERVICE_PROTOCOL \
	LOGGING_FILE_NAME \
	SERVICES_MOCK_HOST \
	SERVICES_MOCK_PORT \
	SERVICES_OPINIONS_HOST \
	SERVICES_OPINIONS_PORT \
	FRONTEND_HTTP_HOST_PORT \
	FRONTEND_HTTPS_HOST_PORT
UNSET_DOCKER_ENV = env $(foreach var,$(DOCKER_ENV_VARS),-u $(var))
SERVICES := gateway opinions mock
BOOT_JAR_TASKS := $(addprefix :,$(addsuffix -sv:bootJar,$(SERVICES)))
FRONTEND_CERT_DIR := deployment/certs
FRONTEND_CERT_KEY := $(FRONTEND_CERT_DIR)/localhost.key
FRONTEND_CERT_CRT := $(FRONTEND_CERT_DIR)/localhost.crt

DOCKER_ENV_ARGS := --env-file $(DOCKER_ENV_FILE)
ifneq ("$(wildcard $(DOCKER_ENV_FILE_LOCAL))","")
DOCKER_ENV_ARGS := $(DOCKER_ENV_ARGS) --env-file $(DOCKER_ENV_FILE_LOCAL)
endif

.PHONY: help local-run-all local-stop-all \
    $(addprefix local-run-,$(SERVICES)) \
    $(addprefix local-stop-,$(SERVICES)) \
    $(addprefix docker-logs-,$(SERVICES)) \
    prebuild-jars prepare-artifacts docker-build docker-up \
    docker-down docker-logs clean clean-artifacts ensure-log-dirs clean-logs fclean re \
    routed-request-opinion routed-request-mock \
    direct-request-opinion direct-request-mock \
    frontend-install frontend-install-all frontend-dev frontend-build frontend-clean frontend-clean-all frontend-cert frontend-cert-clean \
    frontend-check-node \
    frontend-test-unit frontend-test-e2e

help:
	@cat docs/make-help.md

docker-up: frontend-cert docker-build ensure-log-dirs
	$(UNSET_DOCKER_ENV) docker compose $(DOCKER_ENV_ARGS) -f docker-compose.yml up -d

docker-build: frontend-build prepare-artifacts
	$(UNSET_DOCKER_ENV) docker compose $(DOCKER_ENV_ARGS) -f docker-compose.yml build

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

docker-down:
	$(UNSET_DOCKER_ENV) docker compose $(DOCKER_ENV_ARGS) -f docker-compose.yml down

docker-logs:
	$(UNSET_DOCKER_ENV) docker compose $(DOCKER_ENV_ARGS) -f docker-compose.yml logs -f $(SERVICES)

$(addprefix docker-logs-,$(SERVICES)): docker-logs-%:
	$(UNSET_DOCKER_ENV) docker compose $(DOCKER_ENV_ARGS) -f docker-compose.yml logs -f $*

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
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml up -d database

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
	-@kill $$(cat /tmp/$*.pid) 2>/dev/null || true

routed-request-opinion:
	curl "http://localhost:8080/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

routed-request-mock:
	curl "http://localhost:8080/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-opinion:
	curl "http://localhost:8081/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-mock:
	curl "http://localhost:8090/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

frontend-install:
	cd frontend && npm ci

frontend-install-all: frontend-install
	cd frontend && npx playwright install

frontend-check-node:
	@node -e 'const [M,m]=process.versions.node.split(".").map(Number); if (M<22 || (M===22 && m<13)) {console.error("Node >=22.13.0 required. Run: cd frontend && nvm install && nvm use"); process.exit(1)}'
	@echo "Node version OK: $$(node -v)"

frontend-dev:
	cd frontend && npm run dev

frontend-build:
	@cd frontend && ( [ -d node_modules ] || npm ci ) && npm run build

frontend-test-unit:
	cd frontend && npm run test:unit

frontend-test-e2e:
	cd frontend && npm run test:e2e

frontend-clean:
	rm -rf frontend/dist frontend/node_modules/.vite

frontend-clean-all: frontend-clean frontend-cert-clean
	rm -rf frontend/node_modules

frontend-cert:
	@./scripts/gen-frontend-cert.sh

frontend-cert-clean:
	rm -rf deployment/certs

clean-the-fuck-out-of-this-campus-machine:
	rm -rf ~/.local/share/docker ~/.var/app/com.slack.Slack ~/.config/Code ~/.config/Slack ~/.config/google-chrome && mkdir -p ~/.local/share/docker/tmp && chmod 700 ~/.local/share/docker/tmp

who-ate-all-the-space:
	du --all --human-readable --one-file-system --max-depth=1 ~
