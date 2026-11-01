package com.aldaba.recipes.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldaba.recipes.controller.RecipeController;
import com.aldaba.recipes.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(RecipeController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    void returnsProblemDetailWhenRecipeIsNotFound() throws Exception {
        when(recipeService.findByName("xyz"))
                .thenThrow(new RecipeNotFoundException("xyz"));

        assertProblemFormat(mockMvc.perform(get("/api/recipes").param("name", "xyz")), 404)
                .andExpect(jsonPath("$.title").value("Receta no encontrada"))
                .andExpect(jsonPath("$.detail").value(containsString("xyz")));
    }

    @Test
    void hidesExternalApiExceptionMessage() throws Exception {
        when(recipeService.findByName("pasta"))
                .thenThrow(new ExternalApiException("secreto-interno"));

        assertProblemFormat(mockMvc.perform(get("/api/recipes").param("name", "pasta")), 502)
                .andExpect(jsonPath("$.title").value("Error en el proveedor externo"))
                .andExpect(jsonPath("$.detail")
                        .value("No se pudo obtener la receta del proveedor externo"))
                .andExpect(content().string(not(containsString("secreto-interno"))));
    }

    @Test
    void hidesUnexpectedExceptionMessage() throws Exception {
        when(recipeService.findByName("pasta"))
                .thenThrow(new RuntimeException("fallo-interno"));

        assertProblemFormat(mockMvc.perform(get("/api/recipes").param("name", "pasta")), 500)
                .andExpect(jsonPath("$.title").value("Error interno"))
                .andExpect(jsonPath("$.detail").value("Ocurrió un error interno"))
                .andExpect(content().string(not(containsString("fallo-interno"))));
    }

    @Test
    void returnsProblemDetailWhenNameIsBlank() throws Exception {
        assertProblemFormat(mockMvc.perform(get("/api/recipes").param("name", " ")), 400);
    }

    @Test
    void returnsProblemDetailWhenNameIsMissing() throws Exception {
        assertProblemFormat(mockMvc.perform(get("/api/recipes")), 400);
    }

    @Test
    void returnsNotFoundProblemForUnknownUrl() throws Exception {
        assertProblemFormat(mockMvc.perform(get("/api/no-existe")), 404);
    }

    private ResultActions assertProblemFormat(ResultActions result, int expectedStatus)
            throws Exception {
        result.andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty());
        return result;
    }
}
