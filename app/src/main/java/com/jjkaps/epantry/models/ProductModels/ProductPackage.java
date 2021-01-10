package com.jjkaps.epantry.models.ProductModels;

import java.io.Serializable;

public class ProductPackage implements Serializable {
    private int quantity;
    private String size;

    public ProductPackage(){}
    public ProductPackage(int quantity, String size){
        this.quantity = quantity;
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSize() {
        return size;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
