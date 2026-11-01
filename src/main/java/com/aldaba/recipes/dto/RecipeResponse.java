package com.aldaba.recipes.dto;

import java.util.List;

public record RecipeResponse(
        String name,
        Integer preparationTimeMinutes,
        Integer servings,
        List<String> ingredients,
        List<String> instructions,
        String imageUrl) {
}
