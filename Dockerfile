# ── Etapa 1: Compilar con Maven ──────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiar solo el pom.xml primero (caching de dependencias)
COPY backend/pom.xml .
COPY backend/.mvn .mvn
COPY backend/mvnw .

# Descargar dependencias (cacheado si no cambia el pom)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar
COPY backend/src ./src
RUN ./mvnw package -DskipTests -B

# ── Etapa 2: Imagen final liviana ─────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S pic21 && adduser -S pic21 -G pic21
USER pic21

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=70.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
