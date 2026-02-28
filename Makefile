LOCAL_ENV_FILE := config/local.env
DOCKER_ENV_FILE := config/docker.env
LOAD_LOCAL_ENV = set -a; [ -f "$(LOCAL_ENV_FILE)" ] && . "$(LOCAL_ENV_FILE)"; set +a;

.PHONY: run-all run-opinions run-mock run-gateway stop-all \
    prebuild-jars prepare-artifacts docker-build docker-up \
    docker-down docker-logs clean-artifacts ensure-log-dirs clean-logs

docker-up: docker-build ensure-log-dirs
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml up -d

docker-build: prepare-artifacts
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml build

prepare-artifacts: prebuild-jars
	mkdir -p deployment/gateway deployment/opinions deployment/mock
	cp "$$(ls backend/gateway-sv/build/libs/*.jar | grep -v -- '-plain\.jar$$' | head -n1)" deployment/gateway/app.jar
	cp "$$(ls backend/opinions-sv/build/libs/*.jar | grep -v -- '-plain\.jar$$' | head -n1)" deployment/opinions/app.jar
	cp "$$(ls backend/mock-sv/build/libs/*.jar | grep -v -- '-plain\.jar$$' | head -n1)" deployment/mock/app.jar

prebuild-jars:
	cd backend && ./gradlew :gateway-sv:bootJar :opinions-sv:bootJar :mock-sv:bootJar

ensure-log-dirs:
	mkdir -p deployment/gateway/logs deployment/opinions/logs deployment/mock/logs

docker-down:
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml down

docker-logs:
	docker compose --env-file $(DOCKER_ENV_FILE) -f docker-compose.yml logs -f gateway opinions mock

clean-logs:
	rm -rf deployment/gateway/logs/* deployment/opinions/logs/* deployment/mock/logs/*

clean-artifacts:
	rm -f deployment/gateway/app.jar deployment/opinions/app.jar deployment/mock/app.jar

local-run-all: run-opinions run-mock run-gateway

run-opinions:
	@echo "Starting opinions-sv..."
	@$(LOAD_LOCAL_ENV) \
	cd backend && (./gradlew :opinions-sv:bootRun > opinions.log 2>&1 & \
	echo $$! > /tmp/opinions.pid)

run-mock:
	@echo "Starting mock-sv..."
	@$(LOAD_LOCAL_ENV) \
	cd backend && (./gradlew :mock-sv:bootRun > mock.log 2>&1 & \
	echo $$! > /tmp/mock.pid)

run-gateway:
	@echo "Starting gateway-sv..."
	@$(LOAD_LOCAL_ENV) \
	cd backend && (./gradlew :gateway-sv:bootRun > gateway.log 2>&1 & \
	echo $$! > /tmp/gateway.pid)

stop-all:
	-@kill $$(cat /tmp/opinions.pid) 2>/dev/null || true
	-@kill $$(cat /tmp/mock.pid) 2>/dev/null || true
	-@kill $$(cat /tmp/gateway.pid) 2>/dev/null || true

routed-request-opinion:
	curl "http://localhost:8080/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

routed-request-mock:
	curl "http://localhost:8080/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-opinion:
	curl "http://localhost:8081/opinions/id?id=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"

direct-request-mock:
	curl "http://localhost:8090/opinionLists/summary?listId=00000000-0000-0000-0000-000000000000" -i -H "Authorization: Bearer stub-access-token-123"
