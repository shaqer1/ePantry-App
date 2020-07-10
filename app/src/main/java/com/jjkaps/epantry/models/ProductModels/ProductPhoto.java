package com.jjkaps.epantry.models.ProductModels;

import java.io.Serializable;

public class ProductPhoto implements Serializable {

    private String small;
    private String thumb;
    private String display;

    public ProductPhoto(){}

    public ProductPhoto(String small, String thumb, String display) {
        this.small = small;
        this.thumb = thumb;
        this.display = display;
    }

    public String getSmall() {
        return small;
    }

    public String getThumb() {
        return thumb;
    }

    public String getDisplay() {
        return display;
    }
}
