# GitHub Actions - Keep Render Awake

## ¿Qué hace?

El workflow **Keep Render Awake** hace ping a tu servicio de Render cada 10 minutos para evitar que entre en modo sleep (los servicios gratuitos de Render se duermen después de 15 minutos de inactividad).

## Verificar que está activo

1. Ve a tu repositorio en GitHub
2. Click en la pestaña **Actions**
3. Deberías ver el workflow "Keep Render Awake"
4. Si aparece un punto amarillo ⚠️, significa que está esperando
5. Si aparece check verde ✅, significa que funciona correctamente

## Ejecutar manualmente (para probar)

1. Ve a **Actions** en GitHub
2. Click en "Keep Render Awake" en el menú izquierdo
3. Click en el botón **"Run workflow"** (botón azul a la derecha)
4. Selecciona la rama `main`
5. Click en **"Run workflow"** verde
6. Espera 10-15 segundos y actualiza la página
7. Deberías ver una nueva ejecución con estado "running" o "completed"

## Frecuencia

- **Cron schedule**: `*/10 * * * *` (cada 10 minutos)
- **Endpoint**: `https://farmacia-2xm1.onrender.com/api/dashboard/kpis`
- **Timeout**: 30 segundos

## Troubleshooting

### El workflow no aparece en Actions

**Causa**: GitHub Actions puede estar desactivado en el repositorio

**Solución**:
1. Settings → Actions → General
2. Asegúrate de que "Allow all actions" esté seleccionado
3. Click "Save"

### El workflow falla

**Causa**: La URL del servicio puede estar incorrecta o el servicio no arrancó

**Solución**:
1. Verifica que `https://farmacia-2xm1.onrender.com/api/dashboard/kpis` responda en tu navegador
2. Si el deploy falló en Render, arregla el deploy primero
3. Una vez que Render esté funcionando, el workflow funcionará automáticamente

### ¿Cómo sé si está funcionando?

1. Ve a **Actions** en GitHub
2. Deberías ver ejecuciones cada 10 minutos
3. Click en cualquier ejecución
4. Click en "Ping Render service"
5. Deberías ver: `✅ Service is awake (HTTP 200)`

## Alternativas si no funciona

Si GitHub Actions no funciona por alguna razón, puedes usar servicios externos de cron:

- **UptimeRobot** (https://uptimerobot.com/) - Gratis, hace ping cada 5 minutos
- **Cron-job.org** (https://cron-job.org/) - Gratis, configurable
- **BetterUptime** (https://betteruptime.com/) - Gratis con límites

Configura cualquiera de estos para hacer GET request a:
```
https://farmacia-2xm1.onrender.com/api/dashboard/kpis
```

## Notas importantes

- ⚠️ **Render free tier tiene límite de horas mensuales** (~750 horas/mes)
- Este workflow solo evita el sleep por inactividad, no extiende el límite mensual
- Si llegas al límite mensual, el servicio se detendrá hasta el próximo mes
- Considera actualizar a plan de pago si necesitas 100% uptime
