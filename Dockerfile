# ── Etapa 1: Compilar con Maven ───────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Instalar Maven
RUN apk add --no-cache maven

# Copiar pom.xml primero (caching de dependencias)
COPY backend/pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar frontend al directorio de recursos estáticos de Spring Boot
# Así el JAR sirve el frontend directamente en /
COPY frontend/ src/main/resources/static/

# Copiar código fuente Java y compilar (incluye los archivos estáticos del frontend)
COPY backend/src ./src
RUN mvn package -DskipTests -B

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
  "-Duser.timezone=America/Argentina/Buenos_Aires", \
  "-jar", "app.jar"]
