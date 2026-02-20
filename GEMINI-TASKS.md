# Gemini — Action Plan & Tasks

## Action Plan

The goal was to refactor the existing monolithic Spring Boot project into a multi-module Maven project to improve separation of concerns and scalability.

1. **Analyze Project Structure**: Understand the current `pom.xml` and file layout.
2. **Create Module Hierarchy**: Establish a `code` module as the parent for all functional modules, with sub-modules: `contract`, `application`, `domain`, `infrastructure`, `logging`, `observability`.
3. **Configure Maven POMs**: Update the root `pom.xml` to be a parent project. Create `pom.xml` for `code` and all sub-modules. Define dependency management in the root or `code` POM.
4. **Migrate Source Code**: Move the main application class, resources, and tests to the `application` module.
5. **Setup Dependencies**: `application` module depends on all other modules to glue the app together. `infrastructure` contains the heavy dependencies (Spring Data, Kafka, etc.). `contract` and `domain` remain relatively lightweight.
6. **Add Boot and Resilience Modules**: Create `boot` for startup configuration and `resilience` for circuit breaker patterns.
7. **Refactor Root POM**: Move all dependency management and build configuration to `code/pom.xml` to keep the root POM clean and focused on aggregation.
8. **Move Boot Logic**: Ensure the application entry point resides in the `boot` module, not `application`.
9. **Re-wire Dependencies**: Ensure `boot` module aggregates all necessary modules (`infrastructure`, `logging`, `observability`, `resilience`) to start the app, while `application` remains focused on business logic orchestration.

---

## Tasks Done

- [x] **Analyzed existing project**: Checked `pom.xml` and file list.

- [x] **Created `code` module**: Set up the container module for all source sub-modules.

- [x] **Created sub-modules**:
    - [x] `contract`
    - [x] `application`
    - [x] `domain`
    - [x] `infrastructure`
    - [x] `logging`
    - [x] `observability`
    - [x] `boot`
    - [x] `resilience`

- [x] **Updated Root POM**: Converted root `pom.xml` to packaging `pom` and added `code` as a module. Removed dependency management and build plugins.

- [x] **Configured Module POMs**:
    - [x] `code/pom.xml`: Defined modules, properties, dependency management, and build plugins. Added `boot` and `resilience` modules.
    - [x] `application/pom.xml`: Added dependencies on `contract`, `domain`, and `spring-boot-starter-web`. Removed infrastructure dependencies.
    - [x] `infrastructure/pom.xml`: Moved Spring Cloud, Kafka, MongoDB, and Postgres dependencies here. Removed Resilience4j.
    - [x] `resilience/pom.xml`: Added `spring-cloud-starter-circuitbreaker-resilience4j`.
    - [x] `boot/pom.xml`: Added dependencies on `application`, `infrastructure`, `logging`, `observability`, `resilience`, and `spring-boot-starter`.
    - [x] `contract/pom.xml`: Created basic POM.
    - [x] `domain/pom.xml`: Created basic POM.
    - [x] `logging/pom.xml`: Created basic POM.
    - [x] `observability/pom.xml`: Created basic POM.

- [x] **Migrated Files**:
    - [x] Moved `SkeletoniApplication.java` to `code/boot/src/main/java/turbo/diesel/skeletoni/`
    - [x] Moved `application.yml` to `code/boot/src/main/resources/`
    - [x] Moved `SkeletoniApplicationTests.java` to `code/boot/src/test/java/turbo/diesel/skeletoni/`
    - [x] Cleaned up `code/application` to remove the entry point and config.

- [x] **Attempted Cleanup**: Tried to remove the original `src` folder — requires manual deletion due to tool permissions.

---

## Tasks Pending

- [ ] **Delete original `src/` folder**: Must be done manually by the human developer — Gemini does not have delete permissions on this path.

- [ ] **Add missing plugin configuration**: Parent POM may need `maven-compiler-plugin` (Java 21 source/target), `maven-surefire-plugin`, `maven-failsafe-plugin`, `protobuf-maven-plugin`, and `sonar-maven-plugin` entries. Claude flagged this for review.

- [ ] **Expand `infrastructure/pom.xml`**: Add remaining dependencies — MongoDB, Couchbase, RabbitMQ, MapStruct, gRPC, Micrometer, Testcontainers.

- [ ] **Expand `contract/pom.xml`**: Add `protobuf-java`, `grpc-stub`, `grpc-protobuf`, and SpringDoc OpenAPI dependencies.

- [ ] **Expand `observability/pom.xml`**: Add `micrometer-registry-prometheus`, `spring-boot-starter-actuator`.

- [ ] **Expand `logging/pom.xml`**: Add `logback-classic`, `logstash-logback-encoder` for structured JSON logs.

---

## Decisions & Notes

- **`code` wrapper module**: A dedicated `code/` container module was introduced to cleanly separate the functional source modules from root-level project files.

- **Lombok in `code/pom.xml`**: Lombok was placed at the `code` parent level so all sub-modules inherit it without repeating the dependency.

- **Boot Module**: Created a dedicated `boot` module to house the `SpringBootApplication` entry point and configuration, separating startup logic from business logic.

- **Resilience Module**: Isolated `resilience4j` dependencies into a `resilience` module to enforce clear boundaries for fault tolerance patterns.

- **Root POM Cleanup**: The root `pom.xml` was stripped of most configurations, delegating dependency management and build settings to `code/pom.xml`. This adheres to the principle of keeping the root POM minimal and focused on project aggregation.

- **Dependency Re-wiring**: The `application` module was stripped of infrastructure concerns. The `boot` module now acts as the main composition root, pulling in `application`, `infrastructure`, `logging`, `observability`, and `resilience`.

---

## Files Created or Modified

| File | Action | Reason |
|---|---|---|
| `pom.xml` (root) | Modified | Converted to minimal multi-module parent POM |
| `code/pom.xml` | Modified | Added dependency management, build config, and new modules |
| `code/contract/pom.xml` | Created | Contract module POM |
| `code/application/pom.xml` | Modified | Removed infra deps, kept domain/contract/web |
| `code/domain/pom.xml` | Created | Domain module POM (lightweight) |
| `code/infrastructure/pom.xml` | Created | Infrastructure module POM with DB/broker deps |
| `code/logging/pom.xml` | Created | Logging module POM |
| `code/observability/pom.xml` | Created | Observability module POM |
| `code/boot/pom.xml` | Modified | Added all runtime dependencies |
| `code/resilience/pom.xml` | Created | Resilience module POM |
| `code/boot/src/main/java/.../SkeletoniApplication.java` | Created | Moved entry point to boot module |
| `code/boot/src/main/resources/application.yml` | Created | Moved config to boot module |
| `code/boot/src/test/java/.../SkeletoniApplicationTests.java` | Created | Moved tests to boot module |
| `code/application/src/main/java/.../SkeletoniApplication.java` | Modified | Removed entry point logic |
| `code/application/src/main/resources/application.yml` | Modified | Cleared content |
| `code/application/src/test/java/.../SkeletoniApplicationTests.java` | Modified | Removed test logic |