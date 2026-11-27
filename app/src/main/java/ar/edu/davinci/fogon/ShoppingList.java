package ar.edu.davinci.fogon;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The shopping list shared by every screen while the app is open.
 */
public class ShoppingList {
    private static final List<ShoppingItem> items = new ArrayList<>();

    /** Adds the ingredients the user does not have yet, already scaled to the chosen portions. */
    public static int addFromRecipe(Context context, Recipe recipe, int portions) {
        removeRecipe(recipe.getName());
        int added = 0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isChecked() || ingredient.isToTaste()) continue;
            String text = RecipeFormat.ingredient(context, ingredient, portions, recipe.getServings());
            items.add(new ShoppingItem(text, recipe.getName()));
            added++;
        }
        return added;
    }

    public static void removeRecipe(String recipeName) {
        Iterator<ShoppingItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            if (recipeName.equals(iterator.next().getRecipeName())) iterator.remove();
        }
    }

    public static boolean containsRecipe(String recipeName) {
        for (ShoppingItem item : items) {
            if (recipeName.equals(item.getRecipeName())) return true;
        }
        return false;
    }

    public static void addCustom(String text) {
        items.add(new ShoppingItem(text, null));
    }

    public static int clearChecked() {
        int removed = 0;
        Iterator<ShoppingItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isChecked()) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    public static int size() {
        return items.size();
    }

    public static int checkedCount() {
        int count = 0;
        for (ShoppingItem item : items) {
            if (item.isChecked()) count++;
        }
        return count;
    }

    /** Items grouped by recipe, keeping the order in which they were added. Hand-added items use a null key. */
    public static Map<String, List<ShoppingItem>> groupedByRecipe() {
        Map<String, List<ShoppingItem>> groups = new LinkedHashMap<>();
        for (ShoppingItem item : items) {
            String key = item.getRecipeName();
            if (!groups.containsKey(key)) groups.put(key, new ArrayList<>());
            groups.get(key).add(item);
        }
        return groups;
    }
}
