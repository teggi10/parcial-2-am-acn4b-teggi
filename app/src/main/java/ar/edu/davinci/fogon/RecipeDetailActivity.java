package ar.edu.davinci.fogon;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RECIPE = "recipe";
    public final int MIN_PORTIONS = 1;
    public final int COOKING_REQUEST = 1;

    public Recipe recipe;
    public int portions;

    public boolean isFavorite = false;

    private int maxPortions() {
        return recipe.getServings() * 4;
    }

    public void startCooking(View view) {
        Bundle bundle = new Bundle();
        bundle.putString(CookingActivity.EXTRA_RECIPE_NAME, recipe.getName());
        bundle.putSerializable(CookingActivity.EXTRA_STEPS, new ArrayList<>(recipe.getSteps()));

        Intent intent = new Intent(this, CookingActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, COOKING_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != COOKING_REQUEST) return;
        if (resultCode != RESULT_OK) return;
        if (data == null || !data.getBooleanExtra(CookingActivity.EXTRA_COMPLETED, false)) return;

        Button cookingButton = findViewById(R.id.buttonCooking);
        cookingButton.setText(R.string.cooking_again);
        Toast.makeText(this, R.string.cooking_done_toast, Toast.LENGTH_LONG).show();
    }

    public void goBack(View view) {
        finish();
    }

    public void toggleFavorite(View view) {
        ImageButton button = (ImageButton) view;
        isFavorite = !isFavorite;

        if (isFavorite) {
            button.setImageResource(R.drawable.ic_favorite);
            button.setContentDescription(getString(R.string.favorite_remove));
            Toast.makeText(this, R.string.favorite_added_toast, Toast.LENGTH_SHORT).show();
        } else {
            button.setImageResource(R.drawable.ic_favorite_border);
            button.setContentDescription(getString(R.string.favorite_add));
            Toast.makeText(this, R.string.favorite_removed_toast, Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleShoppingList(View view) {
        if (ShoppingList.containsRecipe(recipe.getName())) {
            ShoppingList.removeRecipe(recipe.getName());
            updateShoppingButton();
            Toast.makeText(this, R.string.shopping_removed, Toast.LENGTH_SHORT).show();
            return;
        }

        int added = ShoppingList.addFromRecipe(this, recipe, portions);
        if (added == 0) {
            Toast.makeText(this, R.string.shopping_nothing_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        updateShoppingButton();
        String message = getResources().getQuantityString(R.plurals.shopping_added_toast, added, added);
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.actionBar)
                .setAction(R.string.shopping_view_list, v -> openShoppingList())
                .show();
    }

    private void openShoppingList() {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void updateShoppingButton() {
        Button button = findViewById(R.id.buttonShoppingList);
        boolean inList = ShoppingList.containsRecipe(recipe.getName());
        button.setText(inList ? R.string.shopping_added : R.string.shopping_add);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The list may have changed in another screen while this one was in the background.
        if (recipe != null) updateShoppingButton();
    }

    public void decreasePortions(View view) {
        if (portions <= MIN_PORTIONS) return;
        portions--;
        updatePortions();
    }

    public void increasePortions(View view) {
        if (portions >= maxPortions()) return;
        portions++;
        updatePortions();
    }

    private void updatePortions() {
        TextView portionsCount = findViewById(R.id.portionsCount);
        portionsCount.setText(String.valueOf(portions));

        TextView portionsLabel = findViewById(R.id.portionsLabel);
        portionsLabel.setText(capitalize(recipe.getServingName(portions)));

        findViewById(R.id.buttonLessPortions).setEnabled(portions > MIN_PORTIONS);
        findViewById(R.id.buttonMorePortions).setEnabled(portions < maxPortions());

        renderIngredients();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return getString(R.string.portions_label);
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }

    private void bindRecipe() {
        TextView category = findViewById(R.id.recipeCategory);
        category.setText(getString(R.string.recipe_overline, recipe.getCategory(), recipe.getRegion()));

        TextView title = findViewById(R.id.recipeTitle);
        title.setText(recipe.getName());

        TextView description = findViewById(R.id.recipeDescription);
        description.setText(recipe.getDescription());

        TextView time = findViewById(R.id.infoTimeValue);
        time.setText(RecipeFormat.time(this, recipe.getMinutes()));

        TextView difficulty = findViewById(R.id.infoDifficultyValue);
        difficulty.setText(recipe.getDifficulty());

        TextView calories = findViewById(R.id.infoCaloriesValue);
        calories.setText(recipe.getCalories());

        ImageView hero = findViewById(R.id.heroImage);
        ImageButton favorite = findViewById(R.id.buttonFavorite);
        if (recipe.hasImage()) {
            hero.setContentDescription(recipe.getName());
            ImageDownloader.load(hero, recipe.getImageUrl());
        } else {
            hero.setScaleType(ImageView.ScaleType.CENTER);
            hero.setImageResource(R.drawable.ic_photo_placeholder);
        }
        favorite.setContentDescription(getString(R.string.favorite_add));

        TextView credit = findViewById(R.id.photoCredit);
        if (recipe.getPhotoCredit() != null) {
            credit.setText(recipe.getPhotoCredit());
        } else {
            credit.setVisibility(View.GONE);
        }
    }

    private void renderIngredients() {
        LinearLayout container = findViewById(R.id.ingredientsContainer);
        container.removeAllViews();

        for (Ingredient ingredient : recipe.getIngredients()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(RecipeFormat.ingredient(this, ingredient, portions, recipe.getServings()));
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_body));
            checkBox.setMinHeight(getResources().getDimensionPixelSize(R.dimen.row_min_height));
            checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.herb)));
            checkBox.setChecked(ingredient.isChecked());
            applyCheckedStyle(checkBox, ingredient.isChecked());
            checkBox.setOnCheckedChangeListener((button, isChecked) -> {
                ingredient.setChecked(isChecked);
                applyCheckedStyle(checkBox, isChecked);
            });
            container.addView(checkBox);
        }
    }

    private void applyCheckedStyle(CheckBox checkBox, boolean isChecked) {
        int flags = checkBox.getPaintFlags();
        if (isChecked) {
            checkBox.setPaintFlags(flags | Paint.STRIKE_THRU_TEXT_FLAG);
            checkBox.setTextColor(ContextCompat.getColor(this, R.color.muted));
        } else {
            checkBox.setPaintFlags(flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
            checkBox.setTextColor(ContextCompat.getColor(this, R.color.ink));
        }
    }

    private void renderSteps() {
        LinearLayout container = findViewById(R.id.stepsContainer);
        List<Step> steps = recipe.getSteps();
        int padding = getResources().getDimensionPixelSize(R.dimen.card_padding);
        int numberSize = getResources().getDimensionPixelSize(R.dimen.step_number_size);
        int gap = getResources().getDimensionPixelSize(R.dimen.step_gap);

        for (int i = 0; i < steps.size(); i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(padding, padding, padding, padding);

            TextView number = new TextView(this);
            number.setText(String.valueOf(i + 1));
            number.setGravity(Gravity.CENTER);
            number.setTypeface(Typeface.DEFAULT_BOLD);
            number.setTextColor(ContextCompat.getColor(this, R.color.herb_dark));
            number.setBackgroundResource(R.drawable.bg_step_number);
            row.addView(number, new LinearLayout.LayoutParams(numberSize, numberSize));

            TextView text = new TextView(this);
            text.setText(steps.get(i).getText());
            text.setTextColor(ContextCompat.getColor(this, R.color.ink));
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_body));
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
            );
            textParams.setMarginStart(gap);
            row.addView(text, textParams);

            container.addView(row);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recipe = (Recipe) getIntent().getSerializableExtra(EXTRA_RECIPE);
        if (recipe == null) {
            finish();
            return;
        }
        portions = recipe.getServings();

        bindRecipe();
        updatePortions();
        renderSteps();
    }
}
