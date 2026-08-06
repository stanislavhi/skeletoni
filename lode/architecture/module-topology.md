# Module Topology

The Maven reactor lives at `code/pom.xml` (packaging `pom`, parent
`spring-boot-starter-parent:4.0.3`). The repo-root `pom.xml` only declares `<module>code</module>`.

Reactor order as declared: `contract`, `application`, `domain`, `infrastructure`, `logging`,
`observability`, `boot`, `resilience`. Maven reorders by dependency graph, so declaration order is
cosmetic.

## Actual dependency edges

```mermaid
graph BT
  domain["domain<br/>(no deps)"]
  contract["contract<br/>(spring-web, springdoc, grpc, protobuf)"]
  logging["logging<br/>(spring-web, logstash-encoder)"]
  observability["observability<br/>(actuator, micrometer-prometheus)"]
  resilience["resilience<br/>(spring-cloud-circuitbreaker-resilience4j)"]
  application["application"]
  infrastructure["infrastructure"]
  boot["boot"]

  application --> contract
  application --> domain
  infrastructure --> application
  infrastructure --> domain
  infrastructure --> logging
  infrastructure --> observability
  boot --> application
  boot --> infrastructure
  boot --> logging
  boot --> observability
  boot --> resilience
```

Notes on the real graph, which differs slightly from the prose in `AGENTS.MD`:

- `infrastructure` does **not** declare `contract` directly; it reaches it transitively via
  `application`. Do not rely on this — declare it explicitly if you add a gRPC service impl.
- `boot` does **not** declare `contract` or `domain` directly; both arrive transitively.
- `resilience` is reachable only from `boot`. Nothing else depends on it, which is why the
  circuit breaker configuration is currently inert (see [../resilience/summary.md](../resilience/summary.md)).

## Where a new class goes

```mermaid
flowchart TD
  A{"What is it?"} --> B["Business rule, aggregate, or domain event"] --> domain
  A --> C["Orchestrates domain + ports"] --> application
  A --> D["DTO, proto, event schema, REST controller"] --> contract
  A --> E["Talks to DB, broker, HTTP client, external system"] --> infrastructure
  A --> F["Log formatting, MDC, correlation ID"] --> logging
  A --> G["Metrics, tracing, health"] --> observability
  A --> H["Circuit breaker, retry, bulkhead config"] --> resilience
```

## Keeping the graph legal

- Adding a dependency edge means editing a `pom.xml`. Treat that edit as an architectural decision
  and record it here.
- A "circular dependency detected" Maven failure means class placement is wrong, not that Maven is
  wrong. Move the class; do not add the reverse edge.
- Test-scope dependencies are **not** transitive. `boot` needs its own `h2` test dependency even
  though `infrastructure` already has one — this bit the project once already.

Related: [summary.md](summary.md), [../build/maven-conventions.md](../build/maven-conventions.md)
