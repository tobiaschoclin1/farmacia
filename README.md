# 💊 Sistema de Gestión Farmacéutica

Sistema web moderno para gestión integral de stock, productos y movimientos farmacéuticos.

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?style=flat-square)](https://www.postgresql.org/)

🌐 **Demo en vivo:** [https://farmacia-2xm1.onrender.com](https://farmacia-2xm1.onrender.com)

---

## 📋 Características

- ✅ **Gestión de Productos** - CRUD completo con búsqueda
- ✅ **Control de Stock** - Seguimiento en tiempo real
- ✅ **Vencimientos** - Alertas de productos próximos a vencer
- ✅ **Entradas/Salidas** - Registro de movimientos
- ✅ **Sistema de Usuarios** - Autenticación y roles (Admin/Usuario)
- ✅ **Dashboard** - KPIs y métricas del sistema
- ✅ **Interfaz Moderna** - Diseño profesional y responsive

---

## 🚀 Inicio Rápido

### Prerrequisitos

- **Java 21** o superior
- **Maven 3.8+**
- **PostgreSQL** (producción) o SQLite (desarrollo)

### Ejecutar Localmente

```bash
# Clonar repositorio
git clone https://github.com/tobiaschoclin1/farmacia.git
cd farmacia

# Compilar proyecto
mvn clean install

# Ejecutar aplicación web
cd farmacia-web
mvn spring-boot:run

# Abrir en navegador
open http://localhost:8080
```

---

## 🔐 Credenciales

**Email:** `admin@farmacia.com`  
**Contraseña:** `admin123`

⚠️ **Cambiar contraseña después del primer login** (Mi Perfil → Cambiar Contraseña)

---

## 🌐 Deploy en Render (Gratis)

### 1. Base de Datos PostgreSQL

**Opción: Neon (Recomendado)**
1. [neon.tech](https://neon.tech) → Sign up
2. Crear proyecto "farmacia"
3. Copiar Connection String

### 2. Web Service en Render

1. [render.com](https://render.com) → New Web Service
2. Conectar repositorio GitHub
3. Configuración:
   - **Environment:** Docker
   - **Dockerfile:** `./Dockerfile`

### 3. Variables de Entorno

```bash
DATABASE_URL=jdbc:postgresql://host.neon.tech:5432/neondb?sslmode=require
DATABASE_USER=usuario
DATABASE_PASSWORD=password
```

### 4. Keep-Alive (Evitar Sleep)

**UptimeRobot (Recomendado):**
1. [uptimerobot.com](https://uptimerobot.com) → Add Monitor
2. URL: `https://tu-app.onrender.com/api/dashboard/kpis`
3. Interval: 5 minutes

✅ Tu servicio nunca dormirá

---

## 🏗️ Arquitectura

```
farmacia/
├── farmacia-core/          # Lógica de negocio
│   ├── dao/               # Acceso a datos
│   ├── model/             # Modelos
│   ├── service/           # Servicios
│   └── db/migration/      # Migraciones Flyway
│
├── farmacia-desktop/       # App JavaFX (opcional)
│   └── ui/                # Interfaces gráficas
│
└── farmacia-web/           # App Spring Boot
    ├── controller/        # REST API
    ├── static/            # CSS, JS, imágenes
    └── templates/         # HTML (Thymeleaf)
```

---

## 📊 API Endpoints

### Dashboard
- `GET /api/dashboard/kpis` - KPIs principales

### Productos
- `GET /api/productos` - Listar
- `POST /api/productos` - Crear
- `PUT /api/productos/{id}` - Actualizar
- `DELETE /api/productos/{id}` - Eliminar

### Autenticación
- `POST /api/auth/registro` - Crear cuenta
- `POST /api/auth/login` - Iniciar sesión

### Stock y Vencimientos
- `GET /api/stock` - Estado actual
- `GET /api/vencimientos` - Próximos a vencer

---

## 🛠️ Tecnologías

**Backend:**
- Java 21
- Spring Boot 3.2.4
- Flyway (migraciones)
- PostgreSQL / SQLite

**Frontend:**
- Thymeleaf
- JavaScript vanilla
- CSS custom

**DevOps:**
- Docker
- Render
- GitHub Actions

---

## 🐛 Troubleshooting

### App no arranca

```bash
# Verifica variables de entorno
echo $DATABASE_URL

# Cambia puerto si está ocupado
export PORT=8081
mvn spring-boot:run
```

### Migraciones Flyway fallan

```sql
-- Conectarse a PostgreSQL y limpiar
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
-- Luego redeploy
```

### Servicio se duerme

Configura UptimeRobot (ver sección Keep-Alive)

---

## 📝 Licencia

MIT License - Código abierto para fines educativos

---

## 👨‍💻 Autor

**Tobias Choclin**  
📧 Email: tobiaschoclin1@gmail.com  
🔗 GitHub: [github.com/tobiaschoclin1](https://github.com/tobiaschoclin1)  
🌐 Demo: [farmacia-2xm1.onrender.com](https://farmacia-2xm1.onrender.com)

---

<div align="center">

**Sistema de Gestión Farmacéutica** | Hecho con ❤️ usando Spring Boot

</div>
