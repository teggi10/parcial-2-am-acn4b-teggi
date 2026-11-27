package ar.edu.davinci.fogon;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

public class BottomNav {
    public enum Tab { HOME, SHOPPING }

    public static final int NEW_RECIPE_REQUEST = 10;

    public static void setup(Activity activity, Tab current) {
        highlight(activity, R.id.navHomeIcon, R.id.navHomeLabel, current == Tab.HOME);
        highlight(activity, R.id.navShoppingIcon, R.id.navShoppingLabel, current == Tab.SHOPPING);

        activity.findViewById(R.id.navHome).setOnClickListener(v -> {
            if (current == Tab.HOME) return;
            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        });

        activity.findViewById(R.id.navNewRecipe).setOnClickListener(v -> {
            Intent intent = new Intent(activity, NewRecipeActivity.class);
            activity.startActivityForResult(intent, NEW_RECIPE_REQUEST);
        });

        activity.findViewById(R.id.navShopping).setOnClickListener(v -> {
            if (current == Tab.SHOPPING) return;
            Intent intent = new Intent(activity, ShoppingListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });
    }

    /** Saves the recipe returned by NewRecipeActivity. Returns it, or null when nothing was created. */
    public static Recipe handleNewRecipeResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != NEW_RECIPE_REQUEST || resultCode != Activity.RESULT_OK || data == null) return null;
        Recipe recipe = (Recipe) data.getSerializableExtra(NewRecipeActivity.EXTRA_NEW_RECIPE);
        if (recipe == null) return null;

        RecipeRepository.addCustomRecipe(recipe);
        String message = activity.getString(R.string.new_recipe_saved_toast, recipe.getName());
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        return recipe;
    }

    private static void highlight(Activity activity, int iconId, int labelId, boolean active) {
        int color = ContextCompat.getColor(activity, active ? R.color.herb : R.color.muted);
        ImageView icon = activity.findViewById(iconId);
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(color));

        TextView label = activity.findViewById(labelId);
        label.setTextColor(color);
        label.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }
}
