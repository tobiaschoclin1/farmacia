# Multi-stage build para farmacia-web optimizado
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /build

# Copiar POMs primero para aprovechar cache de Docker
# Usar pom-docker.xml (solo declara core y web, no desktop)
COPY pom-docker.xml pom.xml
COPY farmacia-core/pom.xml farmacia-core/
COPY farmacia-web/pom.xml farmacia-web/

# Descargar dependencias (se cachea si los POMs no cambian)
RUN apt-get update && apt-get install -y maven && \
    mvn dependency:go-offline -pl farmacia-web -am || true

# Copiar código fuente
COPY farmacia-core/src farmacia-core/src
COPY farmacia-web/src farmacia-web/src

# Compilar solo farmacia-web y sus dependencias (farmacia-core)
RUN mvn clean package -pl farmacia-web -am -DskipTests && \
    mv farmacia-web/target/*.jar app.jar

# Runtime stage - imagen ligera
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Crear usuario no-root para seguridad
RUN groupadd -r farmacia && useradd -r -g farmacia farmacia && \
    chown -R farmacia:farmacia /app

# Copiar JAR desde build stage
COPY --from=build --chown=farmacia:farmacia /build/app.jar app.jar

# Cambiar a usuario no-root
USER farmacia

# Exponer puerto HTTP
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/dashboard/kpis || exit 1

# Variables de entorno por defecto (la plataforma puede sobrescribirlas)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV PORT=8080

# Ejecutar aplicación
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
