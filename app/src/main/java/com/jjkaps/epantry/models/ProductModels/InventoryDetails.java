package com.jjkaps.epantry.models.ProductModels;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.Date;

@Keep
public class InventoryDetails implements Serializable {
    private Date expDate;
    private int quantity;

    public InventoryDetails(){
        // needed
    }

    public InventoryDetails(Date expDate, int quantity){
        this.expDate = expDate;
        this.quantity = quantity;
    }


    public Date getExpDate() {
        return expDate;
    }

    public void setExpDate(Date expDate) {
        this.expDate = expDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
