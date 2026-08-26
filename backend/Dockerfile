# No wrapper script — avoids Windows/Linux mvnw compatibility issues.
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# pom.xml copied separately so this layer caches independently of source changes
COPY pom.xml ./
RUN mvn dependency:go-offline -q

# Tests run in CI, not in the Docker build
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# -XX:+UseContainerSupport respects Docker memory limits instead of host RAM
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
