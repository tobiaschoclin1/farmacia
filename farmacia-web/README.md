# 🌐 Farmacia Web

Versión web del sistema de gestión de farmacia, construida con Spring Boot.

## 🚀 Deployment recomendado: Render + Cron Job

### Requisitos previos

1. Cuenta en Render: https://render.com/
2. PostgreSQL gratuito (recomendado Neon): https://neon.tech/
3. Repositorio en GitHub

### Pasos rápidos

1. Crear DB PostgreSQL en Neon
2. En Render: **New** → **Web Service**
3. Conectar este repositorio
4. Configurar Docker:
	- Environment: `Docker`
	- Dockerfile Path: `./Dockerfile`
5. Variables de entorno:
	- `DATABASE_URL`
	- `DATABASE_USER`
	- `DATABASE_PASSWORD`
	- `JAVA_OPTS=-Xmx512m -Xms256m`
6. Deploy

### Evitar sleep (mitigación)

Este proyecto incluye workflow:

- `.github/workflows/keep-render-awake.yml`

Configura en GitHub el secret:

- `RENDER_HEALTHCHECK_URL=https://tu-app.onrender.com/api/dashboard/kpis`

Con eso, se envía un ping cada 10 minutos para reducir cold starts.

### Guía completa

Ver [RENDER_DEPLOY.md](../RENDER_DEPLOY.md).

## 🛠️ Desarrollo local

```bash
# Desde el directorio raíz del proyecto
mvn clean install

# Ejecutar
cd farmacia-web
mvn spring-boot:run

# Abrir en el navegador
open http://localhost:8080
```

## 📊 Endpoints API

- `GET /` - Dashboard principal
- `GET /productos` - Lista de productos
- `GET /api/dashboard/kpis` - KPIs del dashboard
- `GET /api/dashboard/proximos-vencer` - Productos próximos a vencer
- `GET /api/dashboard/stock-bajo` - Productos con stock bajo
- `GET /api/productos` - Lista completa de productos
- `POST /api/productos` - Crear producto
- `PUT /api/productos/{id}` - Actualizar producto
- `DELETE /api/productos/{id}` - Eliminar producto

## 🎨 Diseño

El diseño web replica exactamente la versión desktop de JavaFX:
- Misma paleta de colores
- Mismos componentes visuales
- Misma estructura de navegación
- Mismo estilo profesional tipo SaaS
