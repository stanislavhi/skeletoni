# Maven Conventions & Gotchas

## Annotation processing (parent POM, applies to every module)

```xml
<plugin>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path>org.mapstruct:mapstruct-processor:1.6.3</path>
      <path>org.projectlombok:lombok:1.18.36</path>
      <path>org.projectlombok:lombok-mapstruct-binding:0.2.0</path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

**Order matters and the binding is not optional.** Without `lombok-mapstruct-binding`, MapStruct
runs before Lombok has generated accessors and emits
`Target bean does not have a default constructor` or silently unmapped fields.

Lombok is declared once in the parent `<dependencies>` as `<optional>true</optional>`, so every
module has it without redeclaring.

## Plugin management (parent, opt-in per module)

- `spring-boot-maven-plugin` — Lombok excluded from the repackaged jar. Activated only in `boot`.
- `protobuf-maven-plugin` 0.6.1 — `protoc:4.29.3` + `protoc-gen-grpc-java:1.70.0`, goals `compile`
  and `compile-custom`. Activated only in `contract`.
- `os-maven-plugin` 1.7.1 is a build **extension** (not a plugin) in the parent — it resolves
  `${os.detected.classifier}` for the protoc binaries. Removing it breaks contract codegen on
  every platform.

## Dependency management

`dependencyManagement` imports `spring-cloud-dependencies` and `testcontainers-bom`, and pins
`mapstruct` and `flyway-core`. Anything else inherits from `spring-boot-starter-parent:4.0.3`.

Rule: **prefer versions managed by the Boot parent BOM.** Explicit versions in this project exist
only where the BOM does not manage the artifact (springdoc, grpc, protobuf, avro, confluent,
logstash-encoder, grpc-server-spring-boot-starter).

## Gotchas that have already cost time

1. **Test-scope dependencies are not transitive.** `boot` declares its own `h2` test dependency
   even though `infrastructure` has one; the `contextLoads` test needs it directly.
2. **`.Native` gRPC starter variants do not resolve.** Use
   `net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE`.
3. **Flyway 10 modular artifacts resolve badly through the Confluent mirror.** Pinned to 9.22.3.
4. **`kafka-avro-serializer` drags a conflicting `kafka-clients`.** Excluded in
   `code/infrastructure/pom.xml` so Boot's managed version wins.
5. **`jakarta.annotation-api` is `provided` in `contract`** — generated gRPC code references
   `@Generated`; without it, compilation of generated sources fails.
6. **MapStruct `expression` mappings need `imports` on `@Mapper`.** See
   [../infrastructure/persistence-adapter.md](../infrastructure/persistence-adapter.md).
7. **IntelliJ does not auto-run `generate-sources`.** After changing a `.proto`, reload the Maven
   project or `mvn generate-sources -pl code/contract`, else
   `package turbo.diesel.skeletoni.contract.grpc does not exist`.
8. **Spring Boot 4 relocated types that Boot 3 tutorials still reference.** Copy-pasted snippets
   will not compile. Known relocations in this project:

   | Type | Boot 4 package |
   |---|---|
   | `MeterRegistryCustomizer` | `org.springframework.boot.micrometer.metrics.autoconfigure` |
   | `Health`, `HealthIndicator` | `org.springframework.boot.health.contributor` |
   | `@DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure` |
   | `@AutoConfigureTestDatabase` | `org.springframework.boot.jdbc.test.autoconfigure` |

   Verify the package against the Boot 4.0.3 dependency tree rather than assuming the Boot 3 path.

9. **Boot 4 split sliced-test annotations into per-technology starters.**
   `spring-boot-starter-test` no longer carries them. All are BOM-managed — never add a version.

   | Slice | Starter (test scope) |
   |---|---|
   | `@DataJpaTest` | `spring-boot-starter-data-jpa-test` |
   | `@AutoConfigureTestDatabase` | `spring-boot-starter-jdbc-test` |
   | `@WebMvcTest` | `spring-boot-starter-webmvc-test` |
   | `@DataMongoTest` | `spring-boot-starter-data-mongodb-test` |

   Symptom of the missing starter is `cannot find symbol` on the annotation, not a dependency error.

10. **`*IT` classes are not executed.** `maven-failsafe-plugin` is not configured, and surefire's
    default includes do not match the `IT` suffix — so integration tests compile and are silently
    skipped while the build reports success. Tracked as `SKL-32`; until it lands, a test named `*IT`
    does not run.

## Sonar

`sonar-maven-plugin` 5.0.0.4389 is declared in the parent's `<plugins>`, so `sonar:sonar` is
available on every module. Exclusions live in two places — keep them in sync:

- `code/pom.xml`: `<sonar.exclusions>**/application-local.yml, **/application-local.yml.example</sonar.exclusions>`
- `sonar-project.properties` at repo root

Rules that have failed the gate before: `java:S6204` — use `Stream.toList()`, never
`collect(Collectors.toList())`.

## Adding a module — checklist

1. Create `code/<name>/pom.xml` with the `turbo.diesel:skeletoni` parent.
2. Add `<module><name></module>` to `code/pom.xml`.
3. Add `COPY code/<name>/pom.xml code/<name>/` to the `Dockerfile` **before** `dependency:go-offline`.
4. Add the dependency edge to whichever module needs it, and record it in
   [../architecture/module-topology.md](../architecture/module-topology.md).

Related: [summary.md](summary.md), [ci-cd.md](ci-cd.md)
