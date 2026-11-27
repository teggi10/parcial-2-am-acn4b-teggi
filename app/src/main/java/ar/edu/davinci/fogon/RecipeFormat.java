package ar.edu.davinci.fogon;

import android.content.Context;

public class RecipeFormat {

    public static String time(Context context, int minutes) {
        int hours = minutes / 60;
        int rest = minutes % 60;
        if (hours == 0) return context.getString(R.string.time_minutes, minutes);
        if (rest == 0) return context.getString(R.string.time_hours, hours);
        return context.getString(R.string.time_hours_minutes, hours, rest);
    }

    public static String quantity(Context context, double value) {
        if (value >= 10) return String.valueOf(Math.round(value));

        double rounded = Math.max(0.5, Math.round(value * 2) / 2.0);
        int whole = (int) Math.floor(rounded);
        boolean hasHalf = rounded - whole > 0;

        if (!hasHalf) return String.valueOf(whole);
        if (whole == 0) return context.getString(R.string.symbol_half);
        return context.getString(R.string.quantity_with_half, whole);
    }

    public static String ingredient(Context context, Ingredient ingredient, int portions, int basePortions) {
        if (ingredient.isToTaste()) return ingredient.getSingular();
        double amount = ingredient.getQuantityFor(portions, basePortions);
        return context.getString(R.string.ingredient_format, quantity(context, amount), ingredient.getNameFor(amount));
    }

    public static String meta(Context context, Recipe recipe) {
        return context.getString(R.string.recipe_meta, time(context, recipe.getMinutes()), recipe.getDifficulty());
    }
}
