# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — Builder
# Uses the official Maven image (includes JDK 17 + Maven pre-installed).
# No wrapper script needed — avoids Windows/Linux mvnw compatibility issues.
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml first — Docker caches this layer.
# Dependencies are re-downloaded only when pom.xml changes, not on every build.
COPY pom.xml ./
RUN mvn dependency:go-offline -q

# Copy source and build, skipping tests (tests run in CI, not Docker build)
COPY src ./src
RUN mvn package -DskipTests -q

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — Runtime
# Only JRE (no JDK, no Maven, no source code) — keeps the image small.
# eclipse-temurin:17-jre-alpine ≈ 80 MB base vs 400 MB for JDK
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security — never run apps as root in containers
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy only the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port Spring Boot listens on
EXPOSE 8080

# JVM flags for containers:
# -XX:+UseContainerSupport  — respect Docker memory limits (not host RAM)
# -XX:MaxRAMPercentage=75.0 — use up to 75% of container memory for heap
# -Djava.security.egd=...   — faster startup (avoids /dev/random blocking)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
