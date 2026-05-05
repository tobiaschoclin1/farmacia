package com.tobias.web.controller;

import com.tobias.model.Usuario;
import com.tobias.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registro(@RequestBody RegistroRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Intento de registro para email: {}", request.getEmail());
            Usuario usuario = authService.registrar(
                request.getNombre(),
                request.getEmail(),
                request.getPassword()
            );

            response.put("success", true);
            response.put("message", "Usuario registrado exitosamente");
            response.put("userId", usuario.getId());

            logger.info("Registro exitoso para usuario: {}", usuario.getEmail());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación en registro para {}: {}", request.getEmail(), e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Error interno al procesar registro para {}: ", request.getEmail(), e);
            response.put("success", false);
            response.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Intento de login para email: {}", request.getEmail());
            Usuario usuario = authService.login(request.getEmail(), request.getPassword());

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", usuario.getId());
            userData.put("nombre", usuario.getNombre());
            userData.put("email", usuario.getEmail());
            userData.put("rol", usuario.getRol());

            response.put("success", true);
            response.put("user", userData);

            logger.info("Login exitoso para usuario: {}", usuario.getEmail());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de autenticación para {}: {}", request.getEmail(), e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            logger.error("Error interno al procesar login para {}: ", request.getEmail(), e);
            response.put("success", false);
            response.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // DTOs
    static class RegistroRequest {
        private String nombre;
        private String email;
        private String password;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
