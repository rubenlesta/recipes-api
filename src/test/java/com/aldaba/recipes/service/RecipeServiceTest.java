package com.aldaba.recipes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aldaba.recipes.client.SpoonacularClient;
import com.aldaba.recipes.client.dto.SpoonacularSearchResponse;
import com.aldaba.recipes.client.dto.SpoonacularSearchResponse.Ingredient;
import com.aldaba.recipes.client.dto.SpoonacularSearchResponse.InstructionGroup;
import com.aldaba.recipes.client.dto.SpoonacularSearchResponse.Result;
import com.aldaba.recipes.client.dto.SpoonacularSearchResponse.Step;
import com.aldaba.recipes.dto.RecipeResponse;
import com.aldaba.recipes.exception.RecipeNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private SpoonacularClient spoonacularClient;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void mapsAllRecipeFieldsAndOrdersSteps() {
        Result result = new Result(
                1L,
                "Pasta",
                35,
                4,
                "https://example.test/pasta.jpg",
                null,
                List.of(new Ingredient("pasta"), new Ingredient("tomato")),
                List.of(new InstructionGroup(
                        "",
                        List.of(
                                new Step(2, "Añadir tomate"),
                                new Step(1, "Cocer pasta")))));
        when(spoonacularClient.findFirstByName("pasta")).thenReturn(Optional.of(result));

        RecipeResponse response = recipeService.findByName("pasta");

        assertEquals("Pasta", response.name());
        assertEquals(35, response.preparationTimeMinutes());
        assertEquals(4, response.servings());
        assertEquals(List.of("pasta", "tomato"), response.ingredients());
        assertEquals(List.of("Cocer pasta", "Añadir tomate"), response.instructions());
        assertEquals("https://example.test/pasta.jpg", response.imageUrl());
    }

    @Test
    void throwsWhenClientReturnsNoRecipe() {
        when(spoonacularClient.findFirstByName("pasta")).thenReturn(Optional.empty());

        RecipeNotFoundException exception = assertThrows(
                RecipeNotFoundException.class,
                () -> recipeService.findByName("pasta"));

        assertEquals("No se encontró ninguna receta para: pasta", exception.getMessage());
    }

    @Test
    void returnsEmptyListsWhenRecipeDetailsAreNull() {
        Result result = new Result(1L, "Pasta", null, null, null, null, null, null);
        when(spoonacularClient.findFirstByName("pasta")).thenReturn(Optional.of(result));

        RecipeResponse response = recipeService.findByName("pasta");

        assertNotNull(response.ingredients());
        assertEquals(List.of(), response.ingredients());
        assertNotNull(response.instructions());
        assertEquals(List.of(), response.instructions());
    }

    @Test
    void usesPlainTextInstructionsWhenStructuredInstructionsAreNull() {
        Result result = new Result(
                1L, "Pasta", null, null, null, "Paso uno\n\nPaso dos\nPaso tres", null, null);
        when(spoonacularClient.findFirstByName("pasta")).thenReturn(Optional.of(result));

        RecipeResponse response = recipeService.findByName("pasta");

        assertEquals(List.of("Paso uno", "Paso dos", "Paso tres"), response.instructions());
    }

    @Test
    void trimsNameBeforeCallingClient() {
        when(spoonacularClient.findFirstByName("pasta")).thenReturn(Optional.of(
                new Result(1L, "Pasta", null, null, null, null, null, null)));

        recipeService.findByName("  pasta  ");

        verify(spoonacularClient).findFirstByName("pasta");
    }

    @Test
    void keepsStepOrderWithinEachInstructionGroup() {
        Result result = new Result(1L, "Pasta", null, null, null, null, null, List.of(
            new InstructionGroup("Salsa", List.of(new Step(1, "Salsa 1"), new Step(2, "Salsa 2"))),
            new InstructionGroup("Masa", List.of(new Step(1, "Masa 1"), new Step(2, "Masa 2")))));
        when(spoonacularClient.findFirstByName("pasta")).thenReturn(Optional.of(result));

        RecipeResponse response = recipeService.findByName("pasta");

        assertEquals(List.of("Salsa 1", "Salsa 2", "Masa 1", "Masa 2"), response.instructions());
}
}
