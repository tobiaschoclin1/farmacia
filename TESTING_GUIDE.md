# 🧪 Guía Completa de Testing - Sistema Farmacia

Esta guía te llevará paso a paso por el proceso de testing completo del sistema, tanto funcional como de UX/UI.

---

## 📋 Índice

1. [Preparación](#1-preparación)
2. [Testing Funcional](#2-testing-funcional)
3. [Testing de UX/UI](#3-testing-de-uxui)
4. [Testing de Rendimiento](#4-testing-de-rendimiento)
5. [Checklist Final](#5-checklist-final)

---

## 1. Preparación

### 1.1 Configurar Entorno de Testing

```bash
# Abrir terminal
cd /Users/tchoclin/Documents/Farmacia

# Verificar que todo compile
mvn clean install

# Levantar aplicación en modo desarrollo
cd farmacia-web
mvn spring-boot:run

# Esperar mensaje: "Started FarmaciaWebApplication"
# Abrir navegador en: http://localhost:8080
```

### 1.2 Herramientas Necesarias

- ✅ **Navegador:** Chrome/Firefox con DevTools
- ✅ **Postman:** Para testing de API (opcional)
- ✅ **Spreadsheet:** Para documentar bugs encontrados
- ✅ **Screenshot tool:** Para capturar errores

### 1.3 Crear Documento de Testing

Crea un archivo `testing-results.txt` o usa una spreadsheet con estas columnas:

| ID | Funcionalidad | Pasos | Resultado Esperado | Resultado Real | Estado | Screenshot |
|----|--------------|-------|-------------------|----------------|---------|------------|

---

## 2. Testing Funcional

### TEST 1: Splash Screen y Redirección

**Objetivo:** Verificar que la splash screen funciona correctamente

**Pasos:**
1. Abrir navegador en modo incógnito
2. Ir a `http://localhost:8080/`
3. ⏱️ Esperar 2.5 segundos

**Resultado Esperado:**
- ✅ Se muestra splash con logo de farmacia
- ✅ Texto "Farmacia" y "Sistema de Gestión"
- ✅ Loading dots animados
- ✅ Logo flota suavemente
- ✅ Después de 2.5s redirige a `/registro`

**Posibles Errores:**
- ❌ Logo con fondo blanco → Problema de transparencia PNG
- ❌ No redirige → Revisar JavaScript en splash.html
- ❌ Redirige a `/dashboard` → Hay sesión activa (probar en incógnito)

---

### TEST 2: Registro de Usuario

**Objetivo:** Crear una cuenta nueva

**Pasos:**
1. En `/registro`, llenar formulario:
   - **Nombre:** Test Usuario
   - **Email:** test@farmacia.com
   - **Contraseña:** Test1234
   - **Confirmar:** Test1234
2. Click "Crear Cuenta"

**Resultado Esperado:**
- ✅ Redirige a `/login` con mensaje "Cuenta creada exitosamente"
- ✅ No muestra errores

**Casos Edge:**

**TEST 2.1:** Contraseñas no coinciden
- Contraseña: Test1234
- Confirmar: Test4321
- **Esperado:** Mensaje de error "Las contraseñas no coinciden"

**TEST 2.2:** Contraseña muy corta
- Contraseña: Test1 (6 caracteres)
- **Esperado:** Error "La contraseña debe tener al menos 8 caracteres"

**TEST 2.3:** Email duplicado
- Registrar: test@farmacia.com (segunda vez)
- **Esperado:** Error "El email ya está registrado"

**TEST 2.4:** Email inválido
- Email: test@invalido
- **Esperado:** Error de validación de email

---

### TEST 3: Login

**Objetivo:** Iniciar sesión con credenciales válidas

**Pasos:**
1. Ir a `/login`
2. Ingresar:
   - Email: test@farmacia.com
   - Contraseña: Test1234
3. Click "Iniciar Sesión"

**Resultado Esperado:**
- ✅ Redirige a `/dashboard`
- ✅ Sidebar muestra nombre "Test Usuario" y rol "USUARIO"
- ✅ Avatar muestra iniciales "TU"

**Casos Edge:**

**TEST 3.1:** Credenciales incorrectas
- Email: test@farmacia.com
- Contraseña: Wrong123
- **Esperado:** Error "Email o contraseña incorrectos"

**TEST 3.2:** Email no existe
- Email: noexiste@farmacia.com
- **Esperado:** Error de autenticación

**TEST 3.3:** Admin Login
- Email: admin@farmacia.com
- Contraseña: admin123
- **Esperado:** Login exitoso con rol "ADMINISTRADOR"

---

### TEST 4: Dashboard

**Objetivo:** Verificar que el dashboard carga correctamente

**Pasos:**
1. Después del login, verificar `/dashboard`

**Resultado Esperado:**
- ✅ Muestra 4 cards con KPIs:
  - Total Productos
  - Productos Activos
  - Stock Bajo
  - Próximos a Vencer
- ✅ Los números son correctos (según BD)
- ✅ Sidebar muestra usuario logueado
- ✅ Todos los links de navegación funcionan

**Verificar:**
- Dashboard link (activo)
- Productos link
- Stock link
- Vencimientos link
- Entradas link
- Salidas link
- Configuración link
- Perfil link (footer sidebar)

---

### TEST 5: Crear Producto

**Objetivo:** Crear un producto nuevo desde la interfaz

**Pasos:**
1. Ir a `/productos`
2. Click botón "Nuevo Producto"
3. Llenar formulario:
   - **Nombre:** Ibuprofeno 600mg
   - **Código de Barras:** 7798140250234
   - **Unidad Base:** Tableta
   - **Unidades por Caja:** 30
   - **Stock Mínimo:** 10
4. Click "Crear Producto"

**Resultado Esperado:**
- ✅ Modal se cierra
- ✅ Mensaje "Producto creado exitosamente"
- ✅ Tabla de productos se recarga automáticamente
- ✅ El nuevo producto aparece en la lista

**Casos Edge:**

**TEST 5.1:** Nombre vacío
- Nombre: (vacío)
- **Esperado:** Error de validación "Campo requerido"

**TEST 5.2:** Stock mínimo negativo
- Stock Mínimo: -5
- **Esperado:** Error "Debe ser mayor o igual a 0"

**TEST 5.3:** Solo nombre (campos opcionales vacíos)
- Nombre: Paracetamol
- Código: (vacío)
- Unidades por Caja: (vacío)
- **Esperado:** Producto creado exitosamente con valores NULL

---

### TEST 6: Búsqueda de Productos

**Objetivo:** Buscar productos por nombre o código

**Pasos:**
1. En `/productos`, escribir en campo de búsqueda: "Ibuprofeno"
2. Click "Buscar"

**Resultado Esperado:**
- ✅ Muestra solo productos que contienen "Ibuprofeno" en el nombre
- ✅ Búsqueda es case-insensitive (IBUPROFENO, ibuprofeno, Ibuprofeno)

**TEST 6.1:** Búsqueda por código
- Buscar: 7798140250234
- **Esperado:** Muestra el producto con ese código

**TEST 6.2:** Sin resultados
- Buscar: XXXXX
- **Esperado:** Tabla vacía o mensaje "No se encontraron productos"

---

### TEST 7: Mi Perfil

**Objetivo:** Verificar que se muestran los datos del usuario correcto

**Pasos:**
1. Click en el perfil del usuario (footer sidebar)
2. Ir a `/usuario`

**Resultado Esperado:**
- ✅ Avatar grande muestra iniciales correctas (TU)
- ✅ Nombre: "Test Usuario"
- ✅ Email: test@farmacia.com
- ✅ Rol: USUARIO (disabled)
- ✅ Todos los campos están llenos con datos del usuario logueado

**TEST 7.1:** Actualizar perfil
1. Cambiar nombre a "Test Usuario Modificado"
2. Click "Guardar Cambios"
- **Esperado:** Mensaje "Perfil actualizado"
- **Esperado:** Sidebar actualiza el nombre

**TEST 7.2:** Cambiar contraseña
1. Contraseña Actual: Test1234
2. Nueva: NewPass123
3. Confirmar: NewPass123
4. Click "Cambiar Contraseña"
- **Esperado:** Mensaje "Contraseña cambiada correctamente"
- **Esperado:** Formulario de contraseña se resetea

---

### TEST 8: Sesión y Navegación

**Objetivo:** Verificar que la sesión persiste correctamente

**Pasos:**
1. Estando logueado, navegar a:
   - Dashboard
   - Productos
   - Stock
   - Vencimientos
2. En cada página, verificar que:
   - ✅ Sidebar muestra el usuario correcto
   - ✅ No redirige a login
   - ✅ Avatar y nombre son consistentes

**TEST 8.1:** Acceso sin sesión
1. Abrir navegador en incógnito
2. Ir directo a `/dashboard`
- **Esperado:** Redirige a `/login`

**TEST 8.2:** Múltiples usuarios
1. Registrar segundo usuario: pedro@farmacia.com
2. Login con pedro@farmacia.com
3. Verificar que sidebar muestra "Pedro Lopez" (no "Test Usuario")

---

### TEST 9: API Endpoints

**Objetivo:** Verificar que la API REST funciona correctamente

**Usando Postman o curl:**

**TEST 9.1:** GET Productos
```bash
curl http://localhost:8080/api/productos
```
**Esperado:** JSON array con productos

**TEST 9.2:** GET KPIs
```bash
curl http://localhost:8080/api/dashboard/kpis
```
**Esperado:** JSON con:
```json
{
  "totalProductos": 10,
  "productosActivos": 9,
  "stockBajo": 2,
  "proximosVencer": 1
}
```

**TEST 9.3:** POST Producto
```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Aspirina 500mg",
    "codigoBarra": "1234567890",
    "unidadBase": "TABLETA",
    "stockMinimo": 5,
    "activo": true
  }'
```
**Esperado:** HTTP 200, producto creado

---

## 3. Testing de UX/UI

### TEST UI-1: Diseño Visual

**Objetivo:** Verificar que el diseño es consistente y profesional

**Checklist Visual:**
- [ ] ✅ Logo se ve correctamente (sin fondo blanco)
- [ ] ✅ Colores consistentes (crema, azul, verde)
- [ ] ✅ Tipografía legible (Inter, SF Pro)
- [ ] ✅ Botones tienen hover effects
- [ ] ✅ Forms tienen focus states (border azul)
- [ ] ✅ Cards tienen sombras suaves
- [ ] ✅ Espaciado consistente
- [ ] ✅ Iconos SVG se renderizan correctamente

---

### TEST UI-2: Responsive Design

**Objetivo:** Verificar que funciona en diferentes tamaños de pantalla

**Pasos:**
1. Abrir DevTools (F12)
2. Click en Toggle Device Toolbar (Ctrl+Shift+M)
3. Probar en:
   - iPhone SE (375x667)
   - iPad (768x1024)
   - Desktop (1920x1080)

**Verificar en cada tamaño:**
- [ ] Sidebar se muestra correctamente (o collapse en mobile)
- [ ] Forms son usables
- [ ] Tablas tienen scroll horizontal si necesario
- [ ] Texto no se corta
- [ ] Botones son clicables
- [ ] Cards se reorganizan bien

---

### TEST UI-3: Animaciones

**Objetivo:** Verificar que las animaciones son suaves

**Verificar:**
- [ ] Splash screen: logo flota suavemente
- [ ] Splash screen: loading dots bouncean
- [ ] Modal productos: aparece con fadeIn
- [ ] Hover en botones: transición suave
- [ ] Focus en inputs: border aparece smooth

---

### TEST UI-4: Usabilidad

**Objetivo:** Verificar que la experiencia es intuitiva

**Tareas de Usuario Real:**

**Tarea 1:** "Quiero agregar un nuevo producto"
- ¿Es fácil encontrar el botón "Nuevo Producto"?
- ¿El formulario es claro?
- ¿Se entiende qué campos son obligatorios?

**Tarea 2:** "Quiero buscar un producto por nombre"
- ¿El campo de búsqueda es visible?
- ¿El botón "Buscar" es obvio?
- ¿Los resultados son claros?

**Tarea 3:** "Quiero cambiar mi contraseña"
- ¿Puedo encontrar dónde hacerlo?
- ¿El formulario es claro?
- ¿Hay feedback cuando lo hago?

---

### TEST UI-5: Accesibilidad Básica

**Objetivo:** Verificar navegación por teclado

**Pasos:**
1. En `/login`, usar solo teclado:
   - Tab para navegar entre campos
   - Enter para submit
2. Verificar:
   - [ ] Focus visible en cada campo
   - [ ] Orden de Tab lógico
   - [ ] Enter submite formulario
   - [ ] Escape cierra modals

---

### TEST UI-6: Mensajes de Error

**Objetivo:** Verificar que los errores son claros

**Verificar que se muestran mensajes útiles:**
- [ ] Login fallido: "Email o contraseña incorrectos"
- [ ] Registro fallido: mensaje específico del error
- [ ] Validación de forms: mensajes en español y claros
- [ ] Errores de servidor: mensaje genérico amigable

---

## 4. Testing de Rendimiento

### TEST PERF-1: Tiempo de Carga

**Objetivo:** Verificar que las páginas cargan rápido

**Usando DevTools Network:**
1. F12 → Network
2. Recargar página
3. Verificar:
   - [ ] Dashboard carga en < 2 segundos
   - [ ] Login/Registro en < 1 segundo
   - [ ] API /api/productos en < 500ms

---

### TEST PERF-2: Tamaño de Assets

**Verificar:**
- [ ] Logo PNG < 50KB
- [ ] CSS principal < 100KB
- [ ] JS total < 50KB (solo session.js)

---

## 5. Checklist Final

### ✅ Funcionalidades Core

- [ ] Splash screen funciona
- [ ] Registro de usuarios funciona
- [ ] Login funciona
- [ ] Logout funciona (si existe)
- [ ] Dashboard muestra KPIs correctos
- [ ] Crear producto funciona
- [ ] Listar productos funciona
- [ ] Buscar productos funciona
- [ ] Perfil de usuario muestra datos correctos
- [ ] Sesión persiste entre páginas
- [ ] Múltiples usuarios funcionan correctamente

### ✅ UX/UI

- [ ] Diseño es profesional y consistente
- [ ] Logo se ve sin fondo blanco
- [ ] Animaciones son suaves
- [ ] Formularios son claros
- [ ] Mensajes de error son útiles
- [ ] Responsive design funciona
- [ ] Navegación es intuitiva

### ✅ Render Deployment

- [ ] App está desplegada en Render
- [ ] Base de datos PostgreSQL conecta
- [ ] UptimeRobot está configurado
- [ ] Credenciales admin funcionan en producción
- [ ] API endpoints responden en producción

---

## 📝 Plantilla de Reporte de Bug

```markdown
**ID:** BUG-001
**Fecha:** 2024-04-XX
**Severidad:** Alta/Media/Baja
**Módulo:** Login/Productos/Dashboard/etc.

**Descripción:**
[Descripción clara del problema]

**Pasos para Reproducir:**
1. Ir a /login
2. Ingresar email incorrecto
3. ...

**Resultado Esperado:**
[Qué debería pasar]

**Resultado Actual:**
[Qué pasa realmente]

**Screenshot:**
[Adjuntar imagen si aplica]

**Navegador:**
Chrome 120.0 / Firefox 121.0

**Solución Propuesta:**
[Si tienes idea de cómo arreglarlo]
```

---

## 🎯 Siguientes Pasos

Después de completar todos los tests:

1. **Documentar bugs encontrados**
2. **Priorizar por severidad** (Alta → Media → Baja)
3. **Arreglar bugs críticos primero**
4. **Re-testear después de cada fix**
5. **Marcar como completado cuando todo pase**

---

## ✅ Cuando TODO pasa...

¡Felicidades! Tu sistema está listo para producción 🚀

