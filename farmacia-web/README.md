# 🌐 Farmacia Web

Versión web del sistema de gestión de farmacia, construida con Spring Boot.

## 🚀 Deployment en Fly.io (Gratuito)

### Requisitos previos
1. Instalar Fly CLI: https://fly.io/docs/hands-on/install-flyctl/
2. Crear cuenta en Fly.io (gratis)

### Pasos para deployment

```bash
# 1. Navegar al directorio
cd farmacia-web

# 2. Login en Fly.io
fly auth login

# 3. Lanzar la aplicación (primera vez)
fly launch --copy-config --name farmacia-demo

# 4. Crear volumen persistente para la base de datos
fly volumes create farmacia_data --region gru --size 1

# 5. Deploy
fly deploy

# 6. Ver logs
fly logs

# 7. Abrir en el navegador
fly open
```

### Actualizar la aplicación

```bash
cd farmacia-web
fly deploy
```

### Características del deployment

- ✅ **Sin cold starts**: la aplicación está siempre disponible
- ✅ **Persistencia**: SQLite con volumen persistente
- ✅ **HTTPS automático**: certificado SSL gratuito
- ✅ **Health checks**: monitoreo automático
- ✅ **256MB RAM**: suficiente para Spring Boot
- ✅ **Gratuito**: dentro del free tier de Fly.io

### URLs

- **Demo**: https://farmacia-demo.fly.dev
- **API**: https://farmacia-demo.fly.dev/api/dashboard/kpis

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
