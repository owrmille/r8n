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
SERVICES := $(shell awk 'NF && $$1 !~ /^#/{printf "%s ",$$1}' "$(SERVICES_FILE)")
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
    docker-database-drop-volume-personal \
    docker-database-drop-volume-campus \
    docker-run-database docker-database-connect \
    build-opinions \
    who-ate-all-the-space clean-the-fuck-out-of-this-campus-machine \
    frontend-dev \
    clean fclean all \
    gradle-%-bootJar \
    lint-makefile check-makefile

# Dynamic service artifacts configuration
SERVICE_JARS := $(foreach svc,$(SERVICES),deployment/$(svc)/app.jar)

# Aggregate per-service Gradle bootJar targets
GRADLE_BOOT_JARS := $(addprefix gradle-,$(addsuffix -bootJar,$(SERVICES)))

# Docker helpers

all: docker-up

docker-up: docker-build ensure-log-dirs docker-certs
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml up -d

docker-build: prepare-artifacts
	docker compose $(DOCKER_COMPOSE_ENV_ARGS) -f docker-compose.yml build

# Preparing artifacts copies built jars; building is handled by per-service Gradle targets
prepare-artifacts: $(SERVICE_JARS)

# Build bootJar for a specific service. Gradle decides if work is needed (up-to-date check).
gradle-%-bootJar:
	cd backend && ./gradlew :$*-sv:bootJar

# Copy the latest non-plain jar for the service into deployment/<svc>/app.jar
# Depends on the Gradle build of that specific service only.
deployment/%/app.jar: gradle-%-bootJar
	@mkdir -p deployment/$*
	cp "$$(ls backend/$*-sv/service/build/libs/*.jar | grep -v -- '-plain\.jar$$' | head -n1)" "$@"

# Keep a target to build all service jars without copying them
prebuild-jars: $(GRADLE_BOOT_JARS)

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

# for local-* runs
routed-request-opinion:
	curl "http://localhost:8080/opinions/30000000-0000-0000-0000-000000000001" -i -H "Authorization: Bearer stub-access-token-123"

routed-request-mock:
	curl "http://localhost:8080/opinion-lists/00000000-0000-0000-0000-000000000000/summary" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-opinion:
	curl "http://localhost:8081/opinions/30000000-0000-0000-0000-000000000001" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-mock:
	curl "http://localhost:8090/opinion-lists/00000000-0000-0000-0000-000000000000/summary" -i -H "Authorization: Bearer stub-access-token-123"

# for docker- runs
https-routed-request-opinion:
	curl --cacert deployment/certs/gateway.crt "https://localhost:8080/opinions/00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

https-routed-request-mock:
	curl --cacert deployment/certs/gateway.crt "https://localhost:8080/opinion-lists/00000000-0000-0000-0000-000000000000/summary" -i -H "Authorization: Bearer stub-access-token-123"

# cleanup
clean-the-fuck-out-of-this-campus-machine:
	@echo "Stopping Docker service..."
	-systemctl --user stop docker.service
	@echo "Pruning Docker system..."
	-docker system prune -f
	@echo "Deleting Docker root directory and other caches..."
	-rm -rf ~/.local/share/docker ~/.var/app/com.slack.Slack ~/.config/Code ~/.config/Slack ~/.config/google-chrome
	@echo "Recreating Docker tmp directory..."
	mkdir -p ~/.local/share/docker/tmp && chmod 700 ~/.local/share/docker/tmp
	@echo "Restarting Docker service..."
	-systemctl --user start docker.service
	@echo "Killing Gradle daemons..."
	-pkill -f GradleDaemon
	@echo "Cleanup complete"

who-ate-all-the-space:
	du --all --human-readable --one-file-system --max-depth=1 ~

clean: clean-logs
	cd backend && ./gradlew clean

fclean: clean clean-artifacts

# linters
check-makefile:
	chmod +x utils/lint-makefile.sh
	./utils/lint-makefile.sh

# utils
move-gradle-to-sgoinfre:
	pkill -f GradleDaemon
	rm -rf /sgoinfre/goinfre/Perso/$USER/gradle ~/.gradle
	ln -s /sgoinfre/goinfre/Perso/$USER/gradle ~/.gradle