package com.jjkaps.epantry.models;



import java.io.Serializable;

public class ShoppingListItem implements Serializable {
    private static final String TAG = "ShoppingListItem";

    private String name;
    private int quantity;
    private boolean checked;
    private String docID;
    private String notes;


    public ShoppingListItem(String name, int quantity, boolean checked, String notes) {
        this.name = name;
        this.quantity = quantity;
        this.checked = checked;
        this.notes = notes;
    }
    public ShoppingListItem(){}

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

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

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getNotes() { return this.notes; }

    public void setNotes(String notes) { this.notes = notes; }
}
