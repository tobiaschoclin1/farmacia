package com.tobias.service;

import com.tobias.dao.UsuarioDao;
import com.tobias.model.Usuario;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

public class AuthService {

    private final UsuarioDao usuarioDao;

    public AuthService() {
        this.usuarioDao = new UsuarioDao();
    }

    /**
     * Registra un nuevo usuario
     */
    public Usuario registrar(String nombre, String email, String password) throws Exception {
        // Validar que el email no exista
        if (usuarioDao.existeEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Validar campos
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email es requerido");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        // Hashear la contraseña
        String passwordHash = hashPassword(password);

        // Crear usuario
        Usuario usuario = new Usuario(nombre, email, passwordHash, "USUARIO");

        // Guardar
        return usuarioDao.guardar(usuario);
    }

    /**
     * Autentica un usuario
     */
    public Usuario login(String email, String password) throws Exception {
        // Buscar usuario por email
        Optional<Usuario> usuarioOpt = usuarioDao.buscarPorEmail(email);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Email o contraseña incorrectos");
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar contraseña
        if (!verificarPassword(password, usuario.getPassword())) {
            throw new IllegalArgumentException("Email o contraseña incorrectos");
        }

        // Verificar que esté activo
        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        // Actualizar último acceso
        usuarioDao.actualizarUltimoAcceso(usuario.getId());

        return usuario;
    }

    /**
     * Cambia la contraseña de un usuario
     */
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva) throws Exception {
        // Buscar usuario
        Optional<Usuario> usuarioOpt = usuarioDao.buscarPorId(usuarioId);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar contraseña actual
        if (!verificarPassword(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }

        // Validar nueva contraseña
        if (passwordNueva == null || passwordNueva.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }

        // Hashear nueva contraseña
        String passwordHash = hashPassword(passwordNueva);

        // Actualizar
        usuarioDao.cambiarPassword(usuarioId, passwordHash);
    }

    /**
     * Hashea una contraseña usando SHA-256
     * Nota: Para producción se recomienda usar BCrypt, pero para simplificar usamos SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    /**
     * Verifica una contraseña contra su hash
     */
    private boolean verificarPassword(String password, String passwordHash) {
        String hash = hashPassword(password);
        return hash.equals(passwordHash);
    }

    /**
     * Busca un usuario por ID
     */
    public Optional<Usuario> buscarPorId(Long id) throws Exception {
        return usuarioDao.buscarPorId(id);
    }

    /**
     * Busca un usuario por email
     */
    public Optional<Usuario> buscarPorEmail(String email) throws Exception {
        return usuarioDao.buscarPorEmail(email);
    }
}
