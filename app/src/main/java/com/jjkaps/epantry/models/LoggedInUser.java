package com.jjkaps.epantry.models;

import com.google.firebase.firestore.CollectionReference;
import java.io.Serializable;

public class LoggedInUser implements Serializable {
    private String displayName;
    private String email;
    private CollectionReference catalogList;
    private CollectionReference fridgeList;
    private CollectionReference shoppingList;

    public LoggedInUser(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public LoggedInUser(){}

    public CollectionReference getCatalogList() {
        return catalogList;
    }

    public CollectionReference getShoppingList() {
        return shoppingList;
    }

    public CollectionReference getFridgeList() {
        return fridgeList;
    }
}
