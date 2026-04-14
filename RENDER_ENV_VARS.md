# Variables de Entorno para Render

## ⚠️ IMPORTANTE: NO configurar JAVA_OPTS en Render

El contenedor maneja automáticamente las opciones de JVM. Si configuras `JAVA_OPTS` en Render, podría causar errores de parsing.

## Variables REQUERIDAS

Configura estas variables en el dashboard de Render (Environment → Environment Variables):

### Base de Datos PostgreSQL

```bash
DATABASE_URL=jdbc:postgresql://tu-host.neon.tech:5432/farmacia?sslmode=require
DATABASE_USER=tu_usuario
DATABASE_PASSWORD=tu_password_segura
```

### Puerto (Opcional)

Render configura esto automáticamente, pero si necesitas cambiarlo:

```bash
PORT=8080
```

## Variables OPCIONALES

Si necesitas ajustar la memoria JVM (solo en casos especiales):

```bash
# Ejemplo: limitar a 512MB
JAVA_OPTS=-Xmx512m -Xms256m
```

**NOTA:** Solo configura `JAVA_OPTS` si tienes problemas de memoria. Por defecto usa 75% de RAM disponible.

## Ejemplo de Configuración Completa

Para PostgreSQL en Neon (gratis):

1. Ve a https://neon.tech y crea un proyecto
2. Copia la connection string
3. En Render, agrega:

```
DATABASE_URL=jdbc:postgresql://ep-xxx-xxx.us-east-2.aws.neon.tech:5432/neondb?sslmode=require
DATABASE_USER=usuario_neon
DATABASE_PASSWORD=password_neon
```

## Troubleshooting

### Error: "Could not find or load main class Xmx512m"

- **Causa:** Variable `JAVA_OPTS` mal configurada en Render
- **Solución:** ELIMINA la variable `JAVA_OPTS` del dashboard de Render (el contenedor la maneja automáticamente)

### Error: "Connection refused" o "Connection timeout"

- **Causa:** DATABASE_URL incorrecta o falta `?sslmode=require`
- **Solución:** Verifica que la URL tenga el sufijo `?sslmode=require` para PostgreSQL en Neon/Supabase

### Error: Flyway migration failed

- **Causa:** Usuario sin permisos o base de datos no inicializada
- **Solución:** Verifica que el usuario PostgreSQL tenga permisos CREATE TABLE
