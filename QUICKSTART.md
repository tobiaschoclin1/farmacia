# 🚀 Guía Rápida - Farmacia

## ⚡ Inicio Rápido

### 📥 1. Compilar todo

```bash
cd /Users/tchoclin/Documents/Farmacia
mvn clean install
```

### 💻 2A. Ejecutar versión Desktop (JavaFX)

```bash
cd farmacia-desktop
mvn javafx:run
```

### 🌐 2B. Ejecutar versión Web (Spring Boot)

```bash
cd farmacia-web
mvn spring-boot:run

# Abrir: http://localhost:8080
```

---

## 🌍 Deployment Web (Fly.io)

### Instalación Fly CLI

**macOS:**
```bash
brew install flyctl
```

**Linux/WSL:**
```bash
curl -L https://fly.io/install.sh | sh
```

**Windows (PowerShell):**
```powershell
iwr https://fly.io/install.ps1 -useb | iex
```

### Deploy paso a paso

```bash
# 1. Login (crear cuenta gratis si no tienes)
fly auth login

# 2. Ir al módulo web
cd farmacia-web

# 3. Lanzar app (primera vez)
fly launch --copy-config --name farmacia-demo

# 4. Crear volumen persistente para SQLite
fly volumes create farmacia_data --region gru --size 1

# 5. Deploy
fly deploy

# 6. Abrir en navegador
fly open

# 7. Ver logs en tiempo real
fly logs
```

### Comandos útiles Fly.io

```bash
# Ver status
fly status

# SSH a la máquina
fly ssh console

# Ver métricas
fly dashboard

# Escalar (cambiar RAM/CPU)
fly scale memory 512

# Ver apps
fly apps list

# Destruir app
fly apps destroy farmacia-demo
```

---

## 📦 Estructura de Archivos

```
Farmacia/
├── pom.xml                     # Maven parent
├── farmacia-core/              # Lógica compartida
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/tobias/
│       │   ├── dao/           # ProductDao
│       │   ├── model/         # Product, EntradaItem
│       │   ├── service/       # StockService
│       │   ├── db/            # Db (SQLite)
│       │   └── util/          # ExcelUtil
│       └── resources/
│           └── db/migration/  # V1__init.sql, etc.
│
├── farmacia-desktop/           # App JavaFX
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/tobias/
│       │   ├── App.java       # Main JavaFX
│       │   ├── ui/            # DashboardView, etc.
│       │   └── util/          # AppBus, Icons
│       └── resources/
│           └── ui/
│               └── theme-professional.css
│
└── farmacia-web/               # App Spring Boot
    ├── pom.xml
    ├── Dockerfile
    ├── fly.toml                # Config Fly.io
    └── src/main/
        ├── java/com/tobias/web/
        │   ├── FarmaciaWebApplication.java
        │   └── controller/     # DashboardController, etc.
        └── resources/
            ├── static/css/     # theme-professional.css
            ├── templates/      # index.html, etc.
            └── application.properties
```

---

## 🔧 Solución de Problemas

### Desktop no arranca

```bash
# Verificar Java 21+
java -version

# Limpiar y recompilar
cd farmacia-desktop
mvn clean javafx:run
```

### Web no arranca

```bash
# Verificar puerto 8080 libre
lsof -i :8080

# Cambiar puerto
export PORT=8081
mvn spring-boot:run
```

### Error de compilación

```bash
# Limpiar todo
mvn clean

# Compilar sin tests
mvn install -DskipTests
```

### Fly.io deployment falla

```bash
# Ver logs detallados
fly logs

# SSH a la máquina
fly ssh console

# Verificar volumen existe
fly volumes list

# Reiniciar app
fly apps restart farmacia-demo
```

---

## 📚 URLs Importantes

- **Código fuente**: `/Users/tchoclin/Documents/Farmacia`
- **Demo web local**: http://localhost:8080
- **Demo online**: https://farmacia-demo.fly.dev
- **API**: https://farmacia-demo.fly.dev/api/dashboard/kpis
- **Documentación Fly.io**: https://fly.io/docs
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **JavaFX Docs**: https://openjfx.io/

---

## ✅ Checklist Pre-Deployment

- [ ] Compilación exitosa: `mvn clean install`
- [ ] Tests pasan: `mvn test`
- [ ] App corre local: `mvn spring-boot:run`
- [ ] Dockerfile funciona: `docker build -t farmacia-web .`
- [ ] Cuenta Fly.io creada
- [ ] Fly CLI instalado
- [ ] Volumen creado en Fly.io
- [ ] Variables de entorno configuradas (si las hay)

---

## 🎯 Próximos Pasos

1. ✅ **Completar vistas faltantes en web** (Entradas, Salidas, Stock, Import/Export)
2. ✅ **Agregar autenticación** (Spring Security)
3. ✅ **Tests unitarios** para controllers y services
4. ✅ **CI/CD con GitHub Actions** (deploy automático a Fly.io)
5. ✅ **Monitoreo** (métricas con Spring Actuator)
6. ✅ **Dominio personalizado** en Fly.io

---

**¡Listo para producción! 🚀**
