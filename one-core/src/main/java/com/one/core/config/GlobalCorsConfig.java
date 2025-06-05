package com.one.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS que permite el acceso desde cualquier origen,
 * método y cabecera, sin credenciales. Ideal para entornos de desarrollo.
 */
@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todas las rutas de la API
                .allowedOrigins("*") // Permite cualquier origen
                .allowedMethods("*") // Permite todos los métodos HTTP
                .allowedHeaders("*") // Permite cualquier cabecera
                .allowCredentials(false); // No permite credenciales (requerido si usás "*")
    }
}
