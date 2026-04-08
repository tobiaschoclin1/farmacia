# 💊 Farmacia - Sistema de Gestión de Stock

<div align="center">

**Aplicación de escritorio para gestionar el inventario de una farmacia**

[![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-22.0.1-4B8BBE?style=flat-square)](https://gluonhq.com/products/javafx/)
[![SQLite](https://img.shields.io/badge/SQLite-3.46+-003B57?style=flat-square&logo=sqlite)](https://www.sqlite.org/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=flat-square&logo=maven)](https://maven.apache.org/)

</div>

---

## 🎯 Características Principales

| Funcionalidad | Descripción |
|---|---|
| 📦 **Gestión de Productos** | Agregar, editar y eliminar productos del catálogo |
| 📥 **Entrada de Stock** | Registrar entrada de nuevos productos y lotes |
| 📤 **Salida de Stock** | Controlar salidas y consumo de productos |
| 📊 **Vista de Stock** | Visualizar stock actual de todos los productos |
| ⏰ **Control de Vencimiento** | Monitorear fechas de expiración |
| 💾 **Import/Export** | Importar y exportar datos en Excel |

---

## 🛠️ Tecnologías

```
Java 21              → Lenguaje principal
├── JavaFX 22        → Interfaz gráfica de escritorio
├── SQLite 3.46      → Base de datos
├── Flyway 10.15     → Migraciones de BD
├── Apache POI 5.2.5 → Manejo de Excel
└── SLF4J 2.0.13     → Logging
```

---

## 📁 Estructura del Proyecto

```
farmacia/
├── pom.xml                          # Configuración Maven
├── src/
│   └── main/
│       ├── java/com/tobias/
│       │   ├── App.java             # Punto de entrada
│       │   ├── dao/                 # Acceso a datos
│       │   ├── db/                  # Configuración BD
│       │   ├── model/               # Modelos de datos
│       │   ├── service/             # Lógica de negocios
│       │   ├── ui/                  # Vistas JavaFX
│       │   └── util/                # Utilidades
│       └── resources/
│           ├── db/migration/        # Scripts SQL de Flyway
│           └── ui/                  # Recursos de UI (CSS)
└── .git/                            # Control de versiones
```

---

## 🚀 Guía de Inicio Rápido

### Requisitos Previos
- **Java 21+** instalado
- **Maven 3.8+** instalado
- **Git** para control de versiones

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <url-del-repositorio>
   cd farmacia
   ```

2. **Compilar el proyecto**
   ```bash
   mvn clean compile
   ```

3. **Ejecutar la aplicación**
   ```bash
   mvn javafx:run
   ```

---

## 📋 Modelos de Datos

### Producto (`Product`)
- ID, nombre, descripción
- Precio unitario, stock actual
- Lote de entrada, fecha de vencimiento

### Entrada de Stock (`EntradaItem`)
- Producto, cantidad
- Fecha de entrada, lote
- Información del proveedor

### Movimientos
- Registro de entradas y salidas
- Trazabilidad completa del stock

---

## 🎨 Interfaz de Usuario

La aplicación utiliza un sistema de **pestañas** para navegar entre vistas:

- 🏠 **Inicio** - Dashboard principal
- 📦 **Productos** - Catálogo de productos
- 📥 **Entrada de Stock** - Registrar entrada de mercancía
- 📤 **Salida de Stock** - Registrar consumo/venta
- 📊 **Stock** - Estado actual del inventario
- ⏰ **Vencimientos** - Monitoreo de fechas
- 💾 **Import/Export** - Gestión de archivos Excel

---

## 💻 Desarrollo

### Compilar
```bash
mvn clean compile
```

### Ejecutar tests
```bash
mvn test
```

### Generar JAR ejecutable
```bash
mvn clean package
```

### Limpiar archivos compilados
```bash
mvn clean
```

---

## 📊 Base de Datos

La aplicación utiliza **SQLite** con migraciones gestionadas por **Flyway**:

```
V1__init.sql                  → Estructura inicial
V2__lote_como_fecha.sql       → Agregación de lotes
V3__movimientos_stock.sql     → Tabla de movimientos
```

La BD se almacena en: `%ProgramData%/Farmacia/data/farmacia.db` (Windows)

---

## 📝 Logging

Configurado con **SLF4J** para debugging y auditoría de operaciones críticas.

---

## 🤝 Contribuciones

Para contribuir:
1. Crear una rama (`git checkout -b feature/nueva-caracteristica`)
2. Hacer cambios y commits
3. Hacer push a la rama
4. Abrir un Pull Request

---

## 📄 Licencia

Este proyecto está disponible bajo licencia MIT.

---

<div align="center">

**Hecho con ❤️ por Tobías**

*Sistema de gestión farmacéutica inteligente*

</div>
