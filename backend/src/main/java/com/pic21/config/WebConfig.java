/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.config.WebConfig
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.http.CacheControl
 *  org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
package com.pic21.config;

import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig
implements WebMvcConfigurer {
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(new String[]{"/js/**", "/css/**"}).addResourceLocations(new String[]{"classpath:/static/js/", "classpath:/static/css/"}).setCacheControl(CacheControl.maxAge((Duration)Duration.ofDays(365L)).cachePublic());
        registry.addResourceHandler(new String[]{"/img/**"}).addResourceLocations(new String[]{"classpath:/static/img/"}).setCacheControl(CacheControl.maxAge((Duration)Duration.ofDays(7L)).cachePublic());
        registry.addResourceHandler(new String[]{"/**"}).addResourceLocations(new String[]{"classpath:/static/"}).setCacheControl(CacheControl.noCache().mustRevalidate());
    }
}

