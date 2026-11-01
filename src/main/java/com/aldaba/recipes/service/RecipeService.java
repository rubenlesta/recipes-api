package com.aldaba.recipes.service;

import com.aldaba.recipes.client.SpoonacularClient;
import com.aldaba.recipes.client.dto.SpoonacularSearchResponse;
import com.aldaba.recipes.dto.RecipeResponse;
import com.aldaba.recipes.exception.RecipeNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private final SpoonacularClient spoonacularClient;

    public RecipeService(SpoonacularClient spoonacularClient) {
        this.spoonacularClient = spoonacularClient;
    }

    // La clave unifica variantes del nombre; Spring solo cachea resultados exitosos, no excepciones.
    @Cacheable(cacheNames = "recipes",
            key = "T(com.aldaba.recipes.service.RecipeService).normalizeKey(#name)")
    public RecipeResponse findByName(String name) {
        String normalizedName = name.trim();
        SpoonacularSearchResponse.Result result = spoonacularClient
                .findFirstByName(normalizedName)
                .orElseThrow(() -> new RecipeNotFoundException(normalizedName));
        return toRecipeResponse(result);
    }

    public static String normalizeKey(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private RecipeResponse toRecipeResponse(SpoonacularSearchResponse.Result result) {
        List<String> ingredients = result.extendedIngredients() == null
                ? List.of()
                : result.extendedIngredients().stream()
                        .filter(Objects::nonNull)
                        .map(SpoonacularSearchResponse.Ingredient::original)
                        .filter(value -> value != null && !value.isBlank())
                        .toList();

        
        List<String> instructions = result.analyzedInstructions() == null
                ? List.of()
                : result.analyzedInstructions().stream()
                        .filter(Objects::nonNull)
                        .filter(group -> group.steps() != null)
                        .flatMap(group -> group.steps().stream()
                                .filter(Objects::nonNull)
                                .sorted(Comparator.comparing(
                                SpoonacularSearchResponse.Step::number,
                                Comparator.nullsLast(Comparator.naturalOrder()))))
                        .map(SpoonacularSearchResponse.Step::step)
                        .filter(value -> value != null && !value.isBlank())
                        .toList();

        // El texto plano sirve de alternativa cuando el análisis no aporta pasos.
        if (instructions.isEmpty() && result.instructions() != null) {
            instructions = result.instructions().lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .toList();
        }

        return new RecipeResponse(
                result.title(),
                result.readyInMinutes(),
                result.servings(),
                ingredients,
                instructions,
                result.image());
    }
}
