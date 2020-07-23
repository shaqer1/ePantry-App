package com.jjkaps.epantry.ui.Shopping;

import com.jjkaps.epantry.models.BarcodeProduct;

import java.io.Serializable;

public class FavItem implements Serializable {
    private BarcodeProduct barcodeProduct;
    private String docReference;
    private int quantity;
    private boolean checked;



    public BarcodeProduct getBarcodeProduct() {
        return barcodeProduct;
    }

    public String getDocReference() {
        return docReference;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public FavItem(BarcodeProduct barcodeProduct, String docReference, int quantity, boolean checked) {
        this.barcodeProduct = barcodeProduct;
        this.docReference = docReference;
        this.quantity = quantity;
        this.checked = checked;
    }

}
