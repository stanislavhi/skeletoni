# 🧠 KNOWLEDGE-BASE.md

This document explains the architectural decisions (the "Why's") behind **skeletoni** and documents known issues, common errors, and their resolutions.

---

## 🏗️ Architectural "Why's"

### 1. Multi-Module Maven Structure
- **Why:** To enforce strict separation of concerns and prevent the "Big Ball of Mud" anti-pattern. By using physical module boundaries, we ensure that business logic (`domain`) cannot accidentally depend on infrastructure details (database, message brokers).
- **Module Roles:**
    - `contract`: API definitions (OpenAPI, AsyncAPI, gRPC). Single source of truth for external communication.
    - `domain`: Pure business logic. Zero dependencies on external frameworks (even Spring).
    - `application`: Orchestration layer. Implements use cases and CQRS.
    - `infrastructure`: Concrete implementations (JPA, Mongo, Kafka, etc.).
    - `boot`: Startup logic and configuration (the "Composition Root").

### 2. Dependency Inversion (Ports & Adapters)
- **Why:** To make the application core independent of external tools. If we decide to swap PostgreSQL for MongoDB or Kafka for RabbitMQ, we only change the `infrastructure` layer. The `application` and `domain` remain untouched.

### 3. Environment Variables for Secrets
- **Why:** Security. Hardcoded credentials in `application.yml` are a risk. We use the `${VARIABLE_NAME:DEFAULT}` syntax to allow overriding sensitive data via environment variables or CI/CD secrets while providing safe defaults for local development.

### 4. MapStruct for Object Mapping
- **Why:** Performance and type safety. Unlike reflection-based mappers (ModelMapper), MapStruct generates plain Java code at compile time. This is faster and catches mapping errors during the build process.

### 5. Multi-Stage Dockerfile
- **Why:** Optimized image size and security. The build stage (Maven) is separated from the final runtime stage (JRE). The final image contains only the compiled code and the JRE, reducing the attack surface and download size.

---

## ⚠️ Known Issues & Common Errors

### 1. Protobuf/gRPC Code Generation
- **Error:** `java: package turbo.diesel.skeletoni.contract.grpc does not exist`
- **Cause:** Maven has not generated the Java classes from the `.proto` files yet.
- **Fix:** Run `mvn generate-sources -pl code/contract`. Ensure the `os-maven-plugin` is active in your IDE (IntelliJ users may need to click "Reload Maven Project").

### 2. MapStruct vs Lombok Conflict
- **Error:** `Target bean does not have a default constructor` or fields are not mapped.
- **Cause:** MapStruct needs to be aware of Lombok-generated getters/setters/builders.
- **Fix:** Ensure `lombok-mapstruct-binding` is included in the `annotationProcessorPaths` of the `maven-compiler-plugin` configuration in the parent `pom.xml`.

### 3. Docker Compose Port Conflicts
- **Error:** `Bind for 0.0.0.0:5432 failed: port is already allocated`
- **Cause:** You likely have a local instance of PostgreSQL (or another service) running on your host machine.
- **Fix:** Either stop your local service or change the host port mapping in `compose.yml` (e.g., `"5433:5432"`).

### 4. Flyway Validation Failures
- **Error:** `Migration checksum mismatch`
- **Cause:** A migration file (`V1__...sql`) was modified after it was already applied to the database.
- **Fix:** Locally, you can run `docker compose down -v` to clear the database. In production, never modify existing migrations; always create a new one.

### 5. Circular Dependencies between Modules
- **Error:** Maven build fails with "Circular dependency detected".
- **Cause:** A violation of the strict dependency direction (e.g., `domain` trying to depend on `application`).
- **Fix:** Re-evaluate the class placement. Review `AGENTS.md` for the allowed dependency flow.

### 6. Missing Local Configuration
- **Error:** `Could not resolve placeholder 'SPRING_DATASOURCE_URL' in value "${SPRING_DATASOURCE_URL}"`
- **Cause:** The `application.yml` uses mandatory environment variables for sensitive data, and no `application-local.yml` or env vars are provided.
- **Fix:** Copy `application-local.yml.example` to `application-local.yml` (this file is git-ignored) or set the required environment variables in your IDE run configuration.

### 7. Application Tests Failing without Infrastructure
- **Error:** `Failed to load ApplicationContext` during tests.
- **Cause:** The test is trying to connect to a real database/broker because the `test` profile is not active or correctly configured.
- **Fix:** Ensure your tests are annotated with `@ActiveProfiles("test")`. The `application-test.yml` in the `boot` module is configured to use H2 and mock/local settings to ensure tests can run without the full Docker stack.

### 8. SonarCloud Credential Disclosure Warning
- **Error:** Sonar flags `application-local.yml.example` as a security risk.
- **Cause:** Sonar's static analysis detects hardcoded dummy passwords in example configuration files.
- **Fix:** These files are already excluded from Sonar analysis via the **`sonar-project.properties`** file at the root. If you create new local configuration templates, ensure they follow the `application-local.yml*` pattern or update the exclusions in that file.

### 9. MapStruct "cannot find symbol" for Value Objects
- **Error:** `Compilation error: cannot find symbol ... ExampleId` in generated `ExampleMapperImpl.java`.
- **Cause:** MapStruct's generated implementation may not automatically import domain value objects used in custom `expression` mappings.
- **Fix:** Use the `imports` attribute in the `@Mapper` annotation: `@Mapper(componentModel = "spring", imports = {ExampleId.class})`.

### 10. Missing Maven Dependency in Central
- **Error:** `Could not find artifact net.devh:grpc-server-spring-boot-starter:jar:3.1.0.Native`
- **Cause:** Some "Native" variants of gRPC starters are not always available or correctly indexed in Maven Central for all architectures.
- **Fix:** Use the stable version: `3.1.0.RELEASE`.

### 11. Kafka Connectivity in Docker Compose
- **Error:** `kafka-ui` or other containers cannot connect to Kafka, or they report `localhost:9092` connectivity issues even when using the `kafka` service name.
- **Cause:** Kafka advertised listeners are misconfigured. If Kafka advertises only `localhost:9092`, other containers will try to connect to their own local loopback.
- **Fix:** Configure multiple listeners in `compose.yml`: one for the host (`PLAINTEXT_HOST://localhost:9092`) and one for the internal Docker network (`PLAINTEXT://kafka:29092`).

### 12. Bitnami Kafka Image Resolution
- **Error:** `Error failed to resolve reference "docker.io/bitnami/kafka:3.9": not found`
- **Cause:** Bitnami's tagging convention for Kafka can sometimes be inconsistent or unavailable on certain registries for specific major versions without a patch number.
- **Fix:** Switched to the official `apache/kafka` image which provides more predictable tag resolution for standard versions like `3.9.0`.

---

## 🛠️ Troubleshooting Commands

- **Full Rebuild:** `mvn clean install -f code/pom.xml -DskipTests`
- **Start Local Infra:** `docker compose up -d`
- **View Logs:** `docker compose logs -f`
- **Clear Everything (Docker):** `docker compose down -v`
