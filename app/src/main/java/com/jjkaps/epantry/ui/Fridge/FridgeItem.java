package com.jjkaps.epantry.ui.Fridge;

import com.google.firebase.firestore.DocumentReference;
import com.jjkaps.epantry.models.BarcodeProduct;

public class FridgeItem {
    private String tvFridgeItemName;
    private String tvFridgeItemExpDate;
    private String tvFridgeItemQuantity;
    private String tvFridgeItemNotes;
    private DocumentReference fridgeItemRef;
    private String docID;
    private BarcodeProduct barcodeProduct;
    private Boolean fav = false;



    public FridgeItem(String fridgeItemName, String fridgeItemExpDate, String fridgeItemQuantity, String fridgeItemNotes, BarcodeProduct bp, DocumentReference fridgeItemRef, String id, Boolean favorite) {
        tvFridgeItemName = fridgeItemName;
        tvFridgeItemExpDate = fridgeItemExpDate;
        tvFridgeItemQuantity = fridgeItemQuantity;
        tvFridgeItemNotes = fridgeItemNotes;
        this.fridgeItemRef = fridgeItemRef;
        this.barcodeProduct = bp;
        docID = id;
        fav = favorite;
    }

    public String getTvFridgeItemName() {
        return tvFridgeItemName;
    }

    public String getTvFridgeItemQuantity() {
        return tvFridgeItemQuantity;
    }
    public String getTvFridgeItemExpDate() {
        return tvFridgeItemExpDate;
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

    public void setFav(Boolean fav) {
        this.fav = fav;
    }

    public Boolean getFav() {
        return this.fav;
    }

    public String getDocID() {
        return docID;
    }
}
