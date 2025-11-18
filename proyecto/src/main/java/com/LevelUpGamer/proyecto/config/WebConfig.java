package com.LevelUpGamer.proyecto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer { // <-- Nota: La clase implementa la interfaz

    // Inyecta la ruta de la carpeta 'uploads' desde application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Esta es tu configuración de CORS (está perfecta).
     * Le dice a Spring que acepte peticiones desde tu app de React.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica a toda tu API
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    /**
     * ¡ESTA ES LA PIEZA QUE FALTABA!
     * Esto le dice a Spring CÓMO servir los archivos de la carpeta 'uploads'.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Le decimos a Spring: "Cualquier petición que empiece con /uploads/..."
        registry.addResourceHandler("/uploads/**")

                // "...debe buscar el archivo en esta carpeta física de mi disco duro."
                // "file:" es crucial para decirle que es una ruta del sistema.
                .addResourceLocations("file:" + uploadDir + "/");
    }
}