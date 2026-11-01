package com.aldaba.recipes.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aldaba.recipes.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    void servesOpenApiJsonWithRecipePathAndTitle() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paths['/api/recipes']").exists())
                .andExpect(jsonPath("$.info.title").value("Recipes API"));
    }

    @Test
    void documentsRecipeOperationResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/recipes'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/recipes'].get.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/recipes'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/recipes'].get.responses['502']").exists());
    }

    @Test
    void documentsRecipeResponseSchemaProperties() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.RecipeResponse").exists())
                .andExpect(jsonPath("$.components.schemas.RecipeResponse.properties.name").exists())
                .andExpect(jsonPath("$.components.schemas.RecipeResponse.properties.preparationTimeMinutes")
                        .exists())
                .andExpect(jsonPath("$.components.schemas.RecipeResponse.properties.servings").exists())
                .andExpect(jsonPath("$.components.schemas.RecipeResponse.properties.ingredients")
                        .exists())
                .andExpect(jsonPath("$.components.schemas.RecipeResponse.properties.instructions")
                        .exists())
                .andExpect(jsonPath("$.components.schemas.RecipeResponse.properties.imageUrl").exists());
    }

    @Test
    void redirectsSwaggerUiHtmlToTheInterface() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("swagger-ui")));
    }

    @Test
    void doesNotExposeApiKeyConfigurationInDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("x-api-key"))))
                .andExpect(content().string(not(containsString("spoonacular.api-key"))));
    }
}
