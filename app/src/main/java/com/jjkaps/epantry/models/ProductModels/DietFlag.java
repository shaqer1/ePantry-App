package com.jjkaps.epantry.models.ProductModels;

import java.io.Serializable;

public class DietFlag implements Serializable {
    private String ingredient;
    private String ingredient_description;
    private String diet_label;
    private boolean is_compatible;
    private int compatibility_level;
    private String compatibility_description;
    private boolean is_allergen;

    public DietFlag(){};
    public DietFlag(String ingredient, String ingredient_description, String diet_label, boolean is_compatible, int compatibility_level, String compatibility_description, boolean is_allergen) {
        this.ingredient = ingredient;
        this.ingredient_description = ingredient_description;
        this.diet_label = diet_label;
        this.is_compatible = is_compatible;
        this.compatibility_level = compatibility_level;
        this.compatibility_description = compatibility_description;
        this.is_allergen = is_allergen;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getIngredient_description() {
        return ingredient_description;
    }

    public String getDiet_label() {
        return diet_label;
    }

    public boolean isIs_compatible() {
        return is_compatible;
    }

    public int getCompatibility_level() {
        return compatibility_level;
    }

    public String getCompatibility_description() {
        return compatibility_description;
    }

    public boolean isIs_allergen() {
        return is_allergen;
    }
}
