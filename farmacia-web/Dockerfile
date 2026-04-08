# Multi-stage build para optimizar tamaño
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /build

# Copiar archivos de configuración Maven
COPY ../pom.xml .
COPY farmacia-core/pom.xml farmacia-core/
COPY farmacia-web/pom.xml farmacia-web/

# Copiar código fuente
COPY farmacia-core/src farmacia-core/src
COPY farmacia-web/src farmacia-web/src

# Compilar solo los módulos necesarios
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -pl farmacia-web -am -DskipTests && \
    mv farmacia-web/target/*.jar app.jar

# Runtime stage - imagen ligera
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Crear usuario no-root para seguridad
RUN groupadd -r farmacia && useradd -r -g farmacia farmacia && \
    mkdir -p /home/farmacia/.farmacia/data && \
    chown -R farmacia:farmacia /home/farmacia

# Copiar JAR desde build stage
COPY --from=build /build/app.jar app.jar

# Cambiar a usuario no-root
USER farmacia

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/dashboard/kpis || exit 1

# Ejecutar aplicación
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
