package com.jjkaps.epantry.utils;

import com.jjkaps.epantry.models.ShoppingListItem;

import java.util.Comparator;

public class CustomSorter {
    private String sortMethod;
    private Comparator<ShoppingListItem> customShoppingSorter;
    public CustomSorter(String sortMethod) {
        this.sortMethod = sortMethod;
        //create sorter
         customShoppingSorter = new Comparator<ShoppingListItem>() {
            public int compare(ShoppingListItem o1, ShoppingListItem o2) {
                // Improve this to handle null publishedAt
                if(o1.isChecked() && !o2.isChecked()){ // o1 checked display that first
                    return 1;
                }else if (!o1.isChecked() && o2.isChecked()) { // o2 checked display that first
                    return -1;
                } else if(o1.isChecked() && o2.isChecked() && o1.getName().compareTo(o2.getName())==0){
                    return 0;
                }
                else if(o1.isChecked() && o2.isChecked() && o1.getName().compareTo(o2.getName())>0){
                    return 1;
                }
                else if(o1.isChecked() && o2.isChecked() && o1.getName().compareTo(o2.getName())>-1){
                    return -1;
                }
                else if(!o1.isChecked() && !o2.isChecked()){
                    return 0;
                }
                return 0;//they are equal
            }
        };
    }

    public Comparator<ShoppingListItem> getSorter() {
        return customShoppingSorter;
    }
}
