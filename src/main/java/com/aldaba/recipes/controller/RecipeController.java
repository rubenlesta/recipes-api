package com.aldaba.recipes.controller;

import com.aldaba.recipes.dto.RecipeResponse;
import com.aldaba.recipes.service.RecipeService;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recetas", description = "Búsqueda de recetas por nombre.")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    @Operation(
            summary = "Buscar una receta por nombre",
            description = "Busca en Spoonacular y devuelve la primera receta coincidente.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Receta encontrada",
                content = @Content(schema = @Schema(implementation = RecipeResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Falta el nombre o está en blanco",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "No existe ninguna receta con ese nombre",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "502",
                description = "Spoonacular no responde o devuelve un error",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ProblemDetail.class)))
    })
    public RecipeResponse findByName(
            @Parameter(
                    description = "Nombre de la receta a buscar",
                    example = "pasta",
                    required = true)
            @RequestParam @NotBlank String name) {
        return recipeService.findByName(name);
    }
}
