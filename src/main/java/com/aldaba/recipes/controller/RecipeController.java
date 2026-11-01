package com.aldaba.recipes.controller;

import com.aldaba.recipes.dto.RecipeResponse;
import com.aldaba.recipes.service.RecipeService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public RecipeResponse findByName(@RequestParam @NotBlank String name) {
        return recipeService.findByName(name);
    }
}
