package com.jjkaps.epantry.models.ProductModels;

import java.io.Serializable;

public class Nutrient implements Serializable {
    private String name;
    private int per_100g;
    private String measurement_unit;
    private int rank;
    private int data_points;
    private String description;


    public Nutrient(String name, int per_100g, String measurement_unit, int rank, int data_points, String description) {
        this.name = name;
        this.per_100g = per_100g;
        this.measurement_unit = measurement_unit;
        this.rank = rank;
        this.data_points = data_points;
        this.description = description;
    }

    public Nutrient(){}

    public String getName() {
        return name;
    }

    public int getPer_100g() {
        return per_100g;
    }

    public String getMeasurement_unit() {
        return measurement_unit;
    }

    public int getRank() {
        return rank;
    }

    public int getData_points() {
        return data_points;
    }

    public String getDescription() {
        return description;
    }
}
