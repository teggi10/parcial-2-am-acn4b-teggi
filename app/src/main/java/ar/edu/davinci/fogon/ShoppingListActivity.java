package ar.edu.davinci.fogon;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {

    public void addCustomItem(View view) {
        EditText input = findViewById(R.id.newItemInput);
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;

        ShoppingList.addCustom(text);
        input.setText("");
        render();
    }

    public void clearCheckedItems(View view) {
        int removed = ShoppingList.clearChecked();
        if (removed == 0) return;
        String message = getResources().getQuantityString(R.plurals.shopping_cleared_toast, removed, removed);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        render();
    }

    private void render() {
        Map<String, List<ShoppingItem>> groups = ShoppingList.groupedByRecipe();
        renderSubtitle(groups);
        renderProgress();
        renderItems(groups);
    }

    private void renderSubtitle(Map<String, List<ShoppingItem>> groups) {
        List<String> recipes = new ArrayList<>();
        for (String name : groups.keySet()) {
            if (name != null) recipes.add(name);
        }
        TextView subtitle = findViewById(R.id.shoppingSubtitle);
        if (recipes.isEmpty()) {
            subtitle.setText(R.string.shopping_subtitle_empty);
        } else {
            String joined = TextUtils.join(getString(R.string.shopping_recipes_separator), recipes);
            subtitle.setText(getString(R.string.shopping_subtitle, joined));
        }
    }

    private void renderProgress() {
        int total = ShoppingList.size();
        int checked = ShoppingList.checkedCount();

        findViewById(R.id.shoppingProgressCard).setVisibility(total == 0 ? View.GONE : View.VISIBLE);
        findViewById(R.id.shoppingEmpty).setVisibility(total == 0 ? View.VISIBLE : View.GONE);
        findViewById(R.id.buttonClearChecked).setVisibility(checked == 0 ? View.GONE : View.VISIBLE);

        TextView text = findViewById(R.id.shoppingProgressText);
        text.setText(getString(R.string.shopping_progress, checked, total));

        ProgressBar bar = findViewById(R.id.shoppingProgress);
        bar.setMax(Math.max(total, 1));
        bar.setProgress(checked);
    }

    private void renderItems(Map<String, List<ShoppingItem>> groups) {
        LinearLayout container = findViewById(R.id.shoppingContainer);
        container.removeAllViews();

        for (Map.Entry<String, List<ShoppingItem>> group : groups.entrySet()) {
            String title = group.getKey() == null ? getString(R.string.shopping_others) : group.getKey();
            container.addView(createGroupTitle(title));
            for (ShoppingItem item : group.getValue()) {
                container.addView(createItemRow(item));
            }
        }
    }

    private TextView createGroupTitle(String title) {
        TextView view = new TextView(this);
        view.setText(title.toUpperCase(Locale.getDefault()));
        view.setTextColor(ContextCompat.getColor(this, R.color.muted));
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_overline));
        view.setLetterSpacing(0.06f);
        view.setPadding(0, dimen(R.dimen.section_spacing), 0, dimen(R.dimen.text_spacing));
        return view;
    }

    private CheckBox createItemRow(ShoppingItem item) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(item.getText());
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_body));
        checkBox.setMinHeight(dimen(R.dimen.row_min_height));
        checkBox.setBackgroundResource(R.drawable.bg_surface_card);
        int padding = dimen(R.dimen.card_padding);
        checkBox.setPadding(padding, 0, padding, 0);
        checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.herb)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dimen(R.dimen.text_spacing);
        checkBox.setLayoutParams(params);

        checkBox.setChecked(item.isChecked());
        applyCheckedStyle(checkBox, item.isChecked());
        checkBox.setOnCheckedChangeListener((button, isChecked) -> {
            item.setChecked(isChecked);
            applyCheckedStyle(checkBox, isChecked);
            renderProgress();
        });
        return checkBox;
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

    private int dimen(int res) {
        return getResources().getDimensionPixelSize(res);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNav.setup(this, BottomNav.Tab.SHOPPING);
    }
}
