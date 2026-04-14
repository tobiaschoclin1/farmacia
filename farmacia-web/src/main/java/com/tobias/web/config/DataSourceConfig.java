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

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(normalizedUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // Configuración optimizada para free tier (pocas conexiones)
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        System.out.println("DataSource configured with URL: " + maskPassword(normalizedUrl));
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
     * Enmascara la contraseña en la URL para logging seguro
     */
    private String maskPassword(String url) {
        if (url == null) return null;
        // Reemplazar cualquier cosa que parezca una contraseña en la URL
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
    }
}
