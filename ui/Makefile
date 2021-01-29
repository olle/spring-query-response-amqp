JAVA_HOME=$(shell unset JAVA_HOME; /usr/libexec/java_home -v 11)

.PHONY: run dev build dist clean test lint

#
# Starting watch for client-side, or frontend assets, and running the backend
# server for e2e integration.
#
run: node_modules
	./mvnw clean
	npm run watch & \
	  sleep 5; ./mvnw spring-boot:run ; wait

#
# Focused on client-side, this target will run a front-end only server, so
# no backend service.
#
dev: node_modules
	npm run serve

#
# This target is issued by the Maven `package` goal. Used when building the
# distribution JAR file.
#
build: node_modules
	npm run build

#
# Target to ensure packaging of both the Java/Maven build and the client.
# NOTE: maven triggers the build target (above).
#
dist:
	./mvnw package

node_modules:
	npm install

test:
	npm run test

lint:
	npm run lint

clean:
	rm -rf target/
	rm -rf node_modules/
