package com.aldaba.recipes.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

// Habilita la caché configurada por Spring Boot en application.yml.
@Configuration
@EnableCaching
public class CacheConfig {
}
