package com.aldaba.recipes.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient spoonacularRestClient(
            RestClient.Builder builder,
            @Value("${spoonacular.base-url}") String baseUrl,
            @Value("${spoonacular.api-key:}") String apiKey) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        // Limita la espera para que un fallo remoto no bloquee la aplicación.
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return builder
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .requestFactory(requestFactory)
                .build();
    }
}
