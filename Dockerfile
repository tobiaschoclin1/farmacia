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

# Instalar curl para healthcheck
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Copiar JAR y script de entrada
COPY --from=build /build/app.jar app.jar
COPY docker-entrypoint.sh /usr/local/bin/

# Hacer ejecutable el script
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Crear usuario no-root para seguridad
RUN groupadd -r farmacia && useradd -r -g farmacia farmacia && \
    chown -R farmacia:farmacia /app && \
    chown farmacia:farmacia /usr/local/bin/docker-entrypoint.sh

# Cambiar a usuario no-root
USER farmacia

# Exponer puerto HTTP
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/dashboard/kpis || exit 1

# Variables de entorno (Spring Boot las lee)
ENV PORT=8080

# Script de entrada que maneja JAVA_OPTS correctamente
ENTRYPOINT ["docker-entrypoint.sh"]
