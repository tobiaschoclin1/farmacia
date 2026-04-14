package com.tobias.web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        // Normalizar la URL para que siempre tenga el prefijo jdbc:
        String normalizedUrl = normalizeJdbcUrl(databaseUrl);

        // Limpiar parámetros incompatibles de Neon
        normalizedUrl = cleanNeonParameters(normalizedUrl);

        // Extraer credenciales si están en la URL
        String[] extracted = extractCredentialsFromUrl(normalizedUrl);
        String cleanUrl = extracted[0];
        String finalUsername = extracted[1] != null ? extracted[1] : username;
        String finalPassword = extracted[2] != null ? extracted[2] : password;

        // Cargar explícitamente el driver PostgreSQL
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL driver not found in classpath", e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(cleanUrl);
        config.setUsername(finalUsername);
        config.setPassword(finalPassword);
        config.setDriverClassName(driverClassName);

        // Configuración optimizada para free tier (pocas conexiones)
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        System.out.println("DataSource configured with URL: " + maskPassword(cleanUrl));
        System.out.println("Using username: " + finalUsername);
        return new HikariDataSource(config);
    }

    /**
     * Normaliza la URL de JDBC agregando el prefijo jdbc: si falta.
     * Neon y otros proveedores dan URLs como postgresql://...
     * pero JDBC necesita jdbc:postgresql://...
     */
    private String normalizeJdbcUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Database URL cannot be null or empty");
        }

        // Si ya tiene el prefijo jdbc:, devolverla tal cual
        if (url.startsWith("jdbc:")) {
            return url;
        }

        // Si empieza con postgresql://, agregar jdbc: al inicio
        if (url.startsWith("postgresql://")) {
            return "jdbc:" + url;
        }

        // Si empieza con postgres://, convertir a jdbc:postgresql://
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }

        // Si no reconocemos el formato, lanzar excepción
        throw new IllegalArgumentException("Unsupported database URL format: " + url);
    }

    /**
     * Limpia parámetros incompatibles que Neon agrega automáticamente.
     * El parámetro channel_binding=require puede causar problemas con algunas versiones del driver.
     */
    private String cleanNeonParameters(String url) {
        if (url == null) return null;

        // Eliminar channel_binding=require (incompatible con algunos drivers PostgreSQL)
        url = url.replaceAll("[&?]channel_binding=require", "");

        // Si quedó con ? al final, eliminarlo
        url = url.replaceAll("\\?$", "");

        // Si quedó con & duplicado, limpiarlo
        url = url.replaceAll("&&", "&");

        // Si el primer parámetro quedó con &, cambiarlo a ?
        url = url.replaceAll("\\?&", "?");

        return url;
    }

    /**
     * Extrae credenciales embebidas en la URL y devuelve una URL limpia.
     * Neon y otros proveedores incluyen usuario:password@ en la URL.
     *
     * @return Array [URL limpia, username o null, password o null]
     */
    private String[] extractCredentialsFromUrl(String url) {
        if (url == null || !url.contains("@")) {
            // No hay credenciales en la URL
            return new String[]{url, null, null};
        }

        try {
            // Patrón: jdbc:postgresql://username:password@host:port/database?params
            // Necesitamos extraer username y password, y crear una URL limpia

            String protocol = url.substring(0, url.indexOf("://") + 3);  // "jdbc:postgresql://"
            String rest = url.substring(url.indexOf("://") + 3);  // "user:pass@host:port/db?params"

            if (!rest.contains("@")) {
                return new String[]{url, null, null};
            }

            String credentials = rest.substring(0, rest.indexOf("@"));  // "user:pass"
            String hostAndRest = rest.substring(rest.indexOf("@") + 1);  // "host:port/db?params"

            String extractedUsername = null;
            String extractedPassword = null;

            if (credentials.contains(":")) {
                extractedUsername = credentials.substring(0, credentials.indexOf(":"));
                extractedPassword = credentials.substring(credentials.indexOf(":") + 1);
            } else {
                extractedUsername = credentials;
            }

            String cleanUrl = protocol + hostAndRest;

            return new String[]{cleanUrl, extractedUsername, extractedPassword};

        } catch (Exception e) {
            System.err.println("Error extracting credentials from URL: " + e.getMessage());
            return new String[]{url, null, null};
        }
    }

    /**
     * Enmascara la contraseña en la URL para logging seguro
     */
    private String maskPassword(String url) {
        if (url == null) return null;
        // Reemplazar cualquier cosa que parezca una contraseña en la URL
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
    }
}
