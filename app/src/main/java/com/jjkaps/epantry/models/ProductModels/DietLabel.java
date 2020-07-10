package com.jjkaps.epantry.models.ProductModels;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class DietLabel implements Serializable {
    private String name;
    private boolean is_compatible;
    private int compatibility_level;
    private int confidence;
    private String confidence_description;

    public DietLabel(String name, boolean is_compatible, int compatibility_level, int confidence, String confidence_description){
        this.name = name;
        this.is_compatible = is_compatible;
        this.compatibility_level = compatibility_level;
        this.confidence = confidence;
        this.confidence_description = confidence_description;
    };

    public static DietLabel getDietLabel(JSONObject dietLabel) throws JSONException {

        return new DietLabel(dietLabel.getString("name"), dietLabel.getBoolean("is_compatible"), dietLabel.getInt("compatibility_level"),
                                dietLabel.getInt("confidence"), dietLabel.getString("confidence_description"));
    }

    public String getName() {
        return name;
    }

    public boolean isIs_compatible() {
        return is_compatible;
    }

    public int getCompatibility_level() {
        return compatibility_level;
    }

    public int getConfidence() {
        return confidence;
    }

    public String getConfidence_description() {
        return confidence_description;
    }
}