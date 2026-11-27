package ar.edu.davinci.fogon;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the recipes in memory while the app is open, so every screen sees the same data.
 */
public class RecipeRepository {
    private static final List<Recipe> remoteRecipes = new ArrayList<>();

    public static boolean isLoaded() {
        return !remoteRecipes.isEmpty();
    }

    public static void setRemoteRecipes(List<Recipe> recipes) {
        remoteRecipes.clear();
        remoteRecipes.addAll(recipes);
    }

    public static List<Recipe> getAll() {
        return new ArrayList<>(remoteRecipes);
    }

    public static List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        for (Recipe recipe : getAll()) {
            if (!categories.contains(recipe.getCategory())) categories.add(recipe.getCategory());
        }
        return categories;
    }

    public static Recipe getFeatured() {
        return remoteRecipes.isEmpty() ? null : remoteRecipes.get(0);
    }
}
