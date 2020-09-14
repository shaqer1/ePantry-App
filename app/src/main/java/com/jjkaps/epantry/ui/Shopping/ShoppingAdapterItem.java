package com.jjkaps.epantry.ui.Shopping;

import com.jjkaps.epantry.models.ShoppingListItem;

import java.io.Serializable;

public class ShoppingAdapterItem  implements Serializable {
    private String docID;
    private ShoppingListItem shoppingListItem;

    public ShoppingAdapterItem(String docID, ShoppingListItem shoppingListItem) {
        this.docID = docID;
        this.shoppingListItem = shoppingListItem;
    }
    public ShoppingAdapterItem(){}

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public ShoppingListItem getShoppingListItem() {
        return shoppingListItem;
    }

    public void setShoppingListItem(ShoppingListItem shoppingListItem) {
        this.shoppingListItem = shoppingListItem;
    }
}
