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

    public static String meta(Context context, Recipe recipe) {
        return context.getString(R.string.recipe_meta, time(context, recipe.getMinutes()), recipe.getDifficulty());
    }
}
