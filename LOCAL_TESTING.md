# Testing Local con PostgreSQL y Docker

Esta guía te ayudará a probar localmente el módulo `farmacia-web` con PostgreSQL usando Docker Compose.

## Requisitos Previos

- Docker instalado (con Docker Compose)
- Java 21+ (solo si quieres ejecutar sin Docker)
- Maven 3.9+ (solo si quieres ejecutar sin Docker)

## Opción 1: Usar Docker Compose (Recomendado)

Esta opción levanta automáticamente PostgreSQL y la aplicación web.

```bash
# Desde la raíz del proyecto
docker-compose up --build
```

La aplicación estará disponible en: http://localhost:8080

Para detener los servicios:
```bash
docker-compose down
```

Para eliminar también los datos de PostgreSQL:
```bash
docker-compose down -v
```

## Opción 2: PostgreSQL en Docker + Aplicación en Maven

Si prefieres ejecutar la aplicación directamente con Maven (útil para desarrollo):

### 1. Levantar solo PostgreSQL

```bash
docker run -d \
  --name farmacia-postgres \
  -e POSTGRES_DB=farmacia \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Ejecutar la aplicación web

```bash
# Desde la raíz del proyecto
mvn spring-boot:run -pl farmacia-web
```

La aplicación usará las credenciales por defecto configuradas en `application.properties`:
- URL: `jdbc:postgresql://localhost:5432/farmacia`
- Usuario: `postgres`
- Password: `postgres`

### 3. Detener PostgreSQL

```bash
docker stop farmacia-postgres
docker rm farmacia-postgres
```

## Opción 3: Modo Desarrollo con SQLite (Solo farmacia-web)

Si quieres probar localmente sin PostgreSQL, puedes usar SQLite:

```bash
# Desde la raíz del proyecto
mvn spring-boot:run -pl farmacia-web -Dspring-boot.run.profiles=dev
```

Esto usará el perfil `dev` que está configurado para usar SQLite en `~/.farmacia/data/farmacia.db`.

## Verificación

Una vez que la aplicación esté corriendo, prueba estos endpoints:

### Health Check
```bash
curl http://localhost:8080/api/dashboard/kpis
```

### Ver dashboard en el navegador
Abre http://localhost:8080 en tu navegador

### API de productos
```bash
curl http://localhost:8080/api/productos
```

### API de stock
```bash
curl http://localhost:8080/api/stock
```

### API de vencimientos
```bash
curl http://localhost:8080/api/vencimientos?dias=30
```

## Migraciones de Base de Datos

Las migraciones Flyway se ejecutan automáticamente al iniciar la aplicación:

- **PostgreSQL**: Usa scripts en `farmacia-core/src/main/resources/db/migration/postgresql/`
- **SQLite (dev)**: Usa scripts en `farmacia-core/src/main/resources/db/migration/sqlite/`

Si necesitas limpiar la base de datos y volver a ejecutar todas las migraciones:

```bash
# Para PostgreSQL en Docker
docker-compose down -v
docker-compose up --build

# O manualmente
docker exec -it farmacia-postgres psql -U postgres -c "DROP DATABASE farmacia; CREATE DATABASE farmacia;"
```

## Logs

### Ver logs en Docker Compose
```bash
# Todos los servicios
docker-compose logs -f

# Solo farmacia-web
docker-compose logs -f farmacia-web

# Solo postgres
docker-compose logs -f postgres
```

### Ver logs de PostgreSQL standalone
```bash
docker logs farmacia-postgres
```

## Troubleshooting

### Puerto 5432 ya en uso
Si ya tienes PostgreSQL corriendo localmente:
```bash
# Cambia el puerto en docker-compose.yml o usa:
docker run -d \
  --name farmacia-postgres \
  -e POSTGRES_DB=farmacia \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  postgres:16-alpine

# Y actualiza DATABASE_URL a:
# jdbc:postgresql://localhost:5433/farmacia
```

### Error de conexión a PostgreSQL
Asegúrate de que PostgreSQL esté corriendo:
```bash
docker ps | grep postgres
```

### Rebuild completo
```bash
# Elimina todo (contenedores, volúmenes, imágenes)
docker-compose down -v --rmi all

# Vuelve a construir desde cero
docker-compose up --build
```

### Inspeccionar base de datos PostgreSQL
```bash
# Conectarse al contenedor
docker exec -it farmacia-postgres psql -U postgres -d farmacia

# Ver tablas
\dt

# Ver datos de productos
SELECT * FROM productos;

# Ver migraciones aplicadas
SELECT * FROM flyway_schema_history;

# Salir
\q
```
