package com.jjkaps.epantry.ui.ItemUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class AddFridgeToShopping extends AppCompatActivity {

    private BarcodeProduct bp;
    private static final String TAG = "AddItem";
    private TextView txtClose;
    private Button btDone;
    private Button cancel;
    private EditText inputItem;
    private EditText inputQtyItem;
    private String itemName;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference shopListRef;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fridge_to_shopping);

        txtClose =  findViewById(R.id.txt_close);
        btDone =  findViewById(R.id.bt_done);
        cancel = findViewById(R.id.cancel);
        inputQtyItem = findViewById(R.id.inputQuantityItem);
        inputItem = findViewById(R.id.inputItem);
        Bundle nameB = getIntent().getExtras();
        if (nameB != null) {
            itemName = nameB.getString("itemName");
        }
        initView();
        initText();
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //Firebase
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user != null) {
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
//            fridgeListRef = db.collection("users").document(user.getUid()).collection("fridgeList");
        }

        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get item
                final String item = inputItem.getText().toString();
                if (item.isEmpty()){
                    inputItem.setError("Can't leave name blank!");
                    return;
                }
                if (inputQtyItem.getText().toString().isEmpty()){
                    inputQtyItem.setError("Can't leave blank!");
                    return;
                }
                final int qty = Integer.parseInt(inputQtyItem.getText().toString());



                //Check if item exists (with case check), if not add the item.
                shopListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean itemNotExists = true;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.get("name").toString().toLowerCase().equals(item.toLowerCase())) {
                                    Toast toast = Toast.makeText(AddFridgeToShopping.this, item+" is already in Shopping List", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                    View vi = toast.getView();
                                    vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                                    TextView text = vi.findViewById(android.R.id.message);
                                    text.setTextColor(Color.BLACK);
                                    text.setTextSize(25);
                                    toast.show();

                                    itemNotExists=false;
                                    finish();
                                }
                            }
                            if (itemNotExists) {
                                Map<String, Object> shoppingListMap = new HashMap<>();
                                shoppingListMap.put("name", item);
                                shoppingListMap.put("quantity", qty);
                                shoppingListMap.put("checked", false);
                                shopListRef.add(shoppingListMap)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "onSuccess: "+item+" added.");
                                                Toast toast = Toast.makeText(AddFridgeToShopping.this, item+" added to Shopping List", Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                                View vi = toast.getView();
                                                vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                                                TextView text = vi.findViewById(android.R.id.message);
                                                text.setTextColor(Color.BLACK);
                                                text.setTextSize(25);
                                                toast.show();
                                                finish();
//                                                inputItem.setText(null);
//                                                inputQtyItem.setText(null);
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

    private void initText() {
        if(Utils.isNotNullOrEmpty(itemName)){
            inputItem.setText(itemName);
        }
    }
}

