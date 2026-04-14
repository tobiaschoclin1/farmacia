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

## 🌍 Deployment Web Gratis (Render + Cron Job)

### ⏱️ Tiempo: 15-20 minutos | Costo: $0

**Qué esperar:**
- ✅ Plan gratuito
- ✅ Deploy simple con Docker
- ⚠️ El servicio puede dormir por inactividad
- ✅ Mitigación con ping cada 10 minutos

### Guía Rápida

**1. PostgreSQL Gratuito (Neon):**
   - Ve a https://neon.tech/ → Sign up
   - Crea proyecto "farmacia"
   - Copia la CONNECTION STRING

**2. Deploy en Render:**
   - Ve a https://render.com/ → Sign up
   - Click "New" → "Web Service"
   - Conecta repositorio Farmacia
   - Environment: `Docker`
   - Dockerfile path: `./Dockerfile`
   - Agrega env vars (DATABASE_URL, etc)
   - Deploy

**3. Anti-sleep con cron:**
   - En GitHub crea secret `RENDER_HEALTHCHECK_URL`
   - Valor: `https://tu-app.onrender.com/api/dashboard/kpis`
   - El workflow `.github/workflows/keep-render-awake.yml` hace ping cada 10 min

**4. ¡Listo!**
   - Espera 2-3 minutos
   - Accede a la URL que Render te asigna

📖 **Guía paso a paso completa:** [RENDER_DEPLOY.md](./RENDER_DEPLOY.md)

---

## Alternativas

⚠️ **Nota:** Fly.io y Koyeb pueden requerir pago/tarjeta según plan.  
Para $0, usar Render free + ping anti-sleep.

---

## 📦 Estructura de Archivos

```
Farmacia/
├── pom.xml                     # Maven parent
├── .github/
│   └── workflows/
│       └── keep-render-awake.yml
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
   ├── fly.toml                # Config antigua Fly.io
   ├── README.md
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

### Render deployment falla

- Revisar logs en el dashboard de Render
- Verificar variables: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- Confirmar que el endpoint de ping sea: `/api/dashboard/kpis`
- Forzar redeploy manual desde Render si quedó en estado fallido

---

## 📚 URLs Importantes

- **Código fuente**: `/Users/tchoclin/Documents/Farmacia`
- **Demo web local**: http://localhost:8080
- **Deploy gratuito**: [Render (ver RENDER_DEPLOY.md)](./RENDER_DEPLOY.md)
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **JavaFX Docs**: https://openjfx.io/

---

## ✅ Checklist Pre-Deployment

- [ ] Compilación exitosa: `mvn clean install`
- [ ] Tests pasan: `mvn test`
- [ ] App corre local: `mvn spring-boot:run`
- [ ] Dockerfile funciona: `docker build -f farmacia-web/Dockerfile -t farmacia-web .`
- [ ] Cuenta Render creada (gratuita)
- [ ] Cuenta Neon creada (PostgreSQL gratis)
- [ ] Variables de entorno de base de datos configuradas
- [ ] Secret `RENDER_HEALTHCHECK_URL` configurado en GitHub

---

## 🎯 Próximos Pasos

1. ✅ **Deploying en Render** (gratis + ping anti-sleep)
2. ✅ **Completar vistas faltantes en web** (Entradas, Salidas, Stock, Import/Export)
3. ✅ **Agregar autenticación** (Spring Security)
4. ✅ **Tests unitarios** para controllers y services
5. ✅ **CI/CD con GitHub Actions** (deploy y keep-alive)
6. ✅ **Monitoreo** (métricas con Spring Actuator)
7. ✅ **Dominio personalizado** en Render

---

**¡Listo para producción! 🚀**
