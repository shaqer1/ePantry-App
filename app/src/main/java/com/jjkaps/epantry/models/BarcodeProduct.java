package com.jjkaps.epantry.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.jjkaps.epantry.models.ProductModels.Nutrient;
import com.jjkaps.epantry.models.ProductModels.ProductPackage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarcodeProduct  implements Serializable {
    private static final String TAG = "API_Process";
    private String barcode;
    private String name;
    private String brand;
    private String ingredients;
    private ProductPackage packageDetails;
    private Map<String, String> serving;
    private List<String> categories;
    private List<Nutrient> nutrients;
    private int quantity;
    private DocumentReference catalogReference;
    private Date expDate;

    public BarcodeProduct(){}
    private void setItems(String barcode, String name, String brand, String ingredients, ProductPackage packageDetails, Map<String, String> serving, List<String> categories, List<Nutrient> nutrients) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.ingredients = ingredients;
        this.packageDetails = packageDetails;
        this.serving = serving;
        this.categories = categories;
        this.catalogReference = null;
        this.quantity = 1;
        this.expDate = null;
        this.nutrients = nutrients;
    }

    public static BarcodeProduct processJSON(JSONObject response, BarcodeProduct bp) {
        try {
            JSONObject item = response.getJSONArray("items").getJSONObject(0);
            String barcode = item.getString("barcode");
            String name = item.getString("name");
            String brand = item.getString("brand");
            String ingredients = item.getString("ingredients").equals("null") ? null : item.getString("ingredients");
            ProductPackage packageDetails = getPackage(item.getJSONObject("package"));
            JSONObject servingObj = item.getJSONObject("serving");
            Map<String, String> serving = new HashMap<>();
            serving.put("size", servingObj.getString("size"));
            serving.put("measurement_unit", servingObj.getString("measurement_unit"));
            serving.put("size_fulltext", servingObj.getString("size_fulltext"));
            List<String>  categories = getStringArr(item.getJSONArray("categories"));//TODO update all data
            List<Nutrient> nutrients = getNutrientsArray(item.getJSONArray("nutrients"));

            bp.setItems(barcode, name, brand, ingredients, packageDetails, serving, categories, nutrients);
        } catch (JSONException e) {
            Log.d(TAG, "error during processing" + e.getMessage());
            e.printStackTrace();
        }
        return bp;
    }

    private static ProductPackage getPackage(JSONObject aPackage) throws JSONException {
        return new ProductPackage(aPackage.getString("quantity").equals("null")?0:aPackage.getInt("quantity"), aPackage.getString("size"));
    }

    private static List<Nutrient> getNutrientsArray(JSONArray nutrients) throws JSONException {
        ArrayList<Nutrient> n = new ArrayList<>();
        for (int i = 0; i < nutrients.length(); i++) {
            JSONObject nutrient = nutrients.getJSONObject(i);
            n.add(new Nutrient(nutrient.getString("name"),
                                nutrient.getString("per_100g").equals("null") ? 0 : nutrient.getInt("per_100g"),
                                nutrient.getString("measurement_unit"),
                                nutrient.getString("rank").equals("null") ? 0 : nutrient.getInt("rank"),
                                nutrient.getString("data_points").equals("null") ? 0 : nutrient.getInt("data_points"),
                                nutrient.getString("description")));
        }
        return n;
    }

    private static List<String> getStringArr(JSONArray categories) throws JSONException {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < categories.length(); i++) {
            a.add(categories.getString(i));
        }
        return a;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getIngredients() {
        return ingredients;
    }

    public ProductPackage getPackageDetails() {
        return packageDetails;
    }

    public Map<String, String> getServing() {
        return serving;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<Nutrient> getNutrients() {
        return nutrients;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getExpDate() {
        return expDate;
    }

    public void setExpDate(Date expDate) {
        this.expDate = expDate;
    }

    public DocumentReference getCatalogReference() {
        return catalogReference;
    }

    public void setCatalogReference(DocumentReference catalogReference) {
        this.catalogReference = catalogReference;
    }
}
