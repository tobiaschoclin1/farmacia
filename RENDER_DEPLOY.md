# Despliegue en Render + Cron Job (anti-sleep)

Guía para desplegar `farmacia-web` en Render (plan gratuito) y reducir cold starts con un ping programado.

## Resumen

- Plan gratuito de Render: el servicio entra en reposo por inactividad.
- Mitigación: ping cada 10 minutos a un endpoint de salud.
- Base de datos recomendada: Neon PostgreSQL (free tier).

## 1) Crear PostgreSQL gratis (Neon)

1. Crea cuenta en https://neon.tech
2. Crea proyecto `farmacia`
3. Copia la connection string

Ejemplo:

```bash
DATABASE_URL=jdbc:postgresql://ep-xxxx.us-east-1.aws.neon.tech/farmacia?sslmode=require
DATABASE_USER=neondb_owner
DATABASE_PASSWORD=tu_password
```

## 2) Crear Web Service en Render

1. Dashboard Render → **New** → **Web Service**
2. Conecta tu repositorio GitHub
3. Configuración:
   - **Environment**: Docker
   - **Dockerfile Path**: `./Dockerfile`
   - **Branch**: `main`
4. Variables de entorno:
   - `DATABASE_URL`
   - `DATABASE_USER`
   - `DATABASE_PASSWORD`
   - `JAVA_OPTS=-Xmx512m -Xms256m`
5. Deploy

> El servicio quedará accesible en una URL tipo `https://farmacia-web.onrender.com`.

## 3) Configurar ping anti-sleep (GitHub Actions)

Este repo incluye workflow en `.github/workflows/keep-render-awake.yml`.

Pasos:

1. Ve a GitHub → **Settings** → **Secrets and variables** → **Actions**
2. Crea secret: `RENDER_HEALTHCHECK_URL`
3. Valor recomendado:

```text
https://TU-APP.onrender.com/api/dashboard/kpis
```

El workflow hará `curl` cada 10 minutos para minimizar reposos.

## 4) Verificación

```bash
curl https://TU-APP.onrender.com/api/dashboard/kpis
```

Si devuelve JSON, el despliegue está operativo.

## Troubleshooting rápido

- **Build falla**: revisa logs de Render y que el `Dockerfile` sea `./Dockerfile`.
- **Error DB**: valida `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`.
- **Cold start ocasional**: normal en free tier; el ping reduce, no elimina al 100%.
