# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — Builder
# Compiles the source and produces a fat JAR with all dependencies.
# Uses the full JDK + Maven. This layer is discarded in the final image.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first — Docker caches this layer.
# Dependencies are re-downloaded only when pom.xml changes, not on every build.
COPY mvnw.cmd ./mvnw
COPY pom.xml ./

# Fix line endings and make wrapper executable (pom was created on Windows)
RUN sed -i 's/\r//' mvnw && chmod +x mvnw

# Download dependencies (cached layer — runs only when pom.xml changes)
RUN ./mvnw dependency:go-offline -q

# Copy source code and build, skipping tests (tests run in CI, not in Docker build)
COPY src ./src
RUN ./mvnw package -DskipTests -q

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
