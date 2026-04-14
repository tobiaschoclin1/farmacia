# Guía de Despliegue - Sistema Farmacia

Este documento resume las opciones de despliegue para el sistema de gestión farmacéutica.

## Arquitectura del Proyecto

El proyecto está dividido en 3 módulos Maven:

```
farmacia-parent/
├── farmacia-core/       # Lógica compartida (DAOs, Services, Models)
├── farmacia-desktop/    # Aplicación JavaFX (usa SQLite)
└── farmacia-web/        # Aplicación Spring Boot (usa PostgreSQL en producción)
```

### Base de Datos

- **farmacia-desktop**: Exclusivamente SQLite local (`~/.farmacia/data/farmacia.db`)
- **farmacia-web**: 
  - Producción: PostgreSQL
  - Desarrollo local: SQLite (perfil `dev`) o PostgreSQL

## Opciones de Despliegue

### 1. Despliegue en Render + Cron Job (Recomendado para $0)

**Ventajas:**
- ✅ Plan gratuito disponible
- ✅ Despliegue simple desde GitHub
- ✅ Logs y health checks integrados
- ✅ Compatible con PostgreSQL externo (Neon/Supabase)
- ✅ Mitigación de reposo con cron job

**Pasos:**
Ver guía completa en [RENDER_DEPLOY.md](./RENDER_DEPLOY.md)

**Resumen rápido:**
```bash
# 1. Crear DB PostgreSQL gratuita (Neon)
# 2. Crear Web Service en Render (Environment: Docker, Dockerfile: ./Dockerfile)
# 3. Configurar env vars:
DATABASE_URL=jdbc:postgresql://host:5432/farmacia?sslmode=require
DATABASE_USER=postgres
DATABASE_PASSWORD=tu_password

# 4. Configurar ping anti-sleep (GitHub Actions)
# secret: RENDER_HEALTHCHECK_URL=https://tu-app.onrender.com/api/dashboard/kpis
```

### 2. Testing Local con Docker Compose

**Ventajas:**
- ✅ Ambiente idéntico a producción
- ✅ Fácil de levantar/bajar
- ✅ PostgreSQL incluido

**Pasos:**
Ver guía completa en [LOCAL_TESTING.md](./LOCAL_TESTING.md)

**Resumen rápido:**
```bash
# Levantar todo (PostgreSQL + farmacia-web)
docker-compose up --build

# Acceder a http://localhost:8080
```

### 3. Desarrollo Local con Maven

**Para desarrollo del módulo web:**
```bash
# Con PostgreSQL en Docker
docker run -d --name farmacia-postgres \
  -e POSTGRES_DB=farmacia \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Ejecutar farmacia-web
mvn spring-boot:run -pl farmacia-web

# O con SQLite (perfil dev)
mvn spring-boot:run -pl farmacia-web -Dspring-boot.run.profiles=dev
```

**Para desarrollo del módulo desktop:**
```bash
# Desde la raíz
mvn javafx:run -pl farmacia-desktop

# O compilar JAR ejecutable
mvn package -pl farmacia-desktop
java -jar farmacia-desktop/target/farmacia-desktop-0.1.0.jar
```

## Bases de Datos PostgreSQL Gratuitas

Para producción, puedes usar cualquiera de estos proveedores:

| Proveedor | Free Tier | Límites |
|-----------|-----------|---------|
| **Neon** | ✅ | 0.5 GB storage, auto-scaling |
| **Supabase** | ✅ | 500 MB storage, 2 GB bandwidth |
| **Render PostgreSQL** | ⚠️ | Generalmente pago en planes recientes |
| **ElephantSQL** | ✅ | 20 MB storage (limitado) |
| **Aiven** | ⚠️ | Trial, no siempre gratis permanente |

## Variables de Entorno Necesarias

Para desplegar `farmacia-web` en cualquier plataforma, configura:

```bash
DATABASE_URL=jdbc:postgresql://hostname:5432/farmacia
DATABASE_USER=postgres
DATABASE_PASSWORD=tu_password_seguro
JAVA_OPTS=-Xmx512m -Xms256m  # Opcional, para limitar memoria
```

## Endpoints Principales

Una vez desplegado, estos son los endpoints disponibles:

### Vistas Web (HTML)
- `GET /` - Dashboard principal
- `GET /productos` - Gestión de productos
- `GET /stock` - Control de stock
- `GET /vencimientos` - Productos por vencer

### API REST (JSON)
- `GET /api/dashboard/kpis` - KPIs del dashboard
- `GET /api/dashboard/proximos-vencer` - Productos próximos a vencer
- `GET /api/dashboard/stock-bajo` - Productos con stock bajo
- `GET /api/productos` - Lista de productos
- `GET /api/productos/codigo/{codigo}` - Producto por código de barra
- `GET /api/stock` - Stock actual de productos
- `GET /api/stock/lotes?productoId=X` - Lotes de un producto
- `GET /api/vencimientos?dias=90` - Productos que vencen en X días
- `GET /api/vencimientos/stats` - Estadísticas de vencimientos

## Migraciones de Base de Datos

Las migraciones Flyway se ejecutan automáticamente al iniciar:

- **SQLite**: `farmacia-core/src/main/resources/db/migration/sqlite/`
- **PostgreSQL**: `farmacia-core/src/main/resources/db/migration/postgresql/`

El módulo `farmacia-web` selecciona automáticamente las migraciones correctas según el dialecto configurado en `application.properties`.

## Compilación del Proyecto

### Compilar todo
```bash
mvn clean package
```

### Compilar solo farmacia-web
```bash
mvn clean package -pl farmacia-web -am
```

### Compilar solo farmacia-desktop
```bash
mvn clean package -pl farmacia-desktop -am
```

### Ejecutar tests
```bash
mvn test
```

## Dockerfile

El `Dockerfile` en la raíz está optimizado para compilar y ejecutar **exclusivamente** `farmacia-web`:

- Multi-stage build (build + runtime)
- Java 21 (Temurin)
- Non-root user para seguridad
- Health check en `/api/dashboard/kpis`
- Puerto 8080 expuesto

## Health Checks

Todas las plataformas de despliegue deben configurar el health check en:

```
GET /api/dashboard/kpis
```

Este endpoint retorna los KPIs principales del sistema y confirma que:
- La aplicación está corriendo
- La base de datos está conectada
- Las queries funcionan correctamente

## Próximos Pasos

1. **Testing local**: Sigue [LOCAL_TESTING.md](./LOCAL_TESTING.md)
2. **Despliegue en Render**: Sigue [RENDER_DEPLOY.md](./RENDER_DEPLOY.md)
3. **Configurar base de datos**: Elige un proveedor PostgreSQL gratuito
4. **Verificar endpoints**: Prueba todos los endpoints de API

## Troubleshooting

Ver secciones de troubleshooting en:
- [RENDER_DEPLOY.md](./RENDER_DEPLOY.md#troubleshooting-rápido)
- [LOCAL_TESTING.md](./LOCAL_TESTING.md#troubleshooting)

## Recursos

- Repositorio GitHub: (agregar tu URL)
- Demo en vivo: (agregar URL de Render una vez desplegado)
- Documentación Render: https://render.com/docs
