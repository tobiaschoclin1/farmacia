package com.tobias.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    /**
     * Configura el LocaleResolver para usar cookies
     * El idioma se guarda en una cookie llamada "lang"
     * Idioma por defecto: Español (es)
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver("lang");
        localeResolver.setDefaultLocale(new Locale("es")); // Español por defecto
        localeResolver.setCookieMaxAge(Duration.ofDays(365)); // 1 año
        localeResolver.setCookiePath("/"); // Cookie disponible en todo el sitio
        return localeResolver;
    }

    /**
     * Configura el interceptor para cambiar el idioma
     * El parámetro URL es "lang" (ej: ?lang=en para inglés)
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // Parámetro URL para cambiar idioma
        interceptor.setIgnoreInvalidLocale(true); // Ignorar locales inválidos
        return interceptor;
    }

    /**
     * Registra el interceptor de cambio de idioma
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
