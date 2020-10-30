package com.jjkaps.epantry.ui.Recipes;

import com.jjkaps.epantry.models.BarcodeProduct;

import java.io.Serializable;

public class BPAdapterItem implements Serializable {
    private BarcodeProduct barcodeProduct;
    private String docReference;

    public BPAdapterItem(BarcodeProduct barcodeProduct, String docReference){
        this.barcodeProduct = barcodeProduct;
        this.docReference = docReference;
    }

    public BPAdapterItem(){}

    public BarcodeProduct getBarcodeProduct() {
        return barcodeProduct;
    }

    public String getDocReference() {
        return docReference;
    }
}
