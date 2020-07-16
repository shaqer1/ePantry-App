package com.jjkaps.epantry.ui.Fridge;

import com.google.firebase.firestore.DocumentReference;
import com.jjkaps.epantry.models.BarcodeProduct;

public class FridgeItem {
    private String tvFridgeItemName;
    private String tvFridgeItemQuantity;
    private String tvFridgeItemNotes;
    private DocumentReference fridgeItemRef;
    private String docID;
    private BarcodeProduct barcodeProduct;

    public FridgeItem(String fridgeItemName, String fridgeItemQuantity, String fridgeItemNotes, DocumentReference fridgeItemRef, String id) {
        tvFridgeItemName = fridgeItemName;
        tvFridgeItemQuantity = fridgeItemQuantity;
        tvFridgeItemNotes = fridgeItemNotes;
        this.fridgeItemRef = fridgeItemRef;
        docID = id;
    }

    public String getTvFridgeItemName() {
        return tvFridgeItemName;
    }

    public String getTvFridgeItemQuantity() {
        return tvFridgeItemQuantity;
    }

    public String incTvFridgeItemQuantity() {
        tvFridgeItemQuantity = Integer.toString((Integer.parseInt(tvFridgeItemQuantity) + 1));
        return tvFridgeItemQuantity;
    }

    public String decTvFridgeItemQuantity() {
        tvFridgeItemQuantity = Integer.toString((Integer.parseInt(tvFridgeItemQuantity) - 1));
        return tvFridgeItemQuantity;
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
