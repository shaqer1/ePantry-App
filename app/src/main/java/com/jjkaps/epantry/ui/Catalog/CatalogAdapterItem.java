package com.jjkaps.epantry.ui.Catalog;

import com.jjkaps.epantry.models.BarcodeProduct;

import java.io.Serializable;

public class CatalogAdapterItem implements Serializable {
    private BarcodeProduct barcodeProduct;
    private String docReference;

    public CatalogAdapterItem(BarcodeProduct barcodeProduct, String docReference){
        this.barcodeProduct = barcodeProduct;
        this.docReference = docReference;
    }

    public BarcodeProduct getBarcodeProduct() {
        return barcodeProduct;
    }

    public String getDocReference() {
        return docReference;
    }
}
