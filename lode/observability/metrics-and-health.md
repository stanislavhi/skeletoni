# Metrics & Health Stack

## Module wiring

`code/observability` depends only on `spring-boot-starter-actuator` and
`io.micrometer:micrometer-registry-prometheus`. It is a leaf module — `infrastructure` and `boot`
both pull it in, so the metrics beans are always present.

## Exposure

```yaml
# base application.yml
management:
  endpoints:
    web.exposure.include: health,info,prometheus,metrics
  endpoint:
    health.show-details: when-authorized
  metrics:
    tags.application: ${spring.application.name}

# application-local.yml overrides for developer convenience
management:
  endpoint:
    health.show-details: always
```

`show-details` is `when-authorized` by default and only `always` on the `local` profile —
unauthenticated detail would expose datastore names, versions and (once `SKL-13` lands) circuit
breaker state to anyone who can reach `/actuator/health`.

Two independent mechanisms add the same `application` tag: the `management.metrics.tags` property
and `MetricsConfig.commonTags`. The latter also adds `environment`. Redundant but harmless.

## Scrape path

`infra/prometheus/prometheus.yml` targets the app's `/actuator/prometheus`. Prometheus runs at
`:9090`; Grafana at `:3000` (`admin`/`admin`) with provisioning mounted read-only from the repo:

```yaml
grafana:
  volumes:
    - ./infra/grafana/provisioning:/etc/grafana/provisioning
    - ./infra/grafana/dashboards:/var/lib/grafana/dashboards
  depends_on: [prometheus]
```

Provisioned files:

- `infra/grafana/provisioning/datasources/datasources.yml` — Prometheus datasource
- `infra/grafana/provisioning/dashboards/dashboards.yml` — dashboard provider
- `infra/grafana/dashboards/skeletoni-jvm.json` — JVM dashboard

Dashboards are code. Edit the JSON in the repo, not the Grafana UI, or the change is lost on the
next `docker compose down -v`.

## Spring Boot 4 import paths

Boot 4 relocated actuator/metrics types. These are the ones this project uses — Boot 3 snippets
found online will not compile:

| Type | Boot 4 package |
|---|---|
| `MeterRegistryCustomizer` | `org.springframework.boot.micrometer.metrics.autoconfigure` |
| `Health`, `HealthIndicator` | `org.springframework.boot.health.contributor` |

## Health indicator

`ApplicationHealthIndicator` reports a constant `UP` with two details. It contributes to the
aggregate `/actuator/health` status, meaning it currently cannot ever fail the readiness probe.
Real checks belong here:

```java
@Override
public Health health() {
  // e.g. probe broker/downstream, return Health.down().withException(e).build() on failure
  return Health.up().withDetail("service", "skeletoni").withDetail("status", "operational").build();
}
```

Note `resilience4j.circuitbreaker.instances.exampleService.registerHealthIndicator: true` — once a
circuit breaker is actually used, its state also surfaces under `/actuator/health`.

## Gaps

- No custom business metrics (`@Timed`, `Counter`) anywhere. Micrometer only reports JVM, HTTP and
  datasource built-ins.
- No distributed tracing dependency at all.
- Grafana admin password is hardcoded to `admin` in `compose.yml` — local-only, but never carry
  that file into a shared environment.

Related: [summary.md](summary.md), [../resilience/summary.md](../resilience/summary.md)
