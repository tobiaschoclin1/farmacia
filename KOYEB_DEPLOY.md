# Despliegue en Koyeb - Farmacia Web

Esta guía describe cómo desplegar el módulo `farmacia-web` en Koyeb usando PostgreSQL.

## Requisitos Previos

1. Cuenta en [Koyeb](https://www.koyeb.com/)
2. Base de datos PostgreSQL (puedes usar el PostgreSQL managed de Koyeb o cualquier proveedor gratuito como Neon, Supabase, ElephantSQL, etc.)

## Variables de Entorno Requeridas

Configura las siguientes variables de entorno en Koyeb:

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DATABASE_URL` | URL de conexión JDBC a PostgreSQL | `jdbc:postgresql://hostname:5432/farmacia` |
| `DATABASE_USER` | Usuario de PostgreSQL | `postgres` |
| `DATABASE_PASSWORD` | Contraseña de PostgreSQL | `tu_password_seguro` |
| `JAVA_OPTS` | Opciones JVM (opcional) | `-Xmx512m -Xms256m` |

## Pasos de Despliegue

### Opción 1: Despliegue desde Docker Hub

1. **Construir y pushear imagen Docker**

```bash
# Desde la raíz del proyecto
docker build -t tu-usuario/farmacia-web:latest .
docker push tu-usuario/farmacia-web:latest
```

2. **Crear servicio en Koyeb**
   - Ve a la consola de Koyeb
   - Click en "Create Service"
   - Selecciona "Docker" como deployment method
   - Imagen: `tu-usuario/farmacia-web:latest`
   - Puerto: `8080`
   - Health check path: `/api/dashboard/kpis`

3. **Configurar variables de entorno**
   - En la sección "Environment variables", agrega las variables listadas arriba

### Opción 2: Despliegue desde GitHub

1. **Conectar repositorio**
   - En Koyeb, selecciona "Deploy from GitHub"
   - Conecta tu repositorio
   - Branch: `main` (o el que uses)

2. **Configurar build**
   - Builder: `Dockerfile`
   - Dockerfile path: `./Dockerfile`
   - Build context: raíz del proyecto

3. **Configurar servicio**
   - Puerto: `8080`
   - Health check path: `/api/dashboard/kpis`
   - Environment variables: agregar las variables listadas arriba

### Opción 3: Despliegue con Koyeb CLI

```bash
# Instalar Koyeb CLI
curl -fsSL https://cli.koyeb.com/install.sh | sh

# Login
koyeb login

# Crear servicio
koyeb service create farmacia-web \
  --docker tu-usuario/farmacia-web:latest \
  --ports 8080:http \
  --routes /:8080 \
  --env DATABASE_URL=jdbc:postgresql://hostname:5432/farmacia \
  --env DATABASE_USER=postgres \
  --env DATABASE_PASSWORD=tu_password \
  --health-checks http:8080:/api/dashboard/kpis
```

## Base de Datos PostgreSQL

### Opción 1: PostgreSQL en Koyeb

Koyeb ofrece bases de datos PostgreSQL managed. Configúrala desde el dashboard:
1. Ve a "Databases" en Koyeb
2. Crea una nueva instancia PostgreSQL
3. Copia las credenciales y úsalas en las variables de entorno

### Opción 2: Proveedores Gratuitos de PostgreSQL

- **Neon** (https://neon.tech): Free tier con 0.5 GB, auto-scaling
- **Supabase** (https://supabase.com): Free tier con 500 MB
- **ElephantSQL** (https://www.elephantsql.com): Free tier con 20 MB
- **Render** (https://render.com): Free tier con 256 MB (expira después de 90 días de inactividad)

## Verificación del Despliegue

Una vez desplegado, verifica que el servicio esté funcionando:

```bash
# Reemplaza YOUR-APP.koyeb.app con tu URL de Koyeb
curl https://YOUR-APP.koyeb.app/api/dashboard/kpis
```

Deberías recibir una respuesta JSON con los KPIs del dashboard.

## Endpoints Disponibles

- `GET /` - Página principal (Dashboard)
- `GET /productos` - Lista de productos
- `GET /stock` - Control de stock
- `GET /vencimientos` - Productos próximos a vencer
- `GET /api/dashboard/kpis` - KPIs del dashboard (JSON)
- `GET /api/productos` - API de productos (JSON)
- `GET /api/stock` - API de stock (JSON)
- `GET /api/vencimientos` - API de vencimientos (JSON)

## Migraciones de Base de Datos

Las migraciones se ejecutan automáticamente al iniciar la aplicación mediante Flyway:
- Los scripts están en `farmacia-core/src/main/resources/db/migration/postgresql/`
- Flyway aplica automáticamente las migraciones pendientes al arrancar

## Troubleshooting

### Error de conexión a base de datos
- Verifica que las variables `DATABASE_URL`, `DATABASE_USER` y `DATABASE_PASSWORD` estén configuradas correctamente
- Asegúrate de que la base de datos PostgreSQL esté accesible desde Koyeb

### Error en migraciones Flyway
- Revisa los logs de Koyeb para ver qué migración falló
- Verifica que los scripts en `db/migration/postgresql/` sean compatibles con PostgreSQL

### La aplicación no responde
- Verifica el health check en `/api/dashboard/kpis`
- Revisa los logs de Koyeb para errores de inicio
- Asegúrate de que el puerto `8080` esté correctamente expuesto

### Consumo de memoria
- Ajusta `JAVA_OPTS` para limitar memoria: `-Xmx512m -Xms256m`
- Koyeb free tier tiene límites de memoria, ajusta según tu plan

## Recursos

- Documentación de Koyeb: https://www.koyeb.com/docs
- Koyeb CLI: https://www.koyeb.com/docs/cli
- PostgreSQL en Koyeb: https://www.koyeb.com/docs/databases
