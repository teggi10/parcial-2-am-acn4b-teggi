package ar.edu.davinci.fogon;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class NewRecipeActivity extends AppCompatActivity {
    public static final String EXTRA_NEW_RECIPE = "newRecipe";

    public ArrayList<Ingredient> ingredients = new ArrayList<>();
    public ArrayList<Step> steps = new ArrayList<>();
    public String photoUrl = null;

    public void goBack(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void previewPhoto(View view) {
        EditText input = findViewById(R.id.photoUrlInput);
        String url = input.getText().toString().trim();
        if (!url.startsWith("http")) {
            input.setError(getString(R.string.new_recipe_photo_hint));
            return;
        }
        photoUrl = url;
        ImageView photo = findViewById(R.id.newRecipePhoto);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setClipToOutline(true);
        ImageDownloader.load(photo, url);
    }

    public void addIngredient(View view) {
        EditText quantityInput = findViewById(R.id.quantityInput);
        EditText nameInput = findViewById(R.id.ingredientInput);
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.new_recipe_error_name));
            return;
        }
        // An empty quantity means "to taste", like "Sal a gusto" in the downloaded recipes.
        double quantity = parseNumber(quantityInput.getText().toString());
        ingredients.add(new Ingredient(Math.max(quantity, 0), name, name));

        quantityInput.setText("");
        nameInput.setText("");
        quantityInput.requestFocus();
        renderIngredients();
    }

    public void addStep(View view) {
        EditText stepInput = findViewById(R.id.stepInput);
        String text = stepInput.getText().toString().trim();
        if (text.isEmpty()) return;

        steps.add(new Step(text, 0));
        stepInput.setText("");
        renderSteps();
    }

    private void renderIngredients() {
        LinearLayout list = findViewById(R.id.ingredientsList);
        list.removeAllViews();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            String text = RecipeFormat.ingredient(this, ingredient, 1, 1);
            int index = i;
            list.addView(createRow(text, v -> {
                ingredients.remove(index);
                renderIngredients();
            }));
        }
    }

    private void renderSteps() {
        LinearLayout list = findViewById(R.id.stepsList);
        list.removeAllViews();
        for (int i = 0; i < steps.size(); i++) {
            String text = getString(R.string.new_recipe_step_number, i + 1) + " " + steps.get(i).getText();
            int index = i;
            list.addView(createRow(text, v -> {
                steps.remove(index);
                renderSteps();
            }));
        }
    }

    private View createRow(String text, View.OnClickListener onRemove) {
        int padding = getResources().getDimensionPixelSize(R.dimen.card_padding);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_surface_card);
        row.setPadding(padding, 0, 0, 0);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = getResources().getDimensionPixelSize(R.dimen.text_spacing);
        row.setLayoutParams(rowParams);

        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(ContextCompat.getColor(this, R.color.ink));
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_body));
        row.addView(label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        ImageButton remove = new ImageButton(this);
        remove.setImageResource(R.drawable.ic_remove);
        remove.setBackgroundResource(android.R.color.transparent);
        remove.setContentDescription(getString(R.string.new_recipe_remove));
        remove.setOnClickListener(onRemove);
        int size = getResources().getDimensionPixelSize(R.dimen.round_button_size);
        row.addView(remove, new LinearLayout.LayoutParams(size, size));

        return row;
    }

    public void saveRecipe(View view) {
        EditText nameInput = findViewById(R.id.nameInput);
        EditText minutesInput = findViewById(R.id.minutesInput);
        EditText servingsInput = findViewById(R.id.servingsInput);

        String name = nameInput.getText().toString().trim();
        int minutes = (int) parseNumber(minutesInput.getText().toString());
        int servings = (int) parseNumber(servingsInput.getText().toString());

        boolean valid = true;
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.new_recipe_error_name));
            valid = false;
        }
        if (minutes <= 0) {
            minutesInput.setError(getString(R.string.new_recipe_error_number));
            valid = false;
        }
        if (servings <= 0) {
            servingsInput.setError(getString(R.string.new_recipe_error_number));
            valid = false;
        }
        if (!valid) return;
        if (ingredients.isEmpty()) {
            Toast.makeText(this, R.string.new_recipe_error_ingredients, Toast.LENGTH_SHORT).show();
            return;
        }
        if (steps.isEmpty()) {
            Toast.makeText(this, R.string.new_recipe_error_steps, Toast.LENGTH_SHORT).show();
            return;
        }

        Recipe recipe = buildRecipe(name, minutes, servings);
        Intent result = new Intent();
        result.putExtra(EXTRA_NEW_RECIPE, recipe);
        setResult(RESULT_OK, result);
        finish();
    }

    private Recipe buildRecipe(String name, int minutes, int servings) {
        EditText descriptionInput = findViewById(R.id.descriptionInput);
        Spinner categorySpinner = findViewById(R.id.categorySpinner);

        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setDescription(descriptionInput.getText().toString().trim());
        recipe.setCategory(categorySpinner.getSelectedItem().toString());
        recipe.setRegion(getString(R.string.new_recipe_default_region));
        recipe.setMinutes(minutes);
        recipe.setDifficulty(getString(R.string.new_recipe_default_difficulty));
        recipe.setCalories(getString(R.string.new_recipe_default_calories));
        recipe.setServings(servings);
        recipe.setServingNames(
                getString(R.string.new_recipe_default_serving_singular),
                getString(R.string.new_recipe_default_serving_plural)
        );
        recipe.setImageUrl(photoUrl);
        recipe.setIngredients(new ArrayList<>(ingredients));
        recipe.setSteps(new ArrayList<>(steps));
        return recipe;
    }

    private double parseNumber(String text) {
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_recipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
