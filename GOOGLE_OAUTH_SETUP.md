# Configuración de Google OAuth en Farmacia

Este documento explica cómo configurar la autenticación con Google en la aplicación Farmacia.

## Requisitos

- Una cuenta de Google
- Acceso a Google Cloud Console
- Java 21+
- PostgreSQL (o SQLite para desarrollo)

## Pasos para configurar Google OAuth

### 1. Crear un Proyecto en Google Cloud Console

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la API de Google+ (opcional pero recomendado)

### 2. Configurar la Pantalla de Consentimiento OAuth

1. En el menú lateral, ve a **APIs y servicios** > **Pantalla de consentimiento de OAuth**
2. Selecciona **Externo** (o **Interno** si es para una organización Google Workspace)
3. Completa la información requerida:
   - Nombre de la aplicación: `Farmacia`
   - Correo electrónico de asistencia
   - Dominios autorizados (si aplica)
4. Agrega los scopes necesarios:
   - `userinfo.email`
   - `userinfo.profile`
5. Guarda y continúa

### 3. Crear Credenciales OAuth 2.0

1. Ve a **APIs y servicios** > **Credenciales**
2. Haz clic en **Crear credenciales** > **ID de cliente de OAuth 2.0**
3. Selecciona **Aplicación web**
4. Configura:
   - **Nombre**: `Farmacia Web Client`
   - **Orígenes de JavaScript autorizados**:
     - `http://localhost:8080` (desarrollo)
     - `https://tu-dominio.com` (producción)
   - **URIs de redireccionamiento autorizados**:
     - `http://localhost:8080/login/oauth2/code/google` (desarrollo)
     - `https://tu-dominio.com/login/oauth2/code/google` (producción)
5. Haz clic en **Crear**
6. Copia el **Client ID** y **Client Secret**

### 4. Configurar Variables de Entorno

Crea o actualiza tu archivo `.env` local con las siguientes variables:

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/farmacia
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres

# Google OAuth
GOOGLE_CLIENT_ID=tu-client-id-de-google.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu-client-secret-de-google
```

### 5. Verificar Migración de Base de Datos

La tabla de usuarios debe tener la columna `google_id` para almacenar el ID de Google del usuario. 
Esta columna ya está incluida en las migraciones de Flyway.

Si necesitas verificar, ejecuta:

```sql
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'usuarios' AND column_name = 'google_id';
```

### 6. Compilar y Ejecutar la Aplicación

```bash
# Desde el directorio raíz del proyecto
mvn clean install

# Ejecutar el módulo web
cd farmacia-web
mvn spring-boot:run
```

O usando Docker:

```bash
docker-compose up
```

## Uso

Una vez configurado, los usuarios podrán:

1. Hacer clic en "Continuar con Google" en las páginas de login o registro
2. Autenticarse con su cuenta de Google
3. Ser redirigidos automáticamente al dashboard

## Flujo de Autenticación

1. **Nuevo usuario con Google**: Se crea automáticamente una cuenta nueva vinculada a su email de Google
2. **Usuario existente**: Si el email ya existe en la base de datos, se vincula la cuenta con el Google ID
3. **Inicio de sesión**: Los usuarios pueden iniciar sesión con credenciales tradicionales o con Google

## Producción

Para producción, asegúrate de:

1. Configurar las variables de entorno `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` en tu servidor
2. Agregar los URIs de producción a las credenciales de Google OAuth
3. Usar HTTPS (requerido por Google para producción)
4. Considerar cambiar el tipo de audiencia de OAuth a "Interno" si es para una organización

### Configuración en Koyeb/Render/Fly.io

Agrega las siguientes variables de entorno en el panel de tu proveedor:

```
GOOGLE_CLIENT_ID=tu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu-client-secret
DATABASE_URL=jdbc:postgresql://...
DATABASE_USER=...
DATABASE_PASSWORD=...
```

## Solución de Problemas

### Error: "redirect_uri_mismatch"
- Verifica que el URI de redirección en Google Cloud Console coincida exactamente con `{TU_DOMINIO}/login/oauth2/code/google`
- Asegúrate de incluir tanto el dominio de desarrollo como el de producción

### Error: "Invalid client_id"
- Verifica que `GOOGLE_CLIENT_ID` esté configurado correctamente
- Asegúrate de que las credenciales no estén vencidas o revocadas

### Error: "Column google_id does not exist"
- Ejecuta las migraciones de Flyway: `mvn flyway:migrate`
- Verifica que la base de datos esté actualizada

### El usuario no se crea después de autenticarse con Google
- Verifica los logs de la aplicación para ver errores específicos
- Asegúrate de que `OAuth2LoginSuccessHandler` esté correctamente configurado
- Verifica que los métodos `registrarConGoogle` y `vincularGoogleId` existan en `AuthService`

## Arquitectura

### Componentes Clave

1. **SecurityConfig.java**: Configura Spring Security con OAuth2
2. **OAuth2LoginSuccessHandler.java**: Maneja el callback de Google y crea/vincula usuarios
3. **AuthService.java**: Lógica de negocio para registro y autenticación
4. **UsuarioDao.java**: Acceso a datos de usuarios
5. **application.properties**: Configuración de Google OAuth2 Client

### Base de Datos

Tabla `usuarios`:
- `id`: Primary key
- `nombre`: Nombre del usuario
- `email`: Email (único)
- `password`: Hash de contraseña (nullable para usuarios OAuth)
- `google_id`: ID de Google (nullable para usuarios tradicionales)
- `rol`: Rol del usuario (USUARIO, ADMINISTRADOR)
- `activo`: Estado de la cuenta
- `fecha_creacion`: Timestamp de creación
- `ultimo_acceso`: Timestamp de último acceso

## Más Información

- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Spring Boot OAuth2 Guide](https://spring.io/guides/tutorials/spring-boot-oauth2)
