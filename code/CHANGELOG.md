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
