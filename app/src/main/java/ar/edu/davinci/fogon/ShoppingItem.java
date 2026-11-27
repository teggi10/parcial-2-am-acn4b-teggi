package ar.edu.davinci.fogon;

public class ShoppingItem {
    private final String text;
    private final String recipeName;
    private boolean checked;

    public ShoppingItem(String text, String recipeName) {
        this.text = text;
        this.recipeName = recipeName;
    }

    public String getText() {
        return text;
    }

    /** Null when the item was added by hand instead of coming from a recipe. */
    public String getRecipeName() {
        return recipeName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
