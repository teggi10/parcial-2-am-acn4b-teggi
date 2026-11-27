package ar.edu.davinci.fogon;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the recipes in memory while the app is open, so every screen sees the same data.
 */
public class RecipeRepository {
    private static final List<Recipe> remoteRecipes = new ArrayList<>();
    private static final List<Recipe> customRecipes = new ArrayList<>();
    private static int nextCustomId = 1000;

    public static boolean isLoaded() {
        return !remoteRecipes.isEmpty();
    }

    public static void setRemoteRecipes(List<Recipe> recipes) {
        remoteRecipes.clear();
        remoteRecipes.addAll(recipes);
    }

    /** The recipes the user created go first, so they find them right after saving. */
    public static List<Recipe> getAll() {
        List<Recipe> all = new ArrayList<>(customRecipes);
        all.addAll(remoteRecipes);
        return all;
    }

    public static void addCustomRecipe(Recipe recipe) {
        recipe.setId(nextCustomId++);
        customRecipes.add(0, recipe);
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
