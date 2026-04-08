package com.tobias.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Utilidad para manejar diferencias de SQL entre dialectos (SQLite vs PostgreSQL)
 */
public class DatabaseDialect {

    private static volatile DatabaseType cachedType = null;

    public enum DatabaseType {
        SQLITE,
        POSTGRESQL,
        UNKNOWN
    }

    /**
     * Detecta el tipo de base de datos desde una conexión
     */
    public static DatabaseType detect(Connection connection) {
        if (cachedType != null) {
            return cachedType;
        }

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName().toLowerCase();

            if (productName.contains("sqlite")) {
                cachedType = DatabaseType.SQLITE;
            } else if (productName.contains("postgresql")) {
                cachedType = DatabaseType.POSTGRESQL;
            } else {
                cachedType = DatabaseType.UNKNOWN;
            }

            return cachedType;
        } catch (Exception e) {
            return DatabaseType.UNKNOWN;
        }
    }

    /**
     * SQL para obtener la fecha/hora actual como TIMESTAMP
     * SQLite: datetime('now')
     * PostgreSQL: CURRENT_TIMESTAMP
     */
    public static String currentTimestamp(Connection connection) {
        DatabaseType type = detect(connection);
        return switch (type) {
            case SQLITE -> "datetime('now')";
            case POSTGRESQL -> "CURRENT_TIMESTAMP";
            default -> "CURRENT_TIMESTAMP";
        };
    }

    /**
     * SQL para obtener la fecha actual (sin hora)
     * SQLite: date('now')
     * PostgreSQL: CURRENT_DATE
     */
    public static String currentDate(Connection connection) {
        DatabaseType type = detect(connection);
        return switch (type) {
            case SQLITE -> "date('now')";
            case POSTGRESQL -> "CURRENT_DATE";
            default -> "CURRENT_DATE";
        };
    }

    /**
     * SQL para sumar días a una fecha
     * SQLite: date('now', '+X days')
     * PostgreSQL: CURRENT_DATE + INTERVAL 'X days'
     */
    public static String dateAddDays(Connection connection, int days) {
        DatabaseType type = detect(connection);
        return switch (type) {
            case SQLITE -> String.format("date('now', '+%d days')", days);
            case POSTGRESQL -> String.format("CURRENT_DATE + INTERVAL '%d days'", days);
            default -> String.format("CURRENT_DATE + INTERVAL '%d days'", days);
        };
    }

    /**
     * SQL para convertir BOOLEAN a INTEGER (para compatibilidad con SQLite)
     * SQLite: usa 1/0 para true/false
     * PostgreSQL: soporta BOOLEAN nativo
     */
    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * SQL para convertir INTEGER a BOOLEAN (para compatibilidad con SQLite)
     */
    public static boolean intToBoolean(int value) {
        return value != 0;
    }

    /**
     * SQL para CAST de fecha
     * SQLite: date(column)
     * PostgreSQL: column::date o CAST(column AS DATE)
     */
    public static String castToDate(Connection connection, String column) {
        DatabaseType type = detect(connection);
        return switch (type) {
            case SQLITE -> String.format("date(%s)", column);
            case POSTGRESQL -> String.format("CAST(%s AS DATE)", column);
            default -> String.format("CAST(%s AS DATE)", column);
        };
    }

    /**
     * Limpiar cache (útil para testing)
     */
    public static void clearCache() {
        cachedType = null;
    }

    /**
     * SQL para calcular diferencia de días entre dos fechas
     * SQLite: julianday(fecha1) - julianday(fecha2)
     * PostgreSQL: fecha1 - fecha2
     */
    public static String daysBetween(Connection connection, String date1, String date2) {
        DatabaseType type = detect(connection);
        return switch (type) {
            case SQLITE -> String.format("CAST(julianday(%s) - julianday(%s) AS INT)", date1, date2);
            case POSTGRESQL -> String.format("(%s - %s)", date1, date2);
            default -> String.format("(%s - %s)", date1, date2);
        };
    }
}
