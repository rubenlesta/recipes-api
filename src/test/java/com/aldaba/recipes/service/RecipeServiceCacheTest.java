package com.aldaba.recipes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aldaba.recipes.client.SpoonacularClient;
import com.aldaba.recipes.client.dto.SpoonacularSearchResponse;
import com.aldaba.recipes.client.dto.SpoonacularSearchResponse.Result;
import com.aldaba.recipes.dto.RecipeResponse;
import com.aldaba.recipes.exception.ExternalApiException;
import com.aldaba.recipes.exception.RecipeNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RecipeServiceCacheTest {

    @MockitoBean
    private SpoonacularClient spoonacularClient;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearRecipesCache() {
        cacheManager.getCache("recipes").clear();
    }

    @Test
    void cachesSuccessfulRecipeLookup() {
        Result result = recipe("Pasta");
        when(spoonacularClient.findFirstByName("pasta")).thenReturn(Optional.of(result));

        RecipeResponse first = recipeService.findByName("pasta");
        RecipeResponse second = recipeService.findByName("pasta");

        assertEquals(first, second);
        verify(spoonacularClient, times(1)).findFirstByName("pasta");
    }

    @Test
    void normalizesEquivalentNamesToSameCacheEntry() {
        when(spoonacularClient.findFirstByName(anyString()))
                .thenReturn(Optional.of(recipe("Pasta")));

        RecipeResponse first = recipeService.findByName("Pasta");
        RecipeResponse second = recipeService.findByName("  pasta ");
        RecipeResponse third = recipeService.findByName("PASTA   ");

        assertEquals(first, second);
        assertEquals(first, third);
        verify(spoonacularClient, times(1)).findFirstByName(anyString());
    }

    @Test
    void cachesDifferentNamesSeparately() {
        when(spoonacularClient.findFirstByName(anyString()))
                .thenReturn(Optional.of(recipe("Recipe")));

        recipeService.findByName("pasta");
        recipeService.findByName("pizza");

        verify(spoonacularClient, times(2)).findFirstByName(anyString());
    }

    @Test
    void doesNotCacheRecipeNotFoundException() {
        when(spoonacularClient.findFirstByName("pasta"))
                .thenReturn(Optional.empty(), Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> recipeService.findByName("pasta"));
        assertThrows(RecipeNotFoundException.class, () -> recipeService.findByName("pasta"));

        verify(spoonacularClient, times(2)).findFirstByName("pasta");
    }

    @Test
    void doesNotCacheExternalApiException() {
        Result result = recipe("Pasta");
        when(spoonacularClient.findFirstByName("pasta"))
                .thenThrow(new ExternalApiException("fallo externo"))
                .thenReturn(Optional.of(result));

        assertThrows(ExternalApiException.class, () -> recipeService.findByName("pasta"));
        assertEquals(new RecipeResponse(
                        "Pasta", 30, 2, List.of("pasta"), List.of("Cook pasta"), null),
                recipeService.findByName("pasta"));

        verify(spoonacularClient, times(2)).findFirstByName("pasta");
    }

    @Test
    void normalizesKeyWhitespaceAndCase() {
        assertEquals("mac & cheese", RecipeService.normalizeKey("  Mac   &  Cheese "));
    }

    private Result recipe(String title) {
        return new SpoonacularSearchResponse.Result(
                1L,
                title,
                30,
                2,
                null,
                "Cook pasta",
                List.of(new SpoonacularSearchResponse.Ingredient("pasta")),
                List.of(new SpoonacularSearchResponse.InstructionGroup(
                        "main",
                        List.of(new SpoonacularSearchResponse.Step(1, "Cook pasta")))));
    }
}
