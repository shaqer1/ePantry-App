package com.jjkaps.epantry.ui.Fridge;

public class FridgeItem {
    private String tvFridgeItemName;
    private String tvFridgeItemQuantity;

    public FridgeItem(String fridgeItemName, String fridgeItemQuantity) {
        tvFridgeItemName = fridgeItemName;
        tvFridgeItemQuantity = fridgeItemQuantity;
    }

    public String getTvFridgeItemName() {
        return tvFridgeItemName;
    }

    public String getTvFridgeItemQuantity() {
        return tvFridgeItemQuantity;
    }

}
