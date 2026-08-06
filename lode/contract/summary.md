# Contract Module — Summary

`code/contract` owns every externally visible interface definition. It is a leaf module: it depends
on no other internal module, so nothing in it may reference domain or application types.

## Contents

```
code/contract/src/main/
├── java/turbo/diesel/skeletoni/contract/
│   ├── dto/CreateExampleRequest.java     @Data, mutable, single field `name`
│   ├── dto/ExampleResponse.java          @Data @Builder — id (UUID), name, createdAt (OffsetDateTime)
│   ├── web/ExampleController.java        @RestController /api/v1/examples
│   └── web/ExampleControllerDelegate.java  interface implemented in `application`
└── resources/
    ├── openapi.yml       OpenAPI 3.1 — the two REST operations
    ├── asyncapi.yml      AsyncAPI 3.0 — `example-topic` / ExampleCreated
    ├── proto/example.proto  proto3 — ExampleService.GetExample
    └── avro/             EMPTY — plugin configured, no schemas yet
```

## Build behaviour

- `protobuf-maven-plugin` (config inherited from the parent `pluginManagement`) runs `compile` and
  `compile-custom`, generating protobuf messages **and** gRPC stubs into
  `turbo.diesel.skeletoni.contract.grpc`. Requires `os-maven-plugin` (declared as a build extension
  in the parent POM) to resolve `${os.detected.classifier}`.
- `avro-maven-plugin` binds `schema` to `generate-sources` reading `src/main/resources/avro/`.
  The directory holds only a `.gitkeep` — but that file is **load-bearing**. The plugin fails the
  build outright when neither `src/main/resources/avro` nor `src/test/avro` is a directory, and git
  does not track empty directories. Without the placeholder the module builds on a developer machine
  and dies in CI at `generate-sources`, taking the whole reactor with it (`SKL-33`).
- If your IDE reports `package turbo.diesel.skeletoni.contract.grpc does not exist`, run
  `mvn generate-sources -pl code/contract` and reload the Maven project.

## Dependencies of note

`spring-boot-starter-web`, `springdoc-openapi-starter-webmvc-ui:2.8.5`, `protobuf-java:4.29.3`,
`grpc-stub:1.70.0`, `grpc-protobuf:1.70.0`, `jakarta.annotation-api:3.0.0` (provided — needed by
generated gRPC code).

## Design tension to be aware of

`contract` holding a live `@RestController` means the "pure schema" module also carries Spring MVC
runtime concerns. The delegate interface is the mitigation — see
[rest-delegate-pattern.md](rest-delegate-pattern.md) for the rationale and the trade-off.

## Detail lodes

- [rest-delegate-pattern.md](rest-delegate-pattern.md)
- [messaging-and-grpc.md](messaging-and-grpc.md)
