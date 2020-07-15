package com.jjkaps.epantry.models.ProductModels;

import java.io.Serializable;

public class Serving implements Serializable {
    private String size;
    private String measurement_unit;
    private String size_fulltext;

    public Serving(){}
    public Serving(String size, String measurement_unit, String size_fulltext) {

        this.size = size;
        this.measurement_unit = measurement_unit;
        this.size_fulltext = size_fulltext;
    }

    public Serving(String size, String measurement_unit) {
        this.size = size;
        this.measurement_unit = measurement_unit;
        this.size_fulltext = this.size + " " +this.measurement_unit;
    }

    public String getSize() {
        return size;
    }

    public String getMeasurement_unit() {
        return measurement_unit;
    }

    public String getSize_fulltext() {
        return size_fulltext;
    }
}
