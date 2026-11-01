package com.aldaba.recipes.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldaba.recipes.dto.RecipeResponse;
import com.aldaba.recipes.service.RecipeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    void returnsRecipeForName() throws Exception {
        when(recipeService.findByName("pasta")).thenReturn(new RecipeResponse(
                "Pasta",
                35,
                4,
                List.of("pasta", "tomato"),
                List.of("Boil pasta", "Add tomato"),
                "https://example.test/pasta.jpg"));

        mockMvc.perform(get("/api/recipes").param("name", "pasta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pasta"))
                .andExpect(jsonPath("$.preparationTimeMinutes").value(35))
                .andExpect(jsonPath("$.servings").value(4))
                .andExpect(jsonPath("$.ingredients[0]").value("pasta"))
                .andExpect(jsonPath("$.instructions[1]").value("Add tomato"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.test/pasta.jpg"));
    }

    @Test
    void rejectsBlankName() throws Exception {
        mockMvc.perform(get("/api/recipes").param("name", "  "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingName() throws Exception {
        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isBadRequest());
    }
}
