package com.LevelUpGamer.proyecto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // Esta configuración es perfecta para ti.
        // addMapping("/api/**") significa "aplicar a todas las URLs
        // que empiecen con /api/".
        // Esto cubre /api/products y /api/products/{id}/reviews.
        registry.addMapping("/api/**")

                // ¡PREGUNTA CLAVE! (Ver abajo)
                .allowedOrigins("http://localhost:5173")

                // Permite los métodos que necesitas
                .allowedMethods("GET", "POST", "PUT", "DELETE")

                // Permite todos los encabezados
                .allowedHeaders("*");
    }
}