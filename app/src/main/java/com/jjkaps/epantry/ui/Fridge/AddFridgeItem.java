package com.jjkaps.epantry.ui.Fridge;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.jjkaps.epantry.models.ProductModels.DietFlag;
import com.jjkaps.epantry.models.ProductModels.DietInfo;
import com.jjkaps.epantry.models.ProductModels.DietLabel;
import com.jjkaps.epantry.models.ProductModels.Serving;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddFridgeItem extends AppCompatActivity {
    private static final String TAG = "AddFridgeManual";
    private TextView txtClose;
    private String item;
    private String quantity;
    private String expiration;
    private TextView addedExpiration;
    private EditText brandTxt, servingSize, servingUnit, ingredientsTxt;
    private Chip veganChip, vegChip, glutenChip;
    private SimpleDateFormat simpleDateFormat;
    private Button btDone;



    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    private FirebaseFirestore db;
    private EditText addedItem;
    private EditText addedQuantity;
    private CollectionReference fridgeListRef;
    private CollectionReference catalogListRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fridge_item);
        initView();


        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        //Firebase
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user != null) {
            fridgeListRef = db.collection("users").document(user.getUid()).collection("fridgeList");
            catalogListRef = db.collection("users").document(user.getUid()).collection("catalogList");
        }

        txtClose =  findViewById(R.id.txt_close);
        btDone = findViewById(R.id.bt_done);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(2);
                finish();
            }
        });
        addedItem = findViewById(R.id.inputItem);
        addedQuantity = findViewById(R.id.inputQuantity);
        addedExpiration = findViewById(R.id.inputExpiration);
        brandTxt = findViewById(R.id.brand_text);
        servingSize = findViewById(R.id.serving_size);
        servingUnit = findViewById(R.id.serving_unit);
        veganChip = findViewById(R.id.vegan_chip);
        vegChip = findViewById(R.id.veg_chip);
        glutenChip = findViewById(R.id.gluten_chip);
        ingredientsTxt = findViewById(R.id.ingredients);

        addedExpiration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(addedExpiration);
            }
        });

        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get item
                item = addedItem.getText().toString().trim();
                quantity = addedQuantity.getText().toString().trim();
                expiration = addedExpiration.getText().toString().trim();

                // verify quantity is valid
                Pattern containsNum = Pattern.compile("^[0-9]+$");
                Matcher isNum = containsNum.matcher(quantity);
                // if quantity is invalid, make the user reenter
                if ((quantity.equals("")) || !isNum.find() ||
                        ((Integer.parseInt(quantity) <= 0))) {
                    Toast.makeText(AddFridgeItem.this, "Quantity must be greater than zero!", Toast.LENGTH_SHORT).show();
                    addedQuantity.setText(null); // resets just the quantity field
                }
                else {

                    // check serving fields
                    if(servingSize.getText().length() > 0 ^ servingUnit.getText().length() > 0){
                        servingSize.setError(servingSize.getText().length() > 0?null:"Both serving fields must be filled");
                        servingUnit.setError(servingUnit.getText().length() > 0?null:"Both serving fields must be filled");
                    }

                    //check if item exist
                    fridgeListRef.whereEqualTo("name", item).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && task.getResult().size()!=0){
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Toast.makeText(AddFridgeItem.this, item+" Exists!", Toast.LENGTH_SHORT).show();
                                    }
                                    addedItem.setText(null);
                                    addedQuantity.setText(null);
                                    addedExpiration.setText(null);

                                }
                                //if not exist then add
                                else {
                                    //check if item is null
                                    if (item.length() == 0) {
                                        Toast.makeText(AddFridgeItem.this, "Item can't be null!", Toast.LENGTH_SHORT).show();
                                    }
                                    //add non-null item
                                    if (item.length() != 0 ){
                                        //ADD TO CATALOG AS WELL
                                        final BarcodeProduct bp = new BarcodeProduct();
                                        bp.setName(item);
                                        bp.setBrand(brandTxt.getText().toString());
                                        bp.setIngredients(ingredientsTxt.getText().toString());
                                        DietInfo di = new DietInfo(new DietLabel("Vegan", veganChip.isChecked(), 2, true, "verified by user"),
                                                new DietLabel("Gluten Free", glutenChip.isChecked(), 2, true, "verified by user"),
                                                new DietLabel("Vegetarian", vegChip.isChecked(), 2, true, "verified by user"), new ArrayList<DietFlag>());
                                        bp.setDietInfo(di);
                                        if(servingSize.getText().length() > 0 && servingUnit.getText().length() > 0){
                                            bp.setServing(new Serving(servingSize.getText().toString(), servingUnit.getText().toString()));
                                        }

                                    catalogListRef.add(bp)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d(TAG, "onSuccess: "+item+" added.");
                                                    bp.setQuantity(Integer.parseInt(quantity));
                                                    bp.setExpDate(expiration);
                                                    bp.setCatalogReference(documentReference.getPath());
                                                    fridgeListRef.add(bp)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    Log.d(TAG, "onSuccess: "+item+" added.");
                                                                    Toast.makeText(AddFridgeItem.this, item+" added to fridge", Toast.LENGTH_SHORT).show();
                                                                    addedItem.setText(null);
                                                                    addedQuantity.setText(null);
                                                                    brandTxt.getText().clear();
                                                                    servingSize.getText().clear();
                                                                    servingUnit.getText().clear();
                                                                    glutenChip.setChecked(false);
                                                                    veganChip.setChecked(false);
                                                                    vegChip.setChecked(false);
                                                                    ingredientsTxt.getText().clear();
                                                                    addedExpiration.setText(R.string.exp_date_hint);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "onFailure: ",e);
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: ",e);
                                                }
                                            });
                                    //txtNullList.setVisibility(View.INVISIBLE);
                                    //TODO: REFRESH PAGE TO LOAD ADDED ITEMS
                                }
                            }
                        }
                    }
                });
            }
        });

    }

    private void initView() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int) (dm.widthPixels/* *0.8*/);
        int height = (int) (dm.heightPixels - (dm.heightPixels*0.15));
        getWindow().setLayout(width, height);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
    }

    private void showDateDialog(final TextView date) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
//                simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                date.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };
        new DatePickerDialog(this, dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}