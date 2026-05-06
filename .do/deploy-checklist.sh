#!/bin/bash

# Checklist de pre-deployment para DigitalOcean App Platform
# Verifica que todo está listo antes de hacer deploy

set -e

echo "🔍 Verificando configuración para deployment en DigitalOcean..."
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
ERRORS=0
WARNINGS=0

# Helper functions
check_pass() {
  echo -e "${GREEN}✓${NC} $1"
}

check_fail() {
  echo -e "${RED}✗${NC} $1"
  ((ERRORS++))
}

check_warn() {
  echo -e "${YELLOW}⚠${NC} $1"
  ((WARNINGS++))
}

# 1. Verificar archivo app.yaml
echo "1️⃣  Verificando app.yaml..."
if [ -f ".do/app.yaml" ]; then
  check_pass "Archivo .do/app.yaml existe"

  # Verificar que se actualizó el repo de GitHub
  if grep -q "TU_USUARIO/farmacia" ".do/app.yaml"; then
    check_fail "Actualiza el repo de GitHub en .do/app.yaml (línea con 'repo:')"
  else
    check_pass "Repositorio de GitHub configurado"
  fi
else
  check_fail "Falta archivo .do/app.yaml"
fi
echo ""

# 2. Verificar Dockerfile
echo "2️⃣  Verificando Dockerfile..."
if [ -f "Dockerfile" ]; then
  check_pass "Dockerfile existe"

  # Verificar que usa Java 21
  if grep -q "eclipse-temurin:21" "Dockerfile"; then
    check_pass "Dockerfile usa Java 21"
  else
    check_warn "Dockerfile no usa Java 21 (eclipse-temurin:21)"
  fi
else
  check_fail "Falta Dockerfile"
fi
echo ""

# 3. Verificar pom.xml
echo "3️⃣  Verificando configuración Maven..."
if [ -f "pom.xml" ]; then
  check_pass "pom.xml existe"
else
  check_fail "Falta pom.xml"
fi

if [ -f "farmacia-web/pom.xml" ]; then
  check_pass "farmacia-web/pom.xml existe"

  # Verificar PostgreSQL driver
  if grep -q "postgresql" "farmacia-web/pom.xml"; then
    check_pass "PostgreSQL driver configurado"
  else
    check_fail "Falta PostgreSQL driver en farmacia-web/pom.xml"
  fi
else
  check_fail "Falta farmacia-web/pom.xml"
fi
echo ""

# 4. Verificar application.properties
echo "4️⃣  Verificando application.properties..."
APP_PROPS="farmacia-web/src/main/resources/application.properties"
if [ -f "$APP_PROPS" ]; then
  check_pass "application.properties existe"

  # Verificar configuración PostgreSQL
  if grep -q "postgresql" "$APP_PROPS"; then
    check_pass "PostgreSQL configurado"
  else
    check_warn "PostgreSQL no configurado en application.properties"
  fi

  # Verificar Flyway
  if grep -q "spring.flyway.enabled=true" "$APP_PROPS"; then
    check_pass "Flyway habilitado"
  else
    check_warn "Flyway no está habilitado"
  fi

  # Verificar Google OAuth
  if grep -q "GOOGLE_CLIENT_ID" "$APP_PROPS"; then
    check_pass "Google OAuth configurado (variables de entorno)"
  else
    check_warn "Google OAuth no configurado"
  fi
else
  check_fail "Falta application.properties"
fi
echo ""

# 5. Verificar migraciones Flyway
echo "5️⃣  Verificando migraciones Flyway..."
MIGRATIONS_DIR="farmacia-web/src/main/resources/db/migration/postgresql"
if [ -d "$MIGRATIONS_DIR" ]; then
  SQL_COUNT=$(find "$MIGRATIONS_DIR" -name "*.sql" | wc -l)
  if [ "$SQL_COUNT" -gt 0 ]; then
    check_pass "Migraciones Flyway encontradas ($SQL_COUNT archivos .sql)"
  else
    check_warn "No hay archivos de migración en $MIGRATIONS_DIR"
  fi
else
  check_fail "Falta directorio de migraciones: $MIGRATIONS_DIR"
fi
echo ""

# 6. Verificar .gitignore
echo "6️⃣  Verificando .gitignore..."
if [ -f ".gitignore" ]; then
  check_pass ".gitignore existe"

  # Verificar que protege archivos sensibles
  if grep -q ".env" ".gitignore"; then
    check_pass "Archivos .env están en .gitignore"
  else
    check_warn ".env no está en .gitignore (riesgo de seguridad)"
  fi
else
  check_warn ".gitignore no existe"
fi
echo ""

# 7. Verificar que estamos en un repo Git
echo "7️⃣  Verificando Git..."
if [ -d ".git" ]; then
  check_pass "Repositorio Git inicializado"

  # Verificar si hay remote configurado
  if git remote -v | grep -q "origin"; then
    REMOTE_URL=$(git remote get-url origin)
    check_pass "Remote 'origin' configurado: $REMOTE_URL"

    # Verificar si es GitHub
    if echo "$REMOTE_URL" | grep -q "github.com"; then
      check_pass "Remote apunta a GitHub"
    else
      check_warn "Remote no apunta a GitHub (DigitalOcean requiere GitHub)"
    fi
  else
    check_fail "No hay remote 'origin' configurado"
  fi

  # Verificar si hay cambios sin commitear
  if [ -n "$(git status --porcelain)" ]; then
    check_warn "Hay cambios sin commitear"
  else
    check_pass "No hay cambios sin commitear"
  fi
else
  check_fail "No es un repositorio Git"
fi
echo ""

# 8. Verificar .env.example
echo "8️⃣  Verificando .env.example..."
if [ -f ".env.example" ]; then
  check_pass ".env.example existe"
else
  check_warn ".env.example no existe (buena práctica documentar variables)"
fi
echo ""

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Resumen"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
  echo -e "${GREEN}✅ ¡Todo listo para deployment!${NC}"
  echo ""
  echo "Próximos pasos:"
  echo "1. Commit y push a GitHub:"
  echo "   git add ."
  echo "   git commit -m 'Add DigitalOcean configuration'"
  echo "   git push origin main"
  echo ""
  echo "2. Ve a https://cloud.digitalocean.com/apps"
  echo "3. Click en 'Create App' y sigue los pasos en DEPLOYMENT_DIGITALOCEAN.md"
  echo ""
elif [ $ERRORS -eq 0 ]; then
  echo -e "${YELLOW}⚠️  $WARNINGS advertencia(s) - puedes continuar pero revisa las advertencias${NC}"
  echo ""
  echo "Recomendación: Revisa las advertencias antes de deployar"
  echo "Ver DEPLOYMENT_DIGITALOCEAN.md para más detalles"
  echo ""
else
  echo -e "${RED}❌ $ERRORS error(es) crítico(s) encontrado(s)${NC}"
  if [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}⚠️  $WARNINGS advertencia(s) adicional(es)${NC}"
  fi
  echo ""
  echo "Corrige los errores antes de deployar."
  echo "Ver DEPLOYMENT_DIGITALOCEAN.md para más información"
  exit 1
fi
