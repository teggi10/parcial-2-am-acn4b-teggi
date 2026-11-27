package ar.edu.davinci.fogon;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

public class BottomNav {
    public enum Tab { HOME, SHOPPING }

    public static void setup(Activity activity, Tab current) {
        highlight(activity, R.id.navHomeIcon, R.id.navHomeLabel, current == Tab.HOME);
        highlight(activity, R.id.navShoppingIcon, R.id.navShoppingLabel, current == Tab.SHOPPING);

        activity.findViewById(R.id.navHome).setOnClickListener(v -> {
            if (current == Tab.HOME) return;
            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        });

        activity.findViewById(R.id.navShopping).setOnClickListener(v -> {
            if (current == Tab.SHOPPING) return;
            Intent intent = new Intent(activity, ShoppingListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        });
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
