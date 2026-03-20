LOCAL_ENV_FILE := deployment/config/local.env
DOCKER_ENV_FILE := deployment/config/docker.env
LOAD_LOCAL_ENV = set -a; [ -f "$(LOCAL_ENV_FILE)" ] && . "$(LOCAL_ENV_FILE)"; set +a;
SERVICES := gateway opinions mock
BOOT_JAR_TASKS := $(addprefix :,$(addsuffix -sv:bootJar,$(SERVICES)))

.PHONY: local-run-all local-stop-all \
    $(addprefix local-run-,$(SERVICES)) \
    $(addprefix local-stop-,$(SERVICES)) \
    $(addprefix docker-logs-,$(SERVICES)) \
    prebuild-jars prepare-artifacts docker-build docker-up \
    docker-down docker-logs clean-artifacts ensure-log-dirs clean-logs \
    routed-request-opinion routed-request-mock \
    direct-request-opinion direct-request-mock

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

$(addprefix docker-logs-,$(SERVICES)): docker-logs-%:
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml logs -f $*

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
	curl "http://localhost:8080/opinions/00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

routed-request-mock:
	curl "http://localhost:8080/opinion-lists/00000000-0000-0000-0000-000000000000/summary" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-opinion:
	curl "http://localhost:8081/opinions/00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-mock:
	curl "http://localhost:8090/opinion-lists/00000000-0000-0000-0000-000000000000/summary" -i -H "Authorization: Bearer stub-access-token-123"

clean-the-fuck-out-of-this-campus-machine:
	rm -rf ~/.local/share/docker ~/.var/app/com.slack.Slack ~/.config/Code ~/.config/Slack ~/.config/google-chrome && mkdir -p ~/.local/share/docker/tmp && chmod 700 ~/.local/share/docker/tmp

who-ate-all-the-space:
	du --all --human-readable --one-file-system --max-depth=1 ~