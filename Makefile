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

$(addprefix local-run-,$(SERVICES)): local-run-%:
	@echo "Starting $*-sv..."
	@$(LOAD_LOCAL_ENV) \
	cd backend && (./gradlew :$*-sv:bootRun > $*.log 2>&1 & \
	echo $$! > /tmp/$*.pid)

local-stop-all: $(addprefix local-stop-,$(SERVICES))

$(addprefix local-stop-,$(SERVICES)): local-stop-%:
	-@kill $$(cat /tmp/$*.pid) 2>/dev/null || true

frontend-dev:
	cd $(FRONTEND_DIR) && NODE_EXTRA_CA_CERTS="$(FRONTEND_GATEWAY_CERT)" npm run dev

routed-request-opinion:
	curl "http://localhost:8080/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

routed-request-mock:
	curl "http://localhost:8080/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-opinion:
	curl "http://localhost:8081/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-mock:
	curl "http://localhost:8090/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

https-routed-request-opinion:
	curl --cacert deployment/certs/gateway.crt "https://localhost:8080/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

https-routed-request-mock:
	curl --cacert deployment/certs/gateway.crt "https://localhost:8080/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"
