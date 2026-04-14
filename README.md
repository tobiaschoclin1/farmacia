# 💊 Farmacia - Sistema de Gestión de Stock

<div align="center">

**Sistema multi-plataforma de gestión de inventario farmacéutico**

[![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![JavaFX](https://img.shields.io/badge/JavaFX-22.0.1-4B8BBE?style=flat-square)](https://gluonhq.com/products/javafx/)
[![SQLite](https://img.shields.io/badge/SQLite-3.46+-003B57?style=flat-square&logo=sqlite)](https://www.sqlite.org/)

[🌐 Demo Online en Render](#-deploy-web-gratuito-render--cron-job) • [Documentación](#-estructura-del-proyecto) • [Instalación](#-instalación)

</div>

---

## 🎯 Características

| Funcionalidad | Desktop | Web |
|---|:---:|:---:|
| 📦 **Gestión de Productos** | ✅ | ✅ |
| 📥 **Entrada de Stock** | ✅ | 🚧 |
| 📤 **Salida de Stock** | ✅ | 🚧 |
| 📊 **Vista de Stock** | ✅ | 🚧 |
| ⏰ **Control de Vencimiento** | ✅ | ✅ |
| 💾 **Import/Export Excel** | ✅ | 🚧 |
| 🌐 **Acceso Web 24/7** | ❌ | ✅ |
| 💻 **Aplicación de Escritorio** | ✅ | ❌ |

---

## 🌐 Deploy Web Gratuito (Render + Cron Job)

¿Quieres despliegue sin pagar? Usa **Render free tier** + ping programado para reducir reposos.

### Stack recomendado

- **Hosting web**: Render (free)
- **Base de datos**: Neon PostgreSQL (free)
- **Keep-alive**: GitHub Actions cada 10 minutos

**⏱️ Tiempo de setup:** 15-20 minutos

**📖 Guía paso a paso:** [RENDER_DEPLOY.md](./RENDER_DEPLOY.md)

---

## 📁 Estructura del Proyecto

Este proyecto está organizado como **multi-módulo Maven**:

```
Farmacia/
├── pom.xml                     # POM raíz (parent)
├── farmacia-core/              # ⚙️ Lógica de negocio compartida
│   ├── src/main/java/
│   │   └── com/tobias/
│   │       ├── dao/           # Acceso a datos (ProductDao, etc.)
│   │       ├── model/         # Modelos (Product, EntradaItem, etc.)
│   │       ├── service/       # Lógica de negocio (StockService, etc.)
│   │       ├── db/            # Configuración de base de datos
│   │       └── util/          # Utilidades (ExcelUtil, etc.)
│   └── src/main/resources/
│       └── db/migration/      # Scripts SQL de Flyway
│
├── farmacia-desktop/          # 💻 Aplicación JavaFX (escritorio)
│   ├── src/main/java/
│   │   └── com/tobias/
│   │       ├── App.java       # Punto de entrada JavaFX
│   │       ├── ui/            # Vistas JavaFX (DashboardView, etc.)
│   │       └── util/          # AppBus, Icons, etc.
│   └── src/main/resources/
│       └── ui/
│           └── theme-professional.css
│
└── farmacia-web/              # 🌐 Aplicación Spring Boot (web)
    ├── src/main/java/
    │   └── com/tobias/web/
    │       ├── FarmaciaWebApplication.java
    │       └── controller/    # REST controllers
    ├── src/main/resources/
    │   ├── static/css/        # CSS web
    │   └── templates/         # Plantillas HTML (Thymeleaf)
    ├── Dockerfile             # Para deployment
    ├── fly.toml               # Config antigua de Fly.io
    └── README.md              # Guía de deployment
```

### Ventajas de esta arquitectura

- ✅ **Código compartido**: La lógica de negocio (DAOs, Services, Models) está en `farmacia-core` y es reutilizada por ambas aplicaciones
- ✅ **Mantenimiento simplificado**: Un solo lugar para actualizar la lógica de negocio
- ✅ **Diseño consistente**: Ambas versiones usan la misma paleta de colores y estructura visual
- ✅ **Base de datos unificada**: Ambas usan SQLite con las mismas migraciones Flyway

---

## 🚀 Instalación

### Requisitos Previos

- **Java 21+** instalado ([Descargar](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.8+** instalado ([Descargar](https://maven.apache.org/download.cgi))
- **Git** para control de versiones

### Compilar todo el proyecto

```bash
# Clonar el repositorio
git clone <url-del-repositorio>
cd Farmacia

# Compilar todos los módulos
mvn clean install
```

---

## 💻 Ejecutar Versión Desktop (JavaFX)

```bash
cd farmacia-desktop
mvn javafx:run
```

La aplicación de escritorio se abrirá en una ventana nativa con todas las funcionalidades disponibles.

**Ubicación de la base de datos:**
- **Windows**: `%ProgramData%\Farmacia\data\farmacia.db`
- **macOS**: `~/Library/Application Support/Farmacia/data/farmacia.db`
- **Linux**: `~/.farmacia/data/farmacia.db`

---

## 🌐 Ejecutar Versión Web (Spring Boot)

### Desarrollo local

```bash
cd farmacia-web
mvn spring-boot:run
```

Abre tu navegador en: [http://localhost:8080](http://localhost:8080)

### Demo Online (Render)

La versión web se recomienda en Render free + ping anti-sleep:

**🔗 [Configurar en Render](./RENDER_DEPLOY.md)**

#### Deploy tu propia instancia

```bash
# Sigue la guía completa:
# 1. Crea cuenta en Render (gratuita)
# 2. Configura PostgreSQL en Neon (gratuito)
# 3. Configura ping anti-sleep con GitHub Actions

# Ver: RENDER_DEPLOY.md
```

Ver [RENDER_DEPLOY.md](./RENDER_DEPLOY.md) para guía paso a paso.

---

## 🛠️ Tecnologías

### Compartidas (farmacia-core)
- **Java 21** → Lenguaje principal
- **SQLite 3.46** → Base de datos embebida
- **Flyway 10.15** → Migraciones de base de datos
- **Apache POI 5.2.5** → Manejo de archivos Excel
- **SLF4J 2.0.13** → Logging

### Desktop (farmacia-desktop)
- **JavaFX 22** → Interfaz gráfica de escritorio
- **Maven Shade Plugin** → Empaquetado en JAR ejecutable

### Web (farmacia-web)
- **Spring Boot 3.2.4** → Framework web
- **Thymeleaf** → Motor de plantillas HTML
- **Spring Web** → REST API
- **Jackson** → Serialización JSON

---

## 🎨 Diseño

Ambas versiones comparten el **mismo diseño profesional tipo SaaS**:

- 🎨 Paleta de colores moderna (azul #5B7FFF como primario)
- 📊 Cards con métricas KPI
- ⚡ Animaciones y transiciones suaves
- 📱 Diseño responsive (versión web)
- 🌓 Tipografía Inter/SF Pro Display
- 🎯 Iconografía consistente

---

## 📊 Modelos de Datos

### Producto (`Product`)
- ID, código de barras, nombre, descripción
- Unidad base (tableta, ml, gr, etc.)
- Unidades por caja
- Stock mínimo, estado activo

### Entrada de Stock (`EntradaItem`)
- Producto, cantidad
- Fecha de entrada, lote
- Fecha de vencimiento

### Movimientos
- Tipo (ENTRADA/SALIDA)
- Usuario, producto, lote
- Cantidad, motivo, fecha/hora

---

## 🧪 Testing

```bash
# Ejecutar tests (todos los módulos)
mvn test

# Solo un módulo específico
mvn test -pl farmacia-core
```

---

## 📝 Desarrollo

### Agregar nueva funcionalidad

1. **Lógica de negocio** → Agregar en `farmacia-core`
2. **UI Desktop** → Crear vista en `farmacia-desktop/src/main/java/com/tobias/ui/`
3. **UI Web** → Crear controller en `farmacia-web/src/main/java/com/tobias/web/controller/` y template en `templates/`

### Comandos útiles

```bash
# Compilar solo farmacia-core
mvn clean install -pl farmacia-core

# Compilar farmacia-web y sus dependencias
mvn clean package -pl farmacia-web -am

# Ejecutar sin tests
mvn clean install -DskipTests

# Limpiar todo
mvn clean
```

---

## 🤝 Contribuciones

1. Fork el proyecto
2. Crear una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abrir un Pull Request

---

## 📄 Licencia

Este proyecto está disponible bajo licencia MIT.

---

## 👨‍💻 Autor

**Tobías Choclin**

Portfolio: [Tu URL de Portfolio aquí con la demo]

---

<div align="center">

**Hecho con ❤️ usando Java, Spring Boot y JavaFX**

*Sistema de gestión farmacéutica profesional para escritorio y web*

</div>
