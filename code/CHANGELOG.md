# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **SKL-1**: Initial project scaffolding and infrastructure setup.
    - **Architecture & Structure**:
        - Refactored monolithic structure into a multi-module Maven project with `code/` container.
        - Created sub-modules: `contract`, `application`, `domain`, `infrastructure`, `logging`, `observability`, `boot`, `resilience`.
        - Migrated `SkeletoniApplication.java` and `application.yml` to `code/boot`.
        - Centralized dependency management and build configuration in `code/pom.xml`.
    - **Infrastructure & Technical Refinement**:
        - Pure "Hexagonal" reference implementation for an `Example` entity.
        - Full local development stack in `compose.yml` (Postgres, Mongo, Couchbase, Kafka, RabbitMQ, Confluent Schema Registry, Prometheus, Grafana).
        - Multi-stage `Dockerfile` and GitHub Actions workflows (`ci.yml`, `release.yml`).
        - API Contracts: OpenAPI 3 (REST), AsyncAPI 3 (Messaging), Protobuf (gRPC).
        - Avro support with Confluent Schema Registry integration.
        - Flyway database migrations for PostgreSQL and H2.
        - Secure configuration using environment variable placeholders and `application-local.yml`.
        - Configured MapStruct with Lombok binding.
        - Added Testcontainers to `infrastructure` for integration testing.
        - Added SpringDoc OpenAPI to `contract` for API documentation.
        - Added Actuator and Prometheus to `observability` for metrics.
        - Added Logstash encoder to `logging` for structured logging.
        - Comprehensive `KNOWLEDGE-BASE.md` and `CONTRIBUTING.md`.
        - Unit tests for application context loading using H2 test profile.
        - Applied SonarQube best practices by replacing `Stream.collect(Collectors.toList())` with `Stream.toList()`.

### Changed

- **SKL-22**: Moved development-only settings out of base configuration. `spring.jpa.show-sql` and
  `management.endpoint.health.show-details: always` now live in `application-local.yml`; the base
  `application.yml` uses `show-details: when-authorized`.
- **SKL-23**: Dockerfile runtime stage copies `boot-*.jar` instead of a hardcoded version, so
  version bumps no longer break the image build.

### Removed

- **SKL-2**: Untracked `code/boot/src/main/resources/application-local.yml` from the git index. The
  file remains on disk and is now genuinely covered by `.gitignore`; `application-local.yml.example`
  stays tracked as the template.
- **SKL-25**: Removed the unused `app.scheduler.example-cron` property — no `@Scheduled` bean or
  `@EnableScheduling` consumed it.

### Fixed

- **SKL-11**: Correlation ID now appears in console log output. `logging.pattern.console` includes
  `[%X{correlationId:-}]`, so request-scoped log lines are correlatable.

- **SKL-31**: `ExamplePostgresAdapterIT` now compiles. Spring Boot 4 split the sliced-test
  annotations into per-technology starters and relocated their packages; added test-scoped
  `spring-boot-starter-data-jpa-test` (`@DataJpaTest` →
  `org.springframework.boot.data.jpa.test.autoconfigure`) and `spring-boot-starter-jdbc-test`
  (`@AutoConfigureTestDatabase` → `org.springframework.boot.jdbc.test.autoconfigure`) to the
  `infrastructure` module. `mvn verify` is green across all nine modules.

### Known issues

- **SKL-32**: Integration tests do not execute. `maven-failsafe-plugin` is not configured and
  surefire's default includes do not match the `*IT` suffix, so `ExamplePostgresAdapterIT` compiles
  and is silently skipped — `mvn verify` reports `Tests run: 1` while passing.
