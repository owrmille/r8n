LOCAL_ENV_FILE := config/local.env
DOCKER_ENV_FILE := config/docker.env
LOAD_LOCAL_ENV = set -a; [ -f "$(LOCAL_ENV_FILE)" ] && . "$(LOCAL_ENV_FILE)"; set +a;
SERVICES := gateway opinions mock
BOOT_JAR_TASKS := $(addprefix :,$(addsuffix -sv:bootJar,$(SERVICES)))

.PHONY: local-run-all stop-all \
    $(addprefix run-,$(SERVICES)) \
    $(addprefix stop-,$(SERVICES)) \
    prebuild-jars prepare-artifacts docker-build docker-up \
    docker-down docker-logs clean-artifacts ensure-log-dirs clean-logs

docker-up: docker-build ensure-log-dirs
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml up -d

docker-build: prepare-artifacts
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml build

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

docker-down:
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml down

docker-logs:
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml logs -f $(SERVICES)

clean-logs:
	find deployment -type f -name '*.log' -delete

clean-artifacts:
	@for svc in $(SERVICES); do \
		rm -f "deployment/$$svc/app.jar"; \
	done

local-run-all: $(addprefix run-,$(SERVICES))

$(addprefix run-,$(SERVICES)): run-%:
	@echo "Starting $*-sv..."
	@$(LOAD_LOCAL_ENV) \
	cd backend && (./gradlew :$*-sv:bootRun > $*.log 2>&1 & \
	echo $$! > /tmp/$*.pid)

stop-all: $(addprefix stop-,$(SERVICES))

$(addprefix stop-,$(SERVICES)): stop-%:
	-@kill $$(cat /tmp/$*.pid) 2>/dev/null || true

routed-request-opinion:
	curl "http://localhost:8080/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

routed-request-mock:
	curl "http://localhost:8080/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-opinion:
	curl "http://localhost:8081/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-mock:
	curl "http://localhost:8090/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"
