# Reparar Estado de Flyway

## Problema

V6__usuarios.sql falló porque V1 creó la tabla `usuarios` con una estructura antigua (columna `username` en lugar de `email`). Flyway registró el fallo y no reintentará la migración automáticamente.

## Solución: Limpiar la base de datos PostgreSQL

### Opción 1: Conectarte a Neon y limpiar manualmente (RECOMENDADO)

**Paso 1: Conectarte a tu base de datos Neon**

Ve a https://console.neon.tech → tu proyecto → Connection Details

```bash
# Conéctate usando psql o cualquier cliente PostgreSQL
psql "postgresql://usuario:password@host.neon.tech/neondb?sslmode=require"
```

**Paso 2: Eliminar todas las tablas y el historial de Flyway**

```sql
-- Ver qué migraciones están registradas
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Eliminar TODAS las tablas (esto borra todos los datos!)
DROP TABLE IF EXISTS movimientos_stock CASCADE;
DROP TABLE IF EXISTS stock_lote CASCADE;
DROP TABLE IF EXISTS lotes CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP VIEW IF EXISTS v_stock_producto CASCADE;

-- Eliminar el historial de Flyway para que empiece de cero
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
```

**Paso 3: Redeploy en Render**

Una vez limpia la BD, Render hará redeploy automáticamente y Flyway ejecutará todas las migraciones desde cero con la estructura correcta.

---

### Opción 2: Crear nueva base de datos en Neon (MÁS FÁCIL)

Si preferís empezar de cero:

1. En Neon console, crea una **nueva base de datos**
2. Copia las nuevas credenciales
3. En Render → Environment Variables → actualiza `DATABASE_URL` con la nueva connection string
4. Render redeployará automáticamente

---

## Después del fix

Una vez que la BD esté limpia, las migraciones se ejecutarán en este orden:

1. **V1** - Crea tablas productos, lotes, stock_lote, movimientos (SIN usuarios)
2. **V2** - Ajusta campo lote
3. **V3** - Ajusta movimientos_stock
4. **V4** - Datos de prueba
5. **V5** - Lotes con vencimiento próximo
6. **V6** - Crea tabla usuarios con estructura correcta (DROP + CREATE)

---

## Verificación

Una vez deployado exitosamente, verifica:

```bash
curl https://tu-app.onrender.com/api/dashboard/kpis
```

Deberías ver JSON con los KPIs del sistema.
