package com.jjkaps.epantry.models;



import java.io.Serializable;

public class ShoppingListItem implements Serializable {
    private static final String TAG = "ShoppingListItem";

    private String name;
    private int quantity;
    private boolean checked;
    private String docReference;
    private String notes;

    public ShoppingListItem(String name, int quantity, boolean checked, String notes, String docReference){
        this.name = name;
        this.quantity = quantity;
        this.checked = checked;
        this.notes = notes;
        this.docReference = docReference;
    }

    /*public ShoppingListItem(String name, int quantity, boolean checked, String notes) {
        this(name, quantity, checked, notes, "");
    }*/
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

    public String getNotes() { return this.notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public String getDocReference() {
        return docReference;
    }

    public void setDocReference(String docReference) {
        this.docReference = docReference;
    }
}
