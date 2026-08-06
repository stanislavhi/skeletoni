# Epic SKL-OBS — Observability & Resilience

Making the `logging`, `observability` and `resilience` modules actually do what they claim.
Board: [../backlog.md](../backlog.md).

---

## SKL-11 — Add `%X{correlationId}` to the console log pattern

**Type** Bug · **Priority** P1 · **Estimate** XS · **Status** Todo

`CorrelationIdFilter` puts `correlationId` into MDC and echoes it on the response header, but
`application.yml` sets a console pattern that never reads it:

```yaml
logging.pattern.console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

The entire point of the `logging` module — correlating log lines for one request — is therefore
invisible in the only appender that is actually attached. One-line fix:

```yaml
logging.pattern.console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] [%X{correlationId:-}] %-5level %logger{36} - %msg%n"
```

`:-` supplies an empty default so non-request threads (startup, scheduled work) do not print
`correlationId_IS_UNDEFINED`.

**Acceptance**
- [ ] Correlation ID appears in console output for every request-scoped log line
- [ ] Startup logs render cleanly with no placeholder noise
- [ ] Two concurrent requests show distinct IDs

**Lode to update** [../../observability/correlation-id.md](../../observability/correlation-id.md) §"Known limitations"

---

## SKL-12 — Attach or remove the Logstash appender

**Type** Task · **Priority** P2 · **Estimate** S · **Status** Todo

`logback-spring.xml` defines a `LogstashTcpSocketAppender` to `localhost:5044` with a full JSON
composite encoder including the `mdc` provider — exactly what would surface correlation IDs in
structured logs. The root logger references only `CONSOLE`, so it is never used, and there is no
Logstash service in `compose.yml` to receive it.

Naively attaching it produces connection-retry noise on every developer machine. Either:

- **(a)** Add a log sink to `compose.yml` and gate the appender by profile:

  ```xml
  <springProfile name="local">
    <root level="INFO"><appender-ref ref="CONSOLE"/></root>
  </springProfile>
  <springProfile name="!local">
    <root level="INFO"><appender-ref ref="CONSOLE"/><appender-ref ref="LOGSTASH"/></root>
  </springProfile>
  ```

- **(b)** Delete the appender and the `logstash-logback-encoder` dependency.

**Recommendation: (a) with the destination externalised** to `${LOGSTASH_DESTINATION:-}` — a
skeleton should show how structured shipping is wired, but must not require a log pipeline to run
locally.

**Acceptance**
- [ ] No dead appender config; either wired behind a profile or removed with its dependency
- [ ] `docker compose up` + local run produces zero connection-retry noise
- [ ] If kept: destination is env-configurable, and the `mdc` provider is verified to emit `correlationId`

---

## SKL-13 — Wire Resilience4j to a real remote call site

**Type** Story · **Priority** P2 · **Estimate** S · **Status** Todo

`resilience4j.circuitbreaker.instances.exampleService` and the matching retry instance are fully
configured (10s time-based window, 50% threshold, 5s open duration, 3 half-open trials; retry 3×
with exponential backoff ×2). `ResilienceConfig` registers event listeners for state transitions.
**Nothing is annotated**, so registries stay empty and the listeners never fire.

Apply it to the Kafka publish from SKL-6 or the gRPC path from SKL-8 — a genuinely remote call.
**Do not wrap `ExamplePostgresAdapter`**: connection-pool exhaustion is HikariCP's job, and a
breaker in front of the primary datastore converts a slow database into a hard outage.

```java
@CircuitBreaker(name = "exampleService", fallbackMethod = "publishFallback")
@Retry(name = "exampleService")
public void publish(ExampleCreatedEvent event) { ... }

private void publishFallback(ExampleCreatedEvent event, Throwable t) {
  log.error("Publish failed after retries for {}", event.id(), t);
}
```

Verify aspect ordering — retry wrapping breaker versus breaker wrapping retry produces very
different failure counting, and the default is easy to assume wrongly.

**Acceptance**
- [ ] At least one genuinely remote call is decorated, with a fallback
- [ ] `ResilienceConfig` logs registration and transitions at runtime (proof it is live)
- [ ] Aspect order verified and documented
- [ ] Test forces the breaker open and asserts the fallback
- [ ] Breaker state visible at `/actuator/health` (`registerHealthIndicator: true` already set)

**Blocked by** SKL-6
**Blocks** SKL-15

---

## SKL-14 — MDC bridges for async, scheduled and Kafka contexts

**Type** Story · **Priority** P3 · **Estimate** M · **Status** Todo

`CorrelationIdFilter` is a servlet filter, so correlation exists only on HTTP request threads.
Every other execution context logs with no correlation ID at all:

| Context | Bridge needed |
|---|---|
| `@Async` / executor threads | `TaskDecorator` copying the MDC map |
| `@Scheduled` | generate a fresh ID per execution |
| Kafka consumers | `RecordInterceptor` reading a correlation header |
| gRPC | `ServerInterceptor` reading metadata |
| Outbound HTTP | `RestClient`/`WebClient` interceptor writing `X-Correlation-Id` |

The outbound case is the most consequential: correlation currently **stops at this service's
boundary**, so the ID is useless for anything genuinely distributed.

**Acceptance**
- [ ] `TaskDecorator` registered; async logs carry the originating request's ID
- [ ] Kafka consumer propagates a correlation header into MDC (pairs with SKL-6/SKL-7)
- [ ] Outbound client interceptor writes the header
- [ ] MDC always cleared in a `finally` in every bridge — thread reuse leaks IDs otherwise

**Blocks** SKL-16

---

## SKL-15 — Real checks in `ApplicationHealthIndicator`

**Type** Task · **Priority** P3 · **Estimate** S · **Status** Todo

The indicator returns a constant `UP` with two static details. It contributes to aggregate health
while being structurally incapable of reporting a problem — worse than no indicator, because it
implies a check that does not exist.

Replace with genuine readiness signals (broker reachability, downstream availability), or delete it
and rely on Boot's built-in datastore indicators. Also flip
`management.endpoint.health.show-details` off for non-local profiles — see SKL-22.

**Acceptance**
- [ ] Indicator can return `DOWN` under a reproducible condition, proven by test
- [ ] Liveness and readiness are distinguished (`livenessState` / `readinessState` groups)
- [ ] Or: indicator removed, with the rationale recorded

**Blocked by** SKL-13

---

## SKL-16 — Distributed tracing (Micrometer Tracing + OTel)

**Type** Story · **Priority** P3 · **Estimate** L · **Status** Todo

There is no tracing dependency anywhere. `X-Correlation-Id` is a hand-rolled substitute that does
not propagate context, has no span model, and no backend. Adding `micrometer-tracing-bridge-otel`
plus an OTLP exporter gives real spans across HTTP, Kafka and gRPC — and makes SKL-14's manual
bridges partly redundant, since Micrometer's context propagation handles them.

Decide the relationship explicitly: keep `X-Correlation-Id` as a human-facing request handle and
let `traceId`/`spanId` handle machine correlation, or retire the filter. Do not maintain two
uncoordinated schemes.

**Acceptance**
- [ ] Tracing bridge + OTLP exporter configured, sampling externalised
- [ ] `traceId`/`spanId` in the log pattern alongside or replacing `correlationId`
- [ ] Trace context propagates across HTTP → Kafka → gRPC
- [ ] Collector added to `compose.yml`; a trace is visible end to end
- [ ] Relationship to `CorrelationIdFilter` decided and documented

**Blocked by** SKL-14

---

## SKL-17 — Business metrics and a dashboard beyond JVM

**Type** Story · **Priority** P3 · **Estimate** M · **Status** Todo

Micrometer currently reports only JVM, HTTP and datasource built-ins; `skeletoni-jvm.json` is a JVM
dashboard. No use case is instrumented, so nothing shows what the service *does*.

Add counters/timers on use cases (`examples.created`, publish latency, breaker transitions) and a
second provisioned dashboard. `MetricsConfig` already applies `application` and `environment`
common tags, so new meters inherit them.

**Acceptance**
- [ ] At least one counter and one timer on application use cases
- [ ] `infra/grafana/dashboards/skeletoni-application.json` provisioned alongside the JVM dashboard
- [ ] Metrics visible at `/actuator/prometheus` with the common tags applied
- [ ] Dashboard edited as JSON in the repo, never through the Grafana UI

---

Related: [../known-gaps.md](../known-gaps.md) entries 5, 6; [../../resilience/summary.md](../../resilience/summary.md)
