package com.jjkaps.epantry.ui.Fridge;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.utils.Utils;

public class FridgeItem {
    private String tvFridgeItemName;
    private String tvFridgeItemExpDate;
    private String tvFridgeItemQuantity;
    private String tvFridgeItemNotes;
    private DocumentReference fridgeItemRef;
    private String docID;
    private BarcodeProduct barcodeProduct;
    private Boolean fav = false;
    private FirebaseFirestore db;



    public FridgeItem(String fridgeItemName, String fridgeItemExpDate, String fridgeItemQuantity, String fridgeItemNotes, BarcodeProduct bp, DocumentReference fridgeItemRef, String id) {
        tvFridgeItemName = fridgeItemName;
        tvFridgeItemExpDate = fridgeItemExpDate;
        tvFridgeItemQuantity = fridgeItemQuantity;
        tvFridgeItemNotes = fridgeItemNotes;
        this.fridgeItemRef = fridgeItemRef;
        this.barcodeProduct = bp;
        this.fav = false;
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
