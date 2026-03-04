# ⚙️ CONFIGURATIONS.md

This document serves as a reference for all externalized configuration properties and environment variables used in **skeletoni**.

---

## 🌐 Global Environment Variables

| Variable | Description | Default (Local) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`local`, `test`, `prod`) | `local` |
| `SERVER_PORT` | HTTP port for the REST API | `8080` |

---

## 🗄️ Database Configurations

### PostgreSQL
| Variable | Description | Default (Local) |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC Connection URL | `jdbc:postgresql://localhost:5432/skeletoni` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` |

### MongoDB
| Variable | Description | Default (Local) |
|---|---|---|
| `SPRING_DATA_MONGODB_URI` | MongoDB Connection URI | `mongodb://root:rootpassword@localhost:27017/skeletoni?authSource=admin` |

### Couchbase
| Variable | Description | Default (Local) |
|---|---|---|
| `SPRING_DATA_COUCHBASE_CONNECTION_STRING` | Couchbase host | `localhost` |
| `SPRING_DATA_COUCHBASE_USERNAME` | Bucket username | `admin` |
| `SPRING_DATA_COUCHBASE_PASSWORD` | Bucket password | `password` |
| `SPRING_DATA_COUCHBASE_BUCKET_NAME` | Main bucket name | `skeletoni` |

---

## 📨 Messaging Configurations

### Kafka
| Variable | Description | Default (Local) |
|---|---|---|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | List of Kafka brokers | `localhost:9092` |
| `SPRING_KAFKA_SCHEMA_REGISTRY_URL` | Confluent Schema Registry URL | `http://localhost:8085` |
| `SPRING_KAFKA_CONSUMER_GROUP_ID` | Main consumer group ID | `skeletoni-group` |

### RabbitMQ
| Variable | Description | Default (Local) |
|---|---|---|
| `SPRING_RABBITMQ_HOST` | RabbitMQ broker host | `localhost` |
| `SPRING_RABBITMQ_PORT` | AMQP port | `5672` |
| `SPRING_RABBITMQ_USERNAME` | AMQP username | `user` |
| `SPRING_RABBITMQ_PASSWORD` | AMQP password | `password` |

---

## 📈 Observability & Monitoring

### Actuator
- **Health**: `http://localhost:8080/actuator/health`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

### Metrics
- **Prometheus UI**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000` (admin / admin)

---

## 🧪 Testing Profile (`test`)

When the `test` profile is active, the application uses:
- **H2 (In-memory)** instead of PostgreSQL.
- **Auto-configuration exclusions** for Kafka, RabbitMQ, Mongo, and Couchbase.
- **Embedded Flyway** for H2 schema initialization.
