package ar.edu.davinci.fogon;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private double quantity;
    private String singular;
    private String plural;
    private boolean checked;

    public Ingredient() {
    }

    public Ingredient(double quantity, String singular, String plural) {
        this.quantity = quantity;
        this.singular = singular;
        this.plural = plural;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getQuantityFor(int portions, int basePortions) {
        return quantity * portions / basePortions;
    }

    public boolean isToTaste() {
        return quantity == 0;
    }

    public String getSingular() {
        return singular;
    }

    public String getPlural() {
        return plural;
    }

    public String getNameFor(double amount) {
        return amount <= 1 ? singular : plural;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
