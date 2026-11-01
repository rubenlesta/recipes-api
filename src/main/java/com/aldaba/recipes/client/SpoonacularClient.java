package com.aldaba.recipes.client;

import com.aldaba.recipes.client.dto.SpoonacularSearchResponse;
import com.aldaba.recipes.exception.ExternalApiException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SpoonacularClient {

    private final RestClient restClient;
    private final String apiKey;

    public SpoonacularClient(
            RestClient restClient,
            @Value("${spoonacular.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public Optional<SpoonacularSearchResponse.Result> findFirstByName(String name) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExternalApiException("La clave de API de Spoonacular está vacía");
        }

        try {
            SpoonacularSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recipes/complexSearch")
                            // La plantilla evita que caracteres del nombre se interpreten como URI.
                            .queryParam("query", "{name}")
                            .queryParam("number", 1)
                            .queryParam("addRecipeInformation", true)
                            // Spoonacular solo devuelve extendedIngredients si se solicita.
                            .queryParam("fillIngredients", true)
                            .queryParam("addRecipeInstructions", true)
                            .build(name))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                        throw new ExternalApiException(
                                "Spoonacular respondió con HTTP "
                                        + httpResponse.getStatusCode().value());
                    })
                    .body(SpoonacularSearchResponse.class);

            if (response == null || response.results() == null || response.results().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(response.results().get(0));
        } catch (RestClientException exception) {
            throw new ExternalApiException("Error de comunicación con Spoonacular", exception);
        }
    }
}
