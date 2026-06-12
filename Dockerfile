# syntax=docker/dockerfile:1.7

# ---- Build stage: compile and package the Spring Boot application ----
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

# Gradle wrapper + build files first for better layer caching
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ---- Runtime stage: minimal JRE image running the boot jar ----
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system mediwalk && useradd --system --gid mediwalk mediwalk

# bootJar produces <name>-<version>.jar (no -plain suffix) — only that jar is copied
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar
RUN chown mediwalk:mediwalk app.jar

USER mediwalk
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
