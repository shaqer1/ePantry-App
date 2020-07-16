package com.jjkaps.epantry.ui.ItemUI;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.ui.Shopping.AddShoppingItem;
import com.jjkaps.epantry.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemActivity extends AppCompatActivity {

    /*
    Image
    Name
    quantity
    expiration
    Brand <text view>
    Ingredients
    package details
    Serving details
    diet chips
    palm oil ingredients
    notes
    nutrition info (maybe image)//TODO nutrition info
    */
    private BarcodeProduct bp;
    private ImageView imageIV;
    private TextView nameTV, quantityTV,  brandTV, ingredientsTV, pkgSizeTV, pkgQtyTV, srvSizeTV, srvUnitTV, palmOilIngredTV;
    private EditText notesET;
    private Button updateItemBT, addShoppingListBT;
    private Chip veganChip, vegChip, glutenChip;
    private String docRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference shopListRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        //get bp object
        this.bp = BarcodeProduct.getInstance(getIntent().getSerializableExtra("barcodeProduct"));
        //get doc ref
        this.docRef = getIntent().getStringExtra("docID");
        if(docRef != null){
            //Firebase
            db = FirebaseFirestore.getInstance();
        }
        //set action bar name
        if(this.getSupportActionBar() != null){
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setDisplayShowCustomEnabled(true);
            //getSupportActionBar().setIcon(new ColorDrawable(getColor(R.color.colorWhite)));
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);

            //View view = getSupportActionBar().getCustomView();
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            TextView name = findViewById(R.id.name);
            Button backButton = findViewById(R.id.txt_close);
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            name.setText(bp != null ? bp.getName().substring(0, Math.min(bp.getName().length(), 15)) : "Item Info");
        }
        initView();
        if(bp != null){
            initText();
        }

        // update item info button
        updateItemBT = findViewById(R.id.bt_updateItem);
        updateItemBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(db != null && docRef != null && Utils.isNotNullOrEmpty(notesET.getText().toString().trim())){
                    db.document(docRef).update("notes", notesET.getText().toString()); // update notes
                    // todo: Sprint 3 - add more fields to be edited
                }
            }
        });

        // add item to shopping list button
        addShoppingListBT = findViewById(R.id.bt_addShoppingList);
        addShoppingListBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo: @kira here is the button if you want to use it. Feel free to disregard the code below - I got it from AddShoppingItem.java.

                // get item
                final String item = nameTV.getText().toString();
                // user entered quantity
                /*
                if (inputQtyItem.getText().toString().isEmpty()){
                    inputQtyItem.setError("Can't leave blank!");
                    return;
                }
                final int qty = Integer.parseInt(inputQtyItem.getText().toString());
                 */

                /*
                // query the shopping list
                CollectionReference shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
                //check if item exist
                shopListRef.whereEqualTo("name", item).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().size()!=0){
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Toast.makeText(ItemActivity.this, "This item is ", Toast.LENGTH_SHORT).show();
                                }
                                inputItem.setText(null);
                            }
                            else {
                                Map<String, Object> shoppingListMap = new HashMap<>();
                                shoppingListMap.put("name", item);
                                shoppingListMap.put("quantity", qty);
                                shoppingListMap.put("checked", false);
                                shopListRef.add(shoppingListMap)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "onSuccess: "+item+" added.");
                                                Toast.makeText(AddShoppingItem.this, item+" Added", Toast.LENGTH_SHORT).show();
                                                inputItem.setText(null);
                                                inputQtyItem.setText(null);
                                                //getListItems();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: ",e);
                                            }
                                        });
                                Intent i = new Intent();
                                i.putExtra("HIDE_NAV", true);
                                setResult(2, i);// this lets activity know to hide the null bar
                                //txtNullList.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });

                 */
            }
        });


    }

    private void initView() {
        imageIV = findViewById(R.id.item_image);
        nameTV = findViewById(R.id.item_name);
        quantityTV = findViewById(R.id.item_quantity);
        brandTV = findViewById(R.id.item_brand);
        ingredientsTV = findViewById(R.id.item_ingredients);
        pkgSizeTV = findViewById(R.id.item_pkg_size);
        pkgQtyTV = findViewById(R.id.item_pkg_qty);
        srvSizeTV = findViewById(R.id.item_srv_sze);
        srvUnitTV = findViewById(R.id.item_srv_unit);
        notesET = findViewById(R.id.item_notes);
        veganChip = findViewById(R.id.vegan_chip);
        vegChip = findViewById(R.id.veg_chip);
        glutenChip = findViewById(R.id.gluten_chip);
        palmOilIngredTV = findViewById(R.id.palm_oil_ingr);

    }

    private void initText() {
        /*set photo*/
        if(Utils.isNotNullOrEmpty(bp.getFrontPhoto()) && Utils.isNotNullOrEmpty(bp.getFrontPhoto().getDisplay())){
            Picasso.get().load(bp.getFrontPhoto().getDisplay()).into(imageIV);
        }
        /*set name*/
        if(Utils.isNotNullOrEmpty(bp.getName())){
            nameTV.setText(bp.getName());
        }
        /*set quantity*/
        if(Utils.isNotNullOrEmpty(bp.getQuantity())){
            quantityTV.setText(String.valueOf(bp.getQuantity()));
        }
        /*brand*/
        if(Utils.isNotNullOrEmpty(bp.getBrand())){
            brandTV.setText(bp.getBrand());
        }else{
            findViewById(R.id.brand_til).setVisibility(View.GONE);
        }
        /*ingredients*/
        if(Utils.isNotNullOrEmpty(bp.getIngredients())){
            ingredientsTV.setText(bp.getIngredients());
        }else{
            findViewById(R.id.ingredients_til).setVisibility(View.GONE);
        }
        /*notes*/
        if(Utils.isNotNullOrEmpty(bp.getNotes())){
            notesET.setText(bp.getNotes());
        }
        notesET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    if(db != null && docRef != null && Utils.isNotNullOrEmpty(notesET.getText().toString().trim())){
                        db.document(docRef).update("notes", notesET.getText().toString());
                    }
                }
                return false;
            }
        });
        /*gluten chip*/
        if(Utils.isNotNullOrEmpty(bp.getDietInfo().getGluten_free())){
            if(Utils.isNotNullOrEmpty(bp.getDietInfo().getGluten_free().isIs_compatible())){
                glutenChip.setChecked(bp.getDietInfo().getGluten_free().isIs_compatible());
            }
        }else{
            findViewById(R.id.dietView).setVisibility(View.GONE);
        }
        /*vegetarian chip*/
        if(Utils.isNotNullOrEmpty(bp.getDietInfo().getVeg())){
            if(Utils.isNotNullOrEmpty(bp.getDietInfo().getVeg().isIs_compatible())){
                vegChip.setChecked(bp.getDietInfo().getVeg().isIs_compatible());
            }
        }else{
            findViewById(R.id.dietView).setVisibility(View.GONE);
        }
        /*vegan chip*/
        if(Utils.isNotNullOrEmpty(bp.getDietInfo().getVegan())){
            if(Utils.isNotNullOrEmpty(bp.getDietInfo().getVegan().isIs_compatible())){
                veganChip.setChecked(bp.getDietInfo().getVegan().isIs_compatible());
            }
        }else{
            findViewById(R.id.dietView).setVisibility(View.GONE);
        }
        /*pkg*/
        if(Utils.isNotNullOrEmpty(bp.getPackageDetails()) && Utils.isNotNullOrEmpty(bp.getPackageDetails().getQuantity()) && Utils.isNotNullOrEmpty(bp.getPackageDetails().getSize())){
            pkgQtyTV.setText(String.valueOf(bp.getPackageDetails().getQuantity()));
            pkgSizeTV.setText(bp.getPackageDetails().getSize());
        }else{
            findViewById(R.id.package_til).setVisibility(View.GONE);
        }
        /*Serving*/
        if(Utils.isNotNullOrEmpty(bp.getServing()) && Utils.isNotNullOrEmpty(bp.getServing().getSize()) && Utils.isNotNullOrEmpty(bp.getServing().getMeasurement_unit())){
            srvSizeTV.setText(bp.getServing().getSize());
            srvUnitTV.setText(bp.getServing().getMeasurement_unit());
        }else if (Utils.isNotNullOrEmpty(bp.getServing()) && Utils.isNotNullOrEmpty(bp.getServing().getSize_fulltext())){
            srvSizeTV.setText(bp.getServing().getSize_fulltext());
            findViewById(R.id.item_srv_unit).setVisibility(View.GONE);
        }else{
            findViewById(R.id.srv_til).setVisibility(View.GONE);
        }
        /*palm oil chip*/
        if(Utils.isNotNullOrEmpty(bp.getPalm_oil_ingredients()) && bp.getPalm_oil_ingredients().size() > 0){
            palmOilIngredTV.setText(getStringArr(bp.getPalm_oil_ingredients()));
        }else{
            findViewById(R.id.palm_oil_ingr_til).setVisibility(View.GONE);
        }
    }

    private String getStringArr(List<String> palm_oil_ingredients) {
        StringBuilder s = new StringBuilder();
        for(String x : palm_oil_ingredients){
            s.append(x).append(", ");
        }
        return s.toString().substring(0,s.length()-2);
    }
}