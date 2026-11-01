package com.aldaba.recipes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
        description = "Receta encontrada y normalizada para la respuesta de la API.",
        example = "{\"name\":\"Ratatouille Pasta\",\"preparationTimeMinutes\":45,"
                + "\"servings\":4,\"ingredients\":[\"pasta\",\"berenjena\","
                + "\"calabacín\",\"tomate\"],\"instructions\":[\"Cocer la pasta\","
                + "\"Añadir las verduras\"],"
                + "\"imageUrl\":\"https://example.com/ratatouille-pasta.jpg\"}")
public record RecipeResponse(
        @Schema(description = "Nombre de la receta.", example = "Ratatouille Pasta")
        String name,
        @Schema(description = "Tiempo de preparación en minutos.", example = "45")
        Integer preparationTimeMinutes,
        @Schema(description = "Número de porciones.", example = "4")
        Integer servings,
        @Schema(
                description = "Ingredientes de la receta.",
                example = "[\"pasta\", \"berenjena\", \"calabacín\", \"tomate\"]")
        List<String> ingredients,
        @Schema(
                description = "Pasos para preparar la receta.",
                example = "[\"Cocer la pasta\", \"Añadir las verduras\"]")
        List<String> instructions,
        @Schema(
                description = "URL de la imagen de la receta.",
                example = "https://example.com/ratatouille-pasta.jpg")
        String imageUrl) {
}
