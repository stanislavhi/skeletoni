# Configuration Reference

All runtime configuration lives in `code/boot/src/main/resources/`.

```
application.yml            base — env-var indirection for everything sensitive
application-local.yml      profile `local` — real values for compose services
application-local.yml.example   template
logback-spring.xml         console appender + inactive Logstash appender
code/boot/src/test/resources/application-test.yml   profile `test`
```

## Env-var strategy

Two deliberate styles in `application.yml`:

```yaml
# no default → fails fast at startup if unset
url: ${SPRING_DATASOURCE_URL}
uri: ${SPRING_DATA_MONGODB_URI}
password: ${SPRING_RABBITMQ_PASSWORD}

# default supplied → safe for local dev
bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
schema.registry.url: ${SPRING_KAFKA_SCHEMA_REGISTRY_URL:http://localhost:8085}
group-id: ${SPRING_KAFKA_CONSUMER_GROUP_ID:skeletoni-group}
```

Rule: **secrets and connection strings get no default**; non-sensitive endpoints get one.
The consequence is that a bare `mvn spring-boot:run` without the `local` profile or env vars fails
with `Could not resolve placeholder 'SPRING_DATASOURCE_URL'`. That is intended behaviour, not a bug.

## Full variable table

| Variable | Local value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` |
| `SERVER_PORT` | `8080` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/skeletoni` |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | `user` / `password` |
| `SPRING_DATA_MONGODB_URI` | `mongodb://root:rootpassword@localhost:27017/skeletoni?authSource=admin` |
| `SPRING_DATA_COUCHBASE_CONNECTION_STRING` | `localhost` |
| `SPRING_DATA_COUCHBASE_USERNAME` / `_PASSWORD` | `admin` / `password` |
| `SPRING_DATA_COUCHBASE_BUCKET_NAME` | `skeletoni` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `SPRING_KAFKA_SCHEMA_REGISTRY_URL` | `http://localhost:8085` |
| `SPRING_KAFKA_CONSUMER_GROUP_ID` | `skeletoni-group` |
| `SPRING_RABBITMQ_HOST` / `_PORT` | `localhost` / `5672` |
| `SPRING_RABBITMQ_USERNAME` / `_PASSWORD` | `user` / `password` |

## Non-connection settings

```yaml
# base application.yml — applies to every profile
spring.jpa.hibernate.ddl-auto: validate    # Flyway owns the schema
spring.jpa.properties.hibernate.format_sql: true
server.port: 8080
grpc.server.port: 9091
logging.level.turbo.diesel.skeletoni: DEBUG
logging.pattern.console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] [%X{correlationId:-}] %-5level %logger{36} - %msg%n"
management.endpoint.health.show-details: when-authorized
```

Developer-only settings live in `application-local.yml` (and its `.example` template), never in
base config: `spring.jpa.show-sql: true` and `management.endpoint.health.show-details: always`.
Keep it that way — anything in base config ships to production.

Kafka serialization is **JSON**, not Avro, despite the Avro tooling being present:
`JsonSerializer` on the producer, `ErrorHandlingDeserializer` on the consumer.

## Test profile

`application-test.yml` does three things:

1. H2 in-memory with `MODE=PostgreSQL`, `ddl-auto: create-drop`, `flyway.enabled: true`.
2. Excludes Mongo, Couchbase, Kafka and Rabbit auto-configuration, and sets
   `spring.cloud.stream.enabled: false`.
3. **Supplies dummy values for every mandatory placeholder anyway** —
   `SPRING_DATA_COUCHBASE_BUCKET_NAME`, `SPRING_DATA_MONGODB_URI`, etc.

Step 3 is not redundant. Excluding an auto-configuration does not stop property placeholder
resolution, so without the dummies the context fails to load with
`Could not resolve placeholder`. Whenever a new mandatory `${VAR}` is added to `application.yml`,
add a matching dummy here or every `@SpringBootTest` breaks.

Also note `spring.main.allow-bean-definition-overriding: true` in the test profile.

## `application-local.yml` is untracked by design

`.gitignore` contains `**/application-local.yml`, and the file is now genuinely untracked —
`git ls-files` returns only `application-local.yml.example`. It exists on every developer's disk but
never in the index, so credentials placed there cannot be committed.

`application-local.yml.example` **is** tracked and is the template. Any new key added to the local
config must be mirrored into the `.example`, or the next clone boots with an unresolvable
placeholder.

Historical note worth remembering: the file was tracked for a while despite the ignore rule, because
`.gitignore` has no effect on already-tracked paths — it required an explicit
`git rm --cached`. `KNOWLEDGE-BASE.md` §6 may still describe the old broken state (`SKL-27`).

Related: [summary.md](summary.md), [../infrastructure/database-migrations.md](../infrastructure/database-migrations.md)
