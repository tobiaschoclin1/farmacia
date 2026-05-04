package com.tobias.web.config;

import com.tobias.model.Usuario;
import com.tobias.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final AuthService authService;

    public OAuth2LoginSuccessHandler(AuthService authService) {
        this.authService = authService;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String nombre = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        // Verificar si el usuario ya existe
        Usuario usuario = authService.buscarPorEmail(email);

        if (usuario == null) {
            // Crear nuevo usuario con Google OAuth
            usuario = authService.registrarConGoogle(nombre, email, googleId);
        } else if (usuario.getGoogleId() == null) {
            // Vincular cuenta existente con Google
            authService.vincularGoogleId(usuario.getId(), googleId);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
