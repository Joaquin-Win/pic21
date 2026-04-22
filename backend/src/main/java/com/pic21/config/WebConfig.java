package com.pic21.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve los archivos estáticos del frontend que están en B:\java\asistencia\frontend
        registry.addResourceHandler("/**")
                .addResourceLocations("file:///B:/java/asistencia/frontend/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirige la ruta raíz '/' a 'index.html'
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
