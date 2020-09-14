package com.jjkaps.epantry.ui.Shopping.AddItem;

import com.jjkaps.epantry.models.BarcodeProduct;

import java.io.Serializable;

public class SuggItem implements Serializable {
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

    public SuggItem(BarcodeProduct barcodeProduct, String docReference, int qty, boolean checked) {
        this.barcodeProduct = barcodeProduct;
        this.docReference = docReference;
        this.quantity = qty;
        this.checked = checked;
    }
}
