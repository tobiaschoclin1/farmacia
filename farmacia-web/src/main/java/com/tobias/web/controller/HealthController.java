package com.tobias.web.controller;

import com.tobias.db.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }

    @GetMapping("/health/db")
    public Map<String, Object> healthDb() {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Verificando conexión a base de datos...");
            Connection conn = Db.get();

            if (conn != null && !conn.isClosed()) {
                String dbProductName = conn.getMetaData().getDatabaseProductName();
                String dbVersion = conn.getMetaData().getDatabaseProductVersion();

                response.put("status", "UP");
                response.put("database", dbProductName);
                response.put("version", dbVersion);

                conn.close();
                logger.info("Conexión a base de datos exitosa: {} {}", dbProductName, dbVersion);
            } else {
                response.put("status", "DOWN");
                response.put("error", "Connection is null or closed");
                logger.error("La conexión a la base de datos está cerrada o es nula");
            }

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            logger.error("Error al conectar a la base de datos: ", e);
        }

        return response;
    }
}
