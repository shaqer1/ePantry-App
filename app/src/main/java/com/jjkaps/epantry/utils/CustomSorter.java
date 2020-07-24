package com.jjkaps.epantry.utils;

import com.jjkaps.epantry.models.ShoppingListItem;


import java.util.Comparator;

public class CustomSorter {
    private String sortMethod;
    private Comparator<ShoppingListItem> customShoppingSorter;
    public CustomSorter(final String sortMethod) {
        this.sortMethod = sortMethod;
        //create sorter
         customShoppingSorter = new Comparator<ShoppingListItem>() {
            public int compare(ShoppingListItem o1, ShoppingListItem o2) {
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
            }
        };
    }
    public Comparator<ShoppingListItem> getSorter() {
        return customShoppingSorter;
    }
}
