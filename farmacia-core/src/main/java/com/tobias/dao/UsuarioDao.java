package com.tobias.dao;

import com.tobias.db.Db;
import com.tobias.model.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDao {

    public Usuario guardar(Usuario usuario) throws Exception {
        String sql = "INSERT INTO usuarios (nombre, email, password, rol, activo, fecha_creacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPassword());
            stmt.setString(4, usuario.getRol());
            stmt.setBoolean(5, usuario.getActivo());
            stmt.setObject(6, usuario.getFechaCreacion());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error al crear usuario, no se afectaron filas");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Error al crear usuario, no se obtuvo el ID");
                }
            }
        }

        return usuario;
    }

    public Optional<Usuario> buscarPorEmail(String email) throws Exception {
        String sql = "SELECT id, nombre, email, password, rol, activo, fecha_creacion, ultimo_acceso " +
                    "FROM usuarios WHERE email = ?";

        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Usuario> buscarPorId(Long id) throws Exception {
        String sql = "SELECT id, nombre, email, password, rol, activo, fecha_creacion, ultimo_acceso " +
                    "FROM usuarios WHERE id = ?";

        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }

        return Optional.empty();
    }

    public boolean existeEmail(String email) throws Exception {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public void actualizarUltimoAcceso(Long id) throws Exception {
        String sql = "UPDATE usuarios SET ultimo_acceso = ? WHERE id = ?";

        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, LocalDateTime.now());
            stmt.setLong(2, id);

            stmt.executeUpdate();
        }
    }

    public void actualizar(Usuario usuario) throws Exception {
        String sql = "UPDATE usuarios SET nombre = ?, email = ?, rol = ?, activo = ? WHERE id = ?";

        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getRol());
            stmt.setBoolean(4, usuario.getActivo());
            stmt.setLong(5, usuario.getId());

            stmt.executeUpdate();
        }
    }

    public void cambiarPassword(Long id, String nuevoPassword) throws Exception {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";

        try (Connection conn = Db.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoPassword);
            stmt.setLong(2, id);

            stmt.executeUpdate();
        }
    }

    public List<Usuario> listarTodos() throws Exception {
        String sql = "SELECT id, nombre, email, password, rol, activo, fecha_creacion, ultimo_acceso " +
                    "FROM usuarios ORDER BY fecha_creacion DESC";

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = Db.get();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }

        return usuarios;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassword(rs.getString("password"));
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));

        Timestamp fechaCreacionTimestamp = rs.getTimestamp("fecha_creacion");
        if (fechaCreacionTimestamp != null) {
            usuario.setFechaCreacion(fechaCreacionTimestamp.toLocalDateTime());
        }

        Timestamp ultimoAccesoTimestamp = rs.getTimestamp("ultimo_acceso");
        if (ultimoAccesoTimestamp != null) {
            usuario.setUltimoAcceso(ultimoAccesoTimestamp.toLocalDateTime());
        }

        return usuario;
    }
}
