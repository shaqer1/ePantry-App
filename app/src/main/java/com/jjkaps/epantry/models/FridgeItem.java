package com.jjkaps.epantry.models;

import com.google.firebase.firestore.DocumentReference;
import com.jjkaps.epantry.utils.Utils;

public class FridgeItem {
    private String tvFridgeItemName;
    private String tvFridgeItemExpDate;
    private int tvFridgeItemQuantity;
    private String tvFridgeItemNotes;
    private DocumentReference fridgeItemRef;
    private String docID;
    private BarcodeProduct barcodeProduct;


    public FridgeItem(BarcodeProduct bp, DocumentReference fridgeItemRef) {
        tvFridgeItemName = bp.getName();
        tvFridgeItemExpDate = (Utils.isNotNullOrEmpty(bp.getInventoryDetails().getExpDate()))?
                                Utils.getExpDateStr(bp.getInventoryDetails().getExpDate().getTime()):"";
        tvFridgeItemQuantity = bp.getInventoryDetails().getQuantity();
        tvFridgeItemNotes = bp.getNotes();
        this.fridgeItemRef = fridgeItemRef;
        this.barcodeProduct = bp;
        this.docID = fridgeItemRef.getId();
    }

    public String getTvFridgeItemName() {
        return tvFridgeItemName;
    }

    public int getTvFridgeItemQuantity() {
        return tvFridgeItemQuantity;
    }
    public String getTvFridgeItemExpDate() {
        return tvFridgeItemExpDate;
    }

    public String getTvFridgeItemNotes() { return tvFridgeItemNotes; }

    public DocumentReference getFridgeItemRef() {
        return fridgeItemRef;
    }

    public BarcodeProduct getBarcodeProduct() {
        return barcodeProduct;
    }

    public void setBarcodeProduct(BarcodeProduct barcodeProduct) {
        this.barcodeProduct = barcodeProduct;
    }

    public String getDocID() {
        return docID;
    }
}
