package com.jjkaps.epantry.utils;

import android.util.Log;

import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.models.FridgeItem;
import com.jjkaps.epantry.ui.Shopping.ShoppingAdapterItem;


import java.util.Comparator;
import java.util.Date;

public class CustomSorter {
    private static final String TAG = "CustomSorter";
    private final String sortMethod;
    private final Comparator<ShoppingAdapterItem> customShoppingSorter;
    public CustomSorter(final String sortMethod) {
        this.sortMethod = sortMethod;
        //create sorter
         customShoppingSorter = (oA1, oA2) -> {
             ShoppingListItem o1 = oA1.getShoppingListItem();
             ShoppingListItem o2 = oA2.getShoppingListItem();
             // Improve this to handle null publishedAt
             if(o1.isChecked() && !o2.isChecked()){ // o1 checked display that first
                 return 1;
             }else if (!o1.isChecked() && o2.isChecked()) { // o2 checked display that first
                 return -1;
             } else if(sortMethod.equals("Alpha")){
                 return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
             } else if (sortMethod.equals("Qty")){
                 Integer i1 = o1.getQuantity();
                 Integer i2 = o2.getQuantity();
                 return i1.compareTo(i2);
             }

             return 0;//they are equal
         };
    }

    private static final Comparator<FridgeItem> comparatorName = (fridgeItem, t1) -> Integer.compare(fridgeItem.getTvFridgeItemName().compareToIgnoreCase(t1.getTvFridgeItemName()), 0);
    private static final Comparator<FridgeItem> comparatorExp = (fridgeItem, fridgeItem2) -> {
        if(fridgeItem.getTvFridgeItemExpDate().equals("") && fridgeItem2.getTvFridgeItemExpDate().equals("")){
            return 0;
        }else if(fridgeItem.getTvFridgeItemExpDate().equals("")){
            return 1;
        } else if (fridgeItem2.getTvFridgeItemExpDate().equals("")){
            return -1;
        } else {
            try{
                if(Utils.isNotNullOrEmpty(fridgeItem.getBarcodeProduct()) && Utils.isNotNullOrEmpty(fridgeItem.getBarcodeProduct().getInventoryDetails().getExpDate())
                        && Utils.isNotNullOrEmpty(fridgeItem2.getBarcodeProduct()) && Utils.isNotNullOrEmpty(fridgeItem2.getBarcodeProduct().getInventoryDetails().getExpDate())){
                    Date d1 = fridgeItem.getBarcodeProduct().getInventoryDetails().getExpDate();
                    Date d2 = fridgeItem2.getBarcodeProduct().getInventoryDetails().getExpDate();
                    if(d1 != null && d2 != null && d2.compareTo(d1) < 0){
                        return 1;
                    }else if (d1 != null && d2 != null){
                        return -1;
                    }
                }
            }catch (Exception e){
                Log.d(TAG, "could not parse dates in fridge item exp");
            }
        }
        return 0;
    };
    private static final Comparator<FridgeItem> comparatorQuantity = Comparator.comparing(FridgeItem::getTvFridgeItemQuantity);

    public static Comparator<FridgeItem> getComparatorName() {
        return comparatorName;
    }

    public static Comparator<FridgeItem> getComparatorExp() {
        return comparatorExp;
    }

    public static Comparator<FridgeItem> getComparatorQuantity() {
        return comparatorQuantity;
    }

    public Comparator<ShoppingAdapterItem> getSorter() {
        return customShoppingSorter;
    }
}
