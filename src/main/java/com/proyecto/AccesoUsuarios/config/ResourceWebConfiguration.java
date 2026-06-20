package com.proyecto.AccesoUsuarios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Fallback local para imágenes no gestionadas por MongoImageService
        // El ImageController (GET /images/{fileId}) tiene prioridad sobre este handler
        registry.addResourceHandler("/images/**").addResourceLocations("file:images/");
    }
}