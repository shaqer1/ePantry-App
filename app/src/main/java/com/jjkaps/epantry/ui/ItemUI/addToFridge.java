package com.jjkaps.epantry.ui.ItemUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class addToFridge extends AppCompatActivity {

    private BarcodeProduct bp;
    private static final String TAG = "AddItem";
    private TextView txtClose;
    private Button btDone;
    private Button cancel;
    private EditText inputItem;
    private EditText inputQtyItem;
    private String itemName;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference fridgeListRef;
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
            this.bp = BarcodeProduct.getInstance(getIntent().getSerializableExtra("barcodeProduct"));
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
            fridgeListRef = db.collection("users").document(user.getUid()).collection("fridgeList");
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
                fridgeListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean itemNotExists = true;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.get("name").toString().toLowerCase().equals(item.toLowerCase())) {
                                    Toast.makeText(addToFridge.this, item+" is already in Fridge List", Toast.LENGTH_SHORT).show();
                                    itemNotExists=false;
                                    finish();
                                }
                            }
                            if (itemNotExists) {
                                bp.setQuantity(qty);
                                fridgeListRef.add(bp);
                                Log.d(TAG, "onSuccess: "+item+" added.");
                                Toast.makeText(addToFridge.this, item+" added to Fridge List", Toast.LENGTH_SHORT).show();
                                finish();

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

