package com.aldaba.recipes.exception;

public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(String name) {
        super("No se encontró ninguna receta para: " + name);
    }
}
