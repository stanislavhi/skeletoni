# Epic SKL-MSG — Events, Messaging & Protocols

Turning declared contracts into working code, and removing what will not be implemented.
Board: [../backlog.md](../backlog.md).

---

## SKL-6 — Publish `ExampleCreatedEvent` via out-port + Kafka adapter

**Type** Story · **Priority** P1 · **Estimate** M · **Status** Todo

The highest-value ticket in the backlog. `ExampleCreatedEvent` exists in `domain`, its payload is
mirrored in `asyncapi.yml`, and it is never constructed. Publishing it activates three inert
subsystems at once (events, Kafka, resilience) and forces the transaction-boundary decision.

Shape, following the established port/adapter pattern:

```java
// application/port/out/ExampleEventPublisher.java
public interface ExampleEventPublisher {
  void publish(ExampleCreatedEvent event);
}
```

```java
// infrastructure/adapter/messaging/ExampleKafkaProducer.java
@Component
@RequiredArgsConstructor
public class ExampleKafkaProducer implements ExampleEventPublisher {
  private final KafkaTemplate<String, ExampleCreatedEvent> kafkaTemplate;

  @Override
  public void publish(ExampleCreatedEvent event) {
    kafkaTemplate.send("example-topic", event.id().toString(), event);
  }
}
```

Partition key is the aggregate id, so all events for one `Example` stay ordered.

The domain currently has no way to emit an event — `Example.create` returns only the aggregate.
Either `ExampleService` constructs the event, or `Example` exposes recorded events. For a skeleton,
constructing it in the service is the simpler, more legible choice; document whichever you pick.

**Acceptance**
- [ ] `ExampleEventPublisher` out-port in `application`, adapter in `infrastructure`
- [ ] `ExampleService.createExample` publishes after a successful save
- [ ] Published payload matches the `ExampleCreatedPayload` schema in `asyncapi.yml` exactly
- [ ] Testcontainers Kafka IT asserts the event lands on `example-topic`
- [ ] At-least-once semantics documented — do not imply exactly-once

**Blocked by** SKL-9 (serialization format must be settled first)
**Blocks** SKL-4, SKL-7, SKL-13

**Lode to update** [../../architecture/request-flow.md](../../architecture/request-flow.md),
[../../application/summary.md](../../application/summary.md),
[../../contract/messaging-and-grpc.md](../../contract/messaging-and-grpc.md)

---

## SKL-7 — Implement `exampleConsumer` for `example-topic`

**Type** Story · **Priority** P2 · **Estimate** S · **Status** Todo

Restores the binding SKL-3 removed, this time with a bean behind it.

```java
@Bean
public Consumer<ExampleCreatedEvent> exampleConsumer() {
  return event -> log.info("Received ExampleCreated: {}", event.id());
}
```

For a skeleton the consumer should demonstrate the wiring, not invent business logic. What it must
demonstrate properly is failure handling: consumer group `skeletoni-group`,
`auto-offset-reset: earliest`, and `ErrorHandlingDeserializer` are already configured — a poison
message currently has nowhere to go. A DLQ binding is the missing half.

**Acceptance**
- [ ] `exampleConsumer` bean exists and the `spring.cloud.stream` block is restored
- [ ] DLQ configured for deserialization and processing failures
- [ ] Consumer is idempotent, or its non-idempotence is documented (pairs with SKL-6's at-least-once)
- [ ] Testcontainers IT: produce to `example-topic`, assert consumption
- [ ] Poison-message test asserts the DLQ path

**Blocked by** SKL-3, SKL-6

---

## SKL-8 — Implement `ExampleGrpcService`

**Type** Story · **Priority** P2 · **Estimate** M · **Status** Todo

`example.proto` defines `ExampleService.GetExample`, codegen runs, the
`grpc-server-spring-boot-starter` is on the classpath, and `grpc.server.port: 9091` is configured.
Nothing serves it — the port is open in name only. Implementing it proves the second protocol path
reaches the same application core, which is the whole point of ports and adapters.

Name it **`ExampleGrpcService`**, not `ExampleService` — the proto service and the application
service already collide by name.

```java
@GrpcService
@RequiredArgsConstructor
public class ExampleGrpcService extends ExampleServiceGrpc.ExampleServiceImplBase {
  private final ExampleUseCase exampleUseCase;   // same core as REST
  // GetExample → needs a findById use case; only createExample/listExamples exist today
}
```

Note the gap: `ExampleUseCase` has no `findById`, though `ExampleRepository` does. Add the in-port
method as part of this ticket rather than reaching past the use case into the repository.

`infrastructure` reaches `contract` only transitively — declare the dependency explicitly when the
generated stubs are imported.

**Acceptance**
- [ ] `ExampleGrpcService` in `infrastructure`, delegating to `ExampleUseCase`
- [ ] `ExampleUseCase.findById` added; unknown id maps to `Status.NOT_FOUND`
- [ ] Explicit `contract` dependency in `code/infrastructure/pom.xml`
- [ ] Integration test against the in-process gRPC server
- [ ] Correlation ID propagated via a `ServerInterceptor` (or the gap noted for SKL-14)

---

## SKL-9 — Decide JSON vs Avro for Kafka payloads

**Type** Task · **Priority** P2 · **Estimate** S · **Status** Todo

The project is tooled for Avro and configured for JSON:

| Signal | Points to |
|---|---|
| `avro-maven-plugin`, `avro:1.12.0`, `kafka-avro-serializer:7.8.0` | Avro |
| Schema Registry in `compose.yml` and CI, `schema.registry.url` configured | Avro |
| `contract/src/main/resources/avro/` is **empty** | nothing |
| `JsonSerializer` / `ErrorHandlingDeserializer` in `application.yml` | JSON |

This must be settled **before** SKL-6, because switching afterwards means rewriting the producer,
the consumer and their tests.

Trade-off: Avro buys schema evolution enforcement and registry-backed compatibility checks — the
thing a Schema Registry exists for, and the thing a skeleton should demonstrate. It costs a
generate-sources step and makes the contract module's build order matter. JSON buys simplicity and
nothing else. Given AsyncAPI already declares the payload schema, Avro is the coherent choice; JSON
leaves the registry as decoration.

**Recommendation:** Avro. If rejected, remove the Avro plugin, the Confluent dependency and the
Schema Registry service so the build stops advertising it.

**Acceptance**
- [ ] Decision recorded here with rationale
- [ ] Either `ExampleCreated.avsc` exists and serializers switch to Avro, or all Avro tooling is removed
- [ ] `asyncapi.yml` and the chosen schema agree
- [ ] [../../contract/messaging-and-grpc.md](../../contract/messaging-and-grpc.md) updated

**Blocks** SKL-6

---

## SKL-10 — Prune or gate unimplemented starters

**Type** Task · **Priority** P1 · **Estimate** M · **Status** Todo

`infrastructure` declares starters for MongoDB, Couchbase and RabbitMQ with zero code behind them.
This is not free — their auto-configurations stay active, which is precisely why:

- `application.yml` demands `SPRING_DATA_MONGODB_URI` and `SPRING_DATA_COUCHBASE_*` with no defaults
- `application-test.yml` excludes four auto-configurations **and** supplies dummy placeholders
- `compose.yml` runs Couchbase, which is heavy and requires manual UI setup to be useful
- every new `@SpringBootTest` inherits that ceremony

The skeleton's value is showing one thing done properly, not eight things declared. Options:

- **(a)** Remove Couchbase and RabbitMQ entirely; keep Postgres + Kafka as the demonstrated path.
- **(b)** Move them behind Maven profiles (`-Pcouchbase`) so they are opt-in.
- **(c)** Implement an adapter for each — large, and duplicates what Postgres already shows.

**Recommendation: (a).** Document in `README.md` how to add them back. A skeleton that boots
cleanly with two backing services beats one that demands credentials for five.

**Acceptance**
- [ ] Decision recorded; unused starters removed from `code/infrastructure/pom.xml`
- [ ] Corresponding `application.yml` blocks and `application-test.yml` exclusions/dummies removed
- [ ] `compose.yml` trimmed to services that are actually exercised
- [ ] Full test suite passes with fewer exclusions than before
- [ ] `README.md` documents re-adding them

**Blocks** SKL-21

---

Related: [../known-gaps.md](../known-gaps.md) — "declared but unimplemented" table, entries 1, 7
