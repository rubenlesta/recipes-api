package com.aldaba.recipes.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Mapeo parcial del JSON de Spoonacular. Result sirve tanto para cada elemento de
 * /recipes/complexSearch como para la respuesta de /recipes/{id}/information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SpoonacularSearchResponse(List<Result> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            Long id,
            String title,
            Integer readyInMinutes,
            Integer servings,
            String image,
            String instructions,                          // texto plano, puede faltar
            List<Ingredient> extendedIngredients,
            List<InstructionGroup> analyzedInstructions   // pasos estructurados, puede faltar
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ingredient(String original) {}          // p. ej. "1 large Yellow Onion, diced"

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InstructionGroup(String name, List<Step> steps) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Step(Integer number, String step) {}
}