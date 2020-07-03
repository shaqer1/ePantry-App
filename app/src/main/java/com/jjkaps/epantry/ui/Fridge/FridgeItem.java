package com.jjkaps.epantry.ui.Fridge;

public class FridgeItem {
    private String tvFridgeItemName;
    private String tvFridgeItemQuantity;
    private String tvFridgeItemNotes;

    public FridgeItem(String fridgeItemName, String fridgeItemQuantity, String fridgeItemNotes) {
        tvFridgeItemName = fridgeItemName;
        tvFridgeItemQuantity = fridgeItemQuantity;
        tvFridgeItemNotes = fridgeItemNotes;
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

}
