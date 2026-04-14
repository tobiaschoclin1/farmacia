#!/bin/sh
set -e

# Default Java options optimizados para contenedores
DEFAULT_JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Merge con JAVA_OPTS del usuario si existe (pero sanitizar)
if [ -n "$JAVA_OPTS" ]; then
    JAVA_OPTS="$DEFAULT_JAVA_OPTS $JAVA_OPTS"
else
    JAVA_OPTS="$DEFAULT_JAVA_OPTS"
fi

# Ejecutar Java con los parámetros correctos
exec java $JAVA_OPTS -jar /app/app.jar "$@"
