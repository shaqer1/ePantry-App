package com.jjkaps.epantry.ui.Settings;

import com.jjkaps.epantry.models.BarcodeProduct;

import java.io.Serializable;

public class SettingFavItem implements Serializable {
    private BarcodeProduct barcodeProduct;
    private String docReference;
    private boolean favorite;



    public BarcodeProduct getBarcodeProduct() {
        return barcodeProduct;
    }

    public String getDocReference() {
        return docReference;
    }


    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public SettingFavItem(BarcodeProduct barcodeProduct, String docReference, boolean favorite) {
        this.barcodeProduct = barcodeProduct;
        this.docReference = docReference;
        this.favorite = favorite;
    }

}
