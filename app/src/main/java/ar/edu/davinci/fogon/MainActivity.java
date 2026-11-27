package ar.edu.davinci.fogon;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ApiRequest.Listener {

    public void retryLoad(View view) {
        loadRecipes();
    }

    private void loadRecipes() {
        showStatus(true, R.string.home_loading);
        new ApiRequest(this).execute(getString(R.string.recipes_url));
    }

    @Override
    public void onRecipesLoaded(RecipesResponse response) {
        RecipeRepository.setRemoteRecipes(response.getRecipes());
        findViewById(R.id.statusView).setVisibility(View.GONE);
        render();
    }

    @Override
    public void onRecipesError() {
        showStatus(false, R.string.home_error);
    }

    private void showStatus(boolean loading, int messageRes) {
        findViewById(R.id.statusView).setVisibility(View.VISIBLE);
        findViewById(R.id.statusProgress).setVisibility(loading ? View.VISIBLE : View.GONE);
        findViewById(R.id.retryButton).setVisibility(loading ? View.GONE : View.VISIBLE);
        TextView statusText = findViewById(R.id.statusText);
        statusText.setText(messageRes);
    }

    private void render() {
        renderFeatured();
        renderRecipes(RecipeRepository.getAll());
    }

    private void renderFeatured() {
        Recipe recipe = RecipeRepository.getFeatured();
        View card = findViewById(R.id.featuredCard);
        if (recipe == null) {
            card.setVisibility(View.GONE);
            return;
        }
        card.setVisibility(View.VISIBLE);
        card.setClipToOutline(true);

        TextView name = findViewById(R.id.featuredName);
        name.setText(recipe.getName());

        TextView meta = findViewById(R.id.featuredMeta);
        meta.setText(getString(
                R.string.featured_meta,
                RecipeFormat.time(this, recipe.getMinutes()),
                recipe.getDifficulty().toLowerCase(Locale.getDefault()),
                recipe.getServings(),
                recipe.getServingName(recipe.getServings())
        ));

        ImageView image = findViewById(R.id.featuredImage);
        if (recipe.hasImage()) ImageDownloader.load(image, recipe.getImageUrl());
    }

    private void renderRecipes(List<Recipe> recipes) {
        LinearLayout container = findViewById(R.id.recipesContainer);
        container.removeAllViews();

        TextView count = findViewById(R.id.recipesCount);
        count.setText(getResources().getQuantityString(R.plurals.home_recipes_count, recipes.size(), recipes.size()));

        if (recipes.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.home_empty);
            empty.setTextColor(ContextCompat.getColor(this, R.color.muted));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_body));
            empty.setPadding(0, dimen(R.dimen.section_spacing), 0, 0);
            container.addView(empty);
            return;
        }

        for (Recipe recipe : recipes) {
            container.addView(createRecipeCard(recipe));
        }
    }

    private View createRecipeCard(Recipe recipe) {
        int padding = dimen(R.dimen.card_padding);
        int gap = dimen(R.dimen.step_gap);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_surface_card);
        card.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.topMargin = dimen(R.dimen.card_gap);
        card.setLayoutParams(cardParams);

        ImageView thumb = new ImageView(this);
        thumb.setBackgroundResource(R.drawable.bg_thumb);
        thumb.setClipToOutline(true);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        if (recipe.hasImage()) {
            ImageDownloader.load(thumb, recipe.getImageUrl());
        } else {
            thumb.setScaleType(ImageView.ScaleType.CENTER);
            thumb.setImageResource(R.drawable.ic_photo_placeholder);
        }
        int thumbSize = dimen(R.dimen.thumb_size);
        card.addView(thumb, new LinearLayout.LayoutParams(thumbSize, thumbSize));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textsParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        textsParams.setMarginStart(gap);
        textsParams.setMarginEnd(gap);
        card.addView(texts, textsParams);

        TextView name = new TextView(this);
        name.setText(recipe.getName());
        name.setTextColor(ContextCompat.getColor(this, R.color.ink));
        name.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle));
        name.setTypeface(Typeface.DEFAULT_BOLD);
        texts.addView(name);

        TextView meta = new TextView(this);
        meta.setText(RecipeFormat.meta(this, recipe));
        meta.setTextColor(ContextCompat.getColor(this, R.color.muted));
        meta.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_body));
        texts.addView(meta);

        ImageView chevron = new ImageView(this);
        chevron.setImageResource(R.drawable.ic_chevron_right);
        chevron.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        card.addView(chevron);

        return card;
    }

    private int dimen(int res) {
        return getResources().getDimensionPixelSize(res);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNav.setup(this, BottomNav.Tab.HOME);

        if (RecipeRepository.isLoaded()) {
            findViewById(R.id.statusView).setVisibility(View.GONE);
            render();
        } else {
            loadRecipes();
        }
    }
}
