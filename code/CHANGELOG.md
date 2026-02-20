# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **SKL-1**: Initial project structure refactoring to multi-module Maven project.
    - Created `code` module as the parent for all source modules.
    - Created sub-modules: `contract`, `application`, `domain`, `infrastructure`, `logging`, `observability`, `boot`, `resilience`.
    - Migrated `SkeletoniApplication.java` and `application.yml` to `code/boot`.
    - Configured `pom.xml` files for all modules with appropriate dependencies.
    - Centralized dependency management and build configuration in `code/pom.xml`.
