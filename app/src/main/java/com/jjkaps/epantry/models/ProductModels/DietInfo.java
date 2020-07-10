package com.jjkaps.epantry.models.ProductModels;

import java.io.Serializable;
import java.util.List;

public class DietInfo implements Serializable {
    private DietLabel vegan;
    private DietLabel veg;
    private DietLabel gluten_free;
    private List<DietFlag> dietFlags;

    public DietInfo(){};
    public DietInfo(DietLabel vegan, DietLabel veg, DietLabel gluten_free, List<DietFlag> dietFlags) {

        this.vegan = vegan;
        this.veg = veg;
        this.gluten_free = gluten_free;
        this.dietFlags = dietFlags;
    }

    public DietLabel getVegan() {
        return vegan;
    }

    public DietLabel getVeg() {
        return veg;
    }

    public DietLabel getGluten_free() {
        return gluten_free;
    }

    public List<DietFlag> getDietFlags() {
        return dietFlags;
    }
}
