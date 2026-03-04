# Antigravity — Action Plan & Tasks

## Action Plan
I am Antigravity, an expert coding assistant. My goal is to continue the development of the **skeletoni** project, picking up where Gemini and Claude left off. I will focus on implementing the infrastructure, configuration, and skeleton classes as defined in the architectural vision.

1.  Initialize `ANTIGRAVITY-TASKS.md`.
2.  Review existing POM files for completeness and correctness.
3.  Create `compose.yml` for local development infrastructure.
4.  Create a comprehensive `application.yml` in the `boot` module.
5.  Create a multi-stage `Dockerfile`.
6.  Set up GitHub Actions workflows (`ci.yml`, `release.yml`).
7.  Define API contracts (`openapi.yml`, `asyncapi.yml`).
8.  Implement skeleton classes across modules (domain, application, infrastructure).

## Tasks Done

- [x] **Initialize `ANTIGRAVITY-TASKS.md`**: Set up the action plan and task list.
- [x] **Review and Fix POMs**: Created root `pom.xml`, updated `code/pom.xml` with plugins (MapStruct, Lombok, Sonar, Protobuf), fixed `contract/pom.xml` dependency on `domain`, and added missing infra dependencies (Couchbase, RabbitMQ, gRPC).
- [x] **Create `compose.yml`**: Full local stack including Postgres, Mongo, Couchbase, Kafka, RabbitMQ, Prometheus, and Grafana.
- [x] **Configure `application.yml`**: Comprehensive configuration for all external integrations and Actuator. Externalized sensitive data using environment variables (`${VAR:DEFAULT}`).
- [x] **Secret Management Refined**: Externalized all credentials in `application.yml` and removed default passwords. Created `application-local.yml.example` and a git-ignored `application-local.yml` for local development defaults.
- [x] **Application Tests**: Enhanced `SkeletoniApplicationTests.java` with assertions and added `application-test.yml` using H2 to ensure context loads test passes without real infrastructure.
- [x] **Sonar Configuration**: Excluded `application-local.yml` and `application-local.yml.example` from Sonar rules to avoid credential disclosure false positives.
- [x] **Architecture Documentation**: Updated `KNOWLEDGE-BASE.md` with additional troubleshooting for missing local config, test profiles, and Sonar warnings.
- [x] **Refined Project Docs**: Updated `README.md` to accurately reflect the `code/` module structure and updated the `.gitignore`.
- [x] **Project Metadata**: Added `LICENSE` (MIT) and `CONTRIBUTING.md`.
- [x] **Flyway Migration**: Added an initial `V1__init_examples.sql` migration in the `infrastructure` module for the example entity.
- [x] **Create `Dockerfile`**: Multi-stage Maven build and JRE runtime.
- [x] **GitHub Actions**: Created `ci.yml` and `release.yml`.
- [x] **API Contracts**: Defined `openapi.yml`, `asyncapi.yml`, and `example.proto`.
- [x] **Skeleton Classes**: Implemented example domain model, application services/ports/controllers, and infrastructure persistence adapters with MapStruct.
- [x] **Logging**: Added `logback-spring.xml` with Logstash support.
- [x] **Verification**: Successfully compiled the project, verified the application starts with the `local` profile, and confirmed the `docker compose` stack is fully operational with working Kafka connectivity.
- [x] **Sonar Refinement**: Replaced `Stream.collect(Collectors.toList())` with `Stream.toList()` across the codebase for improved readability and unmodifiable lists, satisfying SonarQube rules.
- [x] **Documentation**: Created `KNOWLEDGE-BASE.md`, updated `README.md`, `CONTRIBUTING.md`, `LICENSE`, `CONFIGURATIONS.md`, and merged/restored `code/CHANGELOG.md` to preserve historical refactoring tasks. Added "Known Pitfalls for Agents" to `AGENTS.md`.
- [x] **GitHub Actions Refinement**: Updated `ci.yml` to include Kafka and Schema Registry service containers. Added a secondary job to lint OpenAPI and AsyncAPI contracts using Spectral. Integrated SonarCloud analysis into the build pipeline.
- [x] **Flyway & Schema Registry**: Added missing Flyway dependencies to fix Hibernate validation errors. Added Confluent Schema Registry to the `docker compose` stack and configured the application to use it.
- [x] **Avro Support**: Added Confluent Maven repository, Avro dependencies, and `avro-maven-plugin` for messaging contract generation.
- [x] **Test Fixes**: Resolved `IllegalStateException` and `Cannot load driver class: org.h2.Driver` in `SkeletoniApplicationTests` by adding H2 to the `boot` module and providing mandatory configuration placeholders in `application-test.yml`. Implemented Flyway 9.22.3 fallback to ensure definitive build stability across Maven mirrors.

## Tasks Pending

- [ ] **Grafana Dashboard**: Create a pre-configured JSON dashboard for the infrastructure.
- [ ] **Integration Tests**: Implement sample integration tests using Testcontainers in `infrastructure`.
- [ ] **Observability config**: Add custom metrics and tracing configuration in `observability` module.

## Decisions & Notes

- **SonarCloud**: Excluded local configuration files from analysis.
- **Secret Management**: Removed all clear-text passwords from the main `application.yml`. Developers must now copy `application-local.yml.example` to `application-local.yml` (git-ignored) or provide environment variables.
- **Testing**: Added H2 to the `infrastructure` module for the basic context loads test in `boot`. Actual integration tests will continue to use Testcontainers.
- **Flyway**: Added an initial migration script to ensure the example entity is usable immediately upon starting the infrastructure and application.
- **Root POM**: Added a root `pom.xml` to aggregate the `code` module, which was missing.
- **Contract/Domain separation**: Removed `domain` dependency from `contract` to maintain strict module boundaries.
- **Protobuf/gRPC**: Added `protobuf-maven-plugin` and necessary dependencies to support gRPC contracts.
- **MapStruct mapping**: Implemented a sample mapper in `infrastructure` to handle conversion between domain models and JPA entities.
- **Local Dev**: Use `docker compose up -d` to start the full infrastructure.
- **Application Port**: Application runs on port 8080. Actuator endpoints enabled.

## Files Created or Modified

| File | Action | Reason |
|---|---|---|
| `ANTIGRAVITY-TASKS.md` | Modified | Action plan and task logging |
| `code/pom.xml` | Modified | Added Sonar exclusions |
| `KNOWLEDGE-BASE.md` | Modified | Updated known errors section |
| `code/boot/src/main/resources/application.yml` | Modified | Removed passwords from the main configuration |
| `code/boot/src/main/resources/application-local.yml` | Created | Local dev defaults (git-ignored) |
| `code/boot/src/main/resources/application-local.yml.example` | Created | Template for local dev defaults |
| `code/boot/src/test/java/.../SkeletoniApplicationTests.java` | Modified | Added assertions and test profile |
| `code/boot/src/test/resources/application-test.yml` | Created | Test profile configuration |
| `code/infrastructure/pom.xml` | Modified | Added H2 for tests |
| `README.md` | Modified | Updated module structure documentation |
| `.gitignore` | Modified | Enhanced for modern IDEs and OS files |
| `LICENSE` | Created | Project license (MIT) |
| `CONTRIBUTING.md` | Created | Contribution guidelines |
| `code/infrastructure/src/main/resources/db/migration/V1__init_examples.sql` | Created | Initial DB migration |
| `pom.xml` | Created | Root aggregation POM |
| `code/pom.xml` | Modified | Added plugins and properties |
| `compose.yml` | Created | Local dev infrastructure |
| `infra/prometheus/prometheus.yml` | Created | Prometheus configuration |
| `Dockerfile` | Created | Multi-stage build |
| `.github/workflows/ci.yml` | Created | CI pipeline |
| `.github/workflows/release.yml` | Created | Release pipeline |
| `code/contract/src/main/resources/openapi.yml` | Created | REST API contract |
| `code/contract/src/main/resources/asyncapi.yml` | Created | Messaging contract |
| `code/contract/src/main/resources/proto/example.proto` | Created | gRPC contract |
| `code/domain/src/main/java/.../Example.java` | Created | Domain model |
| `code/application/src/main/java/.../ExampleService.java` | Created | Application service |
| `code/infrastructure/src/main/java/.../ExamplePostgresAdapter.java` | Created | Persistence adapter |
| `code/boot/src/main/resources/logback-spring.xml` | Created | Structured logging config |
