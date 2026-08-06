# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy root POM and code POM
COPY pom.xml .
COPY code/pom.xml code/

# Copy all module POMs to cache dependencies.
# IMPORTANT: every new Maven module MUST get its own COPY line below before
# `dependency:go-offline` runs, otherwise the reactor is incomplete and the build fails.
COPY code/contract/pom.xml code/contract/
COPY code/application/pom.xml code/application/
COPY code/domain/pom.xml code/domain/
COPY code/infrastructure/pom.xml code/infrastructure/
COPY code/logging/pom.xml code/logging/
COPY code/observability/pom.xml code/observability/
COPY code/boot/pom.xml code/boot/
COPY code/resilience/pom.xml code/resilience/

# Download dependencies
RUN mvn -B -f code/pom.xml dependency:go-offline

# Copy source code
COPY code code/

# Build the application
RUN mvn -B -f code/pom.xml clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built artifact from the boot module
COPY --from=build /app/code/boot/target/boot-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
