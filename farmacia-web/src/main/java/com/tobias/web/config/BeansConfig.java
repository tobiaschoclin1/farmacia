package com.tobias.web.config;

import com.tobias.dao.ProductDao;
import com.tobias.dao.UsuarioDao;
import com.tobias.service.AuthService;
import com.tobias.service.StockService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public UsuarioDao usuarioDao() {
        return new UsuarioDao();
    }

    @Bean
    public ProductDao productDao() {
        return new ProductDao();
    }

    @Bean
    public AuthService authService(UsuarioDao usuarioDao) {
        return new AuthService(usuarioDao);
    }

    @Bean
    public StockService stockService() {
        return new StockService();
    }
}
