# Request Flow — `POST /api/v1/examples`

The single fully-wired vertical slice. Every new feature should reproduce this shape.

## Sequence

```mermaid
sequenceDiagram
  participant C as Client
  participant F as CorrelationIdFilter (logging)
  participant K as ExampleController (contract)
  participant D as ExampleControllerDelegateImpl (application)
  participant S as ExampleService (application)
  participant A as ExamplePostgresAdapter (infrastructure)
  participant M as ExampleMapper (MapStruct)
  participant R as ExampleJpaRepository
  participant P as PostgreSQL

  C->>F: POST /api/v1/examples {name}
  F->>F: MDC.put(correlationId), set X-Correlation-Id header
  F->>K: filterChain.doFilter
  K->>D: createExample(CreateExampleRequest)
  D->>D: new CreateExampleCommand(request.getName())
  D->>S: createExample(command)
  S->>S: Example.create(name) → ExampleId.generate(), createdAt=now
  S->>A: save(Example)
  A->>M: toEntity(Example)
  M-->>A: ExampleJpaEntity
  A->>R: save(entity)
  R->>P: INSERT INTO examples
  S-->>D: Example
  D->>D: mapToResponse(Example) → ExampleResponse
  D-->>K: ExampleResponse
  K-->>C: 201 Created + X-Correlation-Id
  F->>F: MDC.remove(correlationId)
```

## Type transitions

| Boundary | In | Out |
|---|---|---|
| HTTP → controller | JSON body | `CreateExampleRequest` (contract DTO) |
| controller → delegate impl | `CreateExampleRequest` | — |
| delegate impl → service | `CreateExampleCommand` (application) | `Example` (domain) |
| service → adapter | `Example` | — |
| adapter → JPA | `ExampleJpaEntity` (infrastructure) | — |
| delegate impl → controller | `Example` | `ExampleResponse` (contract DTO) |

Domain types never cross into `contract`. DTO types never cross into `domain`. The delegate impl in
`application` is the only place both are visible.

## Read path — `GET /api/v1/examples`

Identical shape via `ExampleUseCase.listExamples()` → `ExampleRepository.findAll()` →
`repository.findAll().stream().map(mapper::toDomain).toList()`.

## What is deliberately absent

- `ExampleCreatedEvent` is **not** published after `save`. The natural extension point is an
  `ExampleEventPublisher` out-port in `application` implemented by an
  `ExampleKafkaProducer` adapter in `infrastructure`, emitting to `example-topic` per
  [../contract/messaging-and-grpc.md](../contract/messaging-and-grpc.md).
- No transaction boundary is declared. `ExampleService.createExample` is not `@Transactional`;
  the single `save` is atomic by accident, not by design. Adding a second write makes this a bug.
- No validation. `CreateExampleRequest.name` is `required` in OpenAPI but has no `@NotBlank`
  and the controller has no `@Valid`.

Related: [summary.md](summary.md), [../contract/rest-delegate-pattern.md](../contract/rest-delegate-pattern.md)
