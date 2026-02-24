ENV_FILE := config/local.env

ifneq (,$(wildcard $(ENV_FILE)))
	include $(ENV_FILE)
	export
endif

.PHONY: run-all run-opinions run-mock run-gateway stop-all

run-all: run-opinions run-mock run-gateway

run-opinions:
	@echo "Starting opinions-sv..."
	@./gradlew :opinions-sv:bootRun > opinions.log 2>&1 & \
	echo $$! > /tmp/opinions.pid

run-mock:
	@echo "Starting mock-sv..."
	@./gradlew :mock-sv:bootRun > mock.log 2>&1 & \
	echo $$! > /tmp/mock.pid

run-gateway:
	@echo "Starting gateway-sv..."
	@./gradlew :gateway-sv:bootRun > gateway.log 2>&1 & \
	echo $$! > /tmp/gateway.pid

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