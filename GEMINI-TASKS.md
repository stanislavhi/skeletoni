# Gemini — Action Plan & Tasks

## Action Plan

The goal was to refactor the existing monolithic Spring Boot project into a multi-module Maven project to improve separation of concerns and scalability.

1. **Analyze Project Structure**: Understand the current `pom.xml` and file layout.
2. **Create Module Hierarchy**: Establish a `code` module as the parent for all functional modules, with sub-modules: `contract`, `application`, `domain`, `infrastructure`, `logging`, `observability`.
3. **Configure Maven POMs**: Update the root `pom.xml` to be a parent project. Create `pom.xml` for `code` and all sub-modules. Define dependency management in the root or `code` POM.
4. **Migrate Source Code**: Move the main application class, resources, and tests to the `application` module.
5. **Setup Dependencies**: `application` module depends on all other modules to glue the app together. `infrastructure` contains the heavy dependencies (Spring Data, Kafka, etc.). `contract` and `domain` remain relatively lightweight.
6. **Add Boot and Resilience Modules**: Create `boot` for startup configuration and `resilience` for circuit breaker patterns.

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

- [x] **Updated Root POM**: Converted root `pom.xml` to packaging `pom` and added `code` as a module.

- [x] **Configured Module POMs**:
    - [x] `code/pom.xml`: Defined modules and common dependencies (Lombok).
    - [x] `application/pom.xml`: Added dependencies on sibling modules and `spring-boot-starter-web`.
    - [x] `infrastructure/pom.xml`: Moved Spring Cloud, Kafka, MongoDB, and Postgres dependencies here. Removed Resilience4j.
    - [x] `resilience/pom.xml`: Added `spring-cloud-starter-circuitbreaker-resilience4j`.
    - [x] `boot/pom.xml`: Added dependencies on `application`, `resilience`, and `spring-boot-starter`.
    - [x] `contract/pom.xml`: Created basic POM.
    - [x] `domain/pom.xml`: Created basic POM.
    - [x] `logging/pom.xml`: Created basic POM.
    - [x] `observability/pom.xml`: Created basic POM.

- [x] **Migrated Files**:
    - [x] Moved `SkeletoniApplication.java` to `code/application/src/main/java/turbo/diesel/skeletoni/`
    - [x] Moved `application.yml` to `code/application/src/main/resources/`
    - [x] Moved `SkeletoniApplicationTests.java` to `code/application/src/test/java/turbo/diesel/skeletoni/`

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

---

## Files Created or Modified

| File | Action | Reason |
|---|---|---|
| `pom.xml` (root) | Modified | Converted to multi-module parent POM |
| `code/pom.xml` | Created | Container module grouping all source sub-modules |
| `code/contract/pom.xml` | Created | Contract module POM |
| `code/application/pom.xml` | Created | Application module POM with web and sibling deps |
| `code/domain/pom.xml` | Created | Domain module POM (lightweight) |
| `code/infrastructure/pom.xml` | Created | Infrastructure module POM with DB/broker deps |
| `code/logging/pom.xml` | Created | Logging module POM |
| `code/observability/pom.xml` | Created | Observability module POM |
| `code/boot/pom.xml` | Created | Boot module POM |
| `code/resilience/pom.xml` | Created | Resilience module POM |
| `code/application/src/main/java/.../SkeletoniApplication.java` | Moved | Migrated from root `src/` to application module |
| `code/application/src/main/resources/application.yml` | Moved | Migrated from root `src/` to application module |
| `code/application/src/test/java/.../SkeletoniApplicationTests.java` | Moved | Migrated from root `src/` to application module |