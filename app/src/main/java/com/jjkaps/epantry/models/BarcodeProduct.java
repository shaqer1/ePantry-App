package com.jjkaps.epantry.models;

import android.util.Log;

import com.jjkaps.epantry.models.ProductModels.DietFlag;
import com.jjkaps.epantry.models.ProductModels.DietInfo;
import com.jjkaps.epantry.models.ProductModels.DietLabel;
import com.jjkaps.epantry.models.ProductModels.Nutrient;
import com.jjkaps.epantry.models.ProductModels.ProductPackage;
import com.jjkaps.epantry.models.ProductModels.ProductPhoto;
import com.jjkaps.epantry.models.ProductModels.Serving;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BarcodeProduct  implements Serializable {
    private static final String TAG = "API_Process";
    private String barcode;
    private String name;
    private String brand;
    private String ingredients;
    private String storageType;
    private ProductPackage packageDetails;
    private Serving serving;
    private List<String> categories;
    private List<String> keywords;
    private String description;
    private List<String> palm_oil_ingredients;
    private List<String> ingredient_list;
    private List<String> brand_list;
    private List<String> allergens;
    private List<String> minerals;
    private List<String> traces;
    private List<String> vitamins;
    private ProductPhoto frontPhoto;
    private ProductPhoto nutritionPhoto;
    private ProductPhoto ingredientsPhoto;
    private List<Nutrient> nutrients;
    private int quantity;
    private String catalogReference;
    private String expDate;
    private String notes;
    private DietInfo dietInfo;
    private String userImage;
    private boolean favorite;

    public BarcodeProduct(){}

    /**
     * NOTE: Everything here should be serializable, to alow to be parsed from firebase
     * this is supposed to represent a catalog/fridge database object, The structure must be the same and concise */

    public BarcodeProduct(BarcodeProduct bp) {
        this.barcode = bp.barcode;
        this.name = bp.name;
        this.brand = bp.brand;
        this.ingredients = bp.ingredients;
        this.packageDetails = bp.packageDetails;
        this.serving = bp.serving;
        this.categories = bp.categories;
        this.keywords = bp.keywords;
        this.description = bp.description;
        this.palm_oil_ingredients = bp.palm_oil_ingredients;
        this.ingredient_list = bp.ingredient_list;
        this.brand_list = bp.brand_list;
        this.allergens = bp.allergens;
        this.minerals = bp.minerals;
        this.traces = bp.traces;
        this.vitamins = bp.vitamins;
        this.frontPhoto = bp.frontPhoto;
        this.nutritionPhoto = bp.nutritionPhoto;
        this.ingredientsPhoto = bp.ingredientsPhoto;
        this.catalogReference = bp.catalogReference;
        this.quantity = bp.quantity;
        this.expDate = bp.expDate;
        this.nutrients = bp.nutrients;
        this.dietInfo = bp.dietInfo;
        this.notes = bp.notes;
        this.userImage = bp.userImage;
        this.favorite = bp.favorite;
        this.storageType = bp.storageType;
    }

    public static BarcodeProduct getInstance(Serializable barcodeProduct) {
        return barcodeProduct == null ? null : barcodeProduct instanceof BarcodeProduct ? ((BarcodeProduct) barcodeProduct) : null;
    }

    public static BarcodeProduct getCatalogObj(BarcodeProduct bp) {
        BarcodeProduct bpNew = new BarcodeProduct(bp);
        bpNew.setQuantity(0);
        bpNew.setExpDate("");
        bpNew.setCatalogReference("");
        bpNew.setStorageType(null);
        return bpNew;
    }

    public static BarcodeProduct getFridgeObj(BarcodeProduct bp){
        BarcodeProduct bpNew = new BarcodeProduct(bp);
        bpNew.setFavorite(false);
        return bpNew;
    }

    private void setItems(String barcode, String name, String brand, String ingredients, ProductPackage packageDetails, Serving serving,
                            List<String> categories, List<Nutrient> nutrients, DietInfo df,
                                List<String> keywords, String description, List<String> palm_oil_ingredients, List<String> ingredient_list,
                                    List<String> brand_list, List<String> allergens, List<String> minerals, List<String> traces, List<String> vitamins,
                                        ProductPhoto frontPhoto, ProductPhoto nutritionPhoto, ProductPhoto ingredientsPhoto) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.ingredients = ingredients;
        this.packageDetails = packageDetails;
        this.serving = serving;
        this.categories = categories;
        this.keywords = keywords;
        this.description = description;
        this.palm_oil_ingredients = palm_oil_ingredients;
        this.ingredient_list = ingredient_list;
        this.brand_list = brand_list;
        this.allergens = allergens;
        this.minerals = minerals;
        this.traces = traces;
        this.vitamins = vitamins;
        this.frontPhoto = frontPhoto;
        this.nutritionPhoto = nutritionPhoto;
        this.ingredientsPhoto = ingredientsPhoto;
        this.catalogReference = "";
        this.quantity = 1;
        this.expDate = null;
        this.nutrients = nutrients;
        this.dietInfo = df;
        this.notes = "";
        this.userImage = "";
        this.favorite = false;
    }

    public static BarcodeProduct processJSON(JSONObject response, BarcodeProduct bp) {
        try {
            JSONObject item = response.getJSONArray("items").getJSONObject(0);
            String barcode = item.getString("barcode");
            String name = item.getString("name");
            String brand = item.getString("brand");
            String ingredients = item.getString("ingredients").equals("null") ? null : item.getString("ingredients");
            String description = item.getString("description");

            List<Nutrient> nutrients = getNutrientsArray(item.getJSONArray("nutrients"));

            JSONObject dietVegan = item.getJSONObject("diet_labels").getJSONObject("vegan");
            JSONObject dietVeg = item.getJSONObject("diet_labels").getJSONObject("vegetarian");
            JSONObject dietGluten = item.getJSONObject("diet_labels").getJSONObject("gluten_free");
            List <DietFlag> df = getDietFlagsArr(item.getJSONArray("diet_flags"));

            JSONObject servingObj = item.getJSONObject("serving");
            Serving serving = new Serving(servingObj.getString("size"), servingObj.getString("measurement_unit"), servingObj.getString("size_fulltext"));

            ProductPackage packageDetails = getPackage(item.getJSONObject("package"));
            DietInfo di = new DietInfo(DietLabel.getDietLabel(dietVegan), DietLabel.getDietLabel(dietVeg), DietLabel.getDietLabel(dietGluten), df);

            List<String> categories = getStringArr(item.getJSONArray("categories"));//TODO update all data
            List<String> keywords = getStringArr(item.getJSONArray("keywords"));
            List<String> palm_oil_ingredients = getStringArr(item.getJSONArray("palm_oil_ingredients"));
            List<String> ingredient_list = getStringArr(item.getJSONArray("ingredient_list"));
            List<String> brand_list = getStringArr(item.getJSONArray("brand_list"));
            List<String> allergens = getStringArr(item.getJSONArray("allergens"));
            List<String> minerals = getStringArr(item.getJSONArray("minerals"));
            List<String> traces = getStringArr(item.getJSONArray("traces"));
            List<String> vitamins = getStringArr(item.getJSONArray("vitamins"));

            ProductPhoto frontPhoto = getProductPhoto(item.getJSONObject("packaging_photos").getJSONObject("front"));
            ProductPhoto nutritionPhoto = getProductPhoto(item.getJSONObject("packaging_photos").getJSONObject("nutrition"));
            ProductPhoto ingredientsPhoto = getProductPhoto(item.getJSONObject("packaging_photos").getJSONObject("ingredients"));

            bp.setItems(barcode, name, brand, ingredients,
                            packageDetails, serving, categories, nutrients,
                                di, keywords, description,
                                    palm_oil_ingredients, ingredient_list, brand_list, allergens,
                                        minerals, traces, vitamins, frontPhoto, nutritionPhoto, ingredientsPhoto);
        } catch (JSONException e) {
            Log.d(TAG, "error during processing" + e.getMessage());//TODO handle
            e.printStackTrace();
        }
        return bp;
    }

    private static ProductPhoto getProductPhoto(JSONObject photo) throws JSONException {
        return new ProductPhoto(photo.getString("small"),photo.getString("thumb"), photo.getString("display"));
    }

    private static List<DietFlag> getDietFlagsArr(JSONArray diet_flags) throws JSONException {
        List<DietFlag> dfs = new ArrayList<>();
        for (int i = 0; i < diet_flags.length(); i++) {
            JSONObject df = diet_flags.getJSONObject(i);
            dfs.add(new DietFlag(df.getString("ingredient"),
                    df.getString("ingredient_description"),
                    df.getString("diet_label"),
                    !df.getString("is_compatible").equals("null") && df.getBoolean("is_compatible"),
                    !df.getString("compatibility_level").equals("null")?df.getInt("compatibility_level"):0,
                    df.getString("compatibility_description"),
                    !df.getString("is_allergen").equals("null") && df.getBoolean("is_allergen")));
        }
        return dfs;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setDietInfo(DietInfo dietInfo) {
        this.dietInfo = dietInfo;
    }

    public void setServing(Serving serving) {
        this.serving = serving;
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

    public Serving getServing() {
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

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCatalogReference() {
        return catalogReference;
    }

    public void setCatalogReference(String catalogReference) {
        this.catalogReference = catalogReference;
    }

    public DietInfo getDietInfo() {
        return dietInfo;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getBrand_list() {
        return brand_list;
    }

    public List<String> getPalm_oil_ingredients() {
        return palm_oil_ingredients;
    }

    public List<String> getIngredient_list() {
        return ingredient_list;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public List<String> getMinerals() {
        return minerals;
    }

    public List<String> getTraces() {
        return traces;
    }

    public List<String> getVitamins() {
        return vitamins;
    }

    public ProductPhoto getFrontPhoto() {
        return frontPhoto;
    }

    public ProductPhoto getNutritionPhoto() {
        return nutritionPhoto;
    }

    public ProductPhoto getIngredientsPhoto() {
        return ingredientsPhoto;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }
}
