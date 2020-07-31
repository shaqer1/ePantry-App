package com.jjkaps.epantry.ui.Shopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.ui.Shopping.SuggItem;
import com.jjkaps.epantry.utils.Utils;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddSuggItem extends AppCompatActivity {

    private static final String TAG = "AddSuggItem";
    private TextView txtClose;
    private TextView txtNullSuggList;
    private Button bt_cancel;
    private Button bt_remove;
    private Button bt_add;
    private ListView listView_suggItem;
    private SuggItemAdapter suggItemAdapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private CollectionReference catalogRef;
    private CollectionReference shopListRef;
    private FirebaseUser user;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sugg_item);
        initView();

        txtClose = findViewById(R.id.txt_addSugg_close);
        bt_cancel = findViewById(R.id.bt_addSugg_cancel);
        bt_add = findViewById(R.id.bt_addSugg_add);
        bt_remove = findViewById(R.id.bt_addSugg_remove);
        txtNullSuggList = findViewById(R.id.txt_emptySuggList);
        listView_suggItem = findViewById(R.id.listView_suggList);

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user!=null) {
            catalogRef = db.collection("users").document(user.getUid()).collection("catalogList");
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
        }

        suggItemAdapter = new SuggItemAdapter(getBaseContext(), new ArrayList<SuggItem>());
        listView_suggItem.setAdapter(suggItemAdapter);
        suggItemAdapter.notifyDataSetChanged();
        getSuggItemList();


        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean itemAdded = false;
                for(int i=0; i<suggItemAdapter.getCount(); i++) {
                    final SuggItem item = suggItemAdapter.getItem(i);
                    if (item != null && item.isChecked()) {
                        Map<String, Object> shoppingListMap = new HashMap<>();
                        shoppingListMap.put("name", item.getBarcodeProduct().getName());
                        shoppingListMap.put("quantity", item.getQuantity());
                        shoppingListMap.put("checked", false);
                        shopListRef.add(shoppingListMap);
                        //remove suggested
                        suggItemAdapter.remove(item);
                        suggItemAdapter.notifyDataSetChanged();
                        db.document(item.getDocReference()).update("suggested", false);
                        itemAdded = true;
                    }
                }
                if (itemAdded) {
                    Utils.createToast(AddSuggItem.this, "Items Added", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                } else {
                    Utils.createToast(AddSuggItem.this, "No Item Added", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                }
                finish();
            }

        });
        bt_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean itemRemoved = false;
                for(int i=0; i<suggItemAdapter.getCount(); i++) {
                    final SuggItem item = suggItemAdapter.getItem(i);
//                    if (inputQty == null || inputQty.getText().toString().isEmpty()) {
//                        item.setQuantity(1);
//                    } else {
//                        item.setQuantity(Integer.parseInt(inputQty.getText().toString()));
//                    } always null...
                    if (item != null && item.isChecked()) {
                        //remove suggested
                        suggItemAdapter.remove(item);
                        suggItemAdapter.notifyDataSetChanged();
                        db.document(item.getDocReference()).update("suggested", false);
                        itemRemoved = true;
                    }
                }
                if (itemRemoved) {
                    Utils.createToast(AddSuggItem.this, "Items Removed From Suggested", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                } else {
                    Utils.createToast(AddSuggItem.this, "No Items Removed", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                }

            }

        });


    }

    private void getSuggItemList() {
        Log.d("sugg", "suggested populated");
        suggItemAdapter.clear();
        if (user != null) {
            catalogRef.whereEqualTo("suggested", true)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                if (task.getResult().isEmpty()) {
                                    txtNullSuggList.setVisibility(View.VISIBLE);
                                } else {
                                    ArrayList<SuggItem> SuggItems = new ArrayList<>();
                                    for(QueryDocumentSnapshot document : task.getResult()) {
                                        BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                                        SuggItems.add(new SuggItem(bp, document.getReference().getPath(), 1, false));
                                    }
                                    suggItemAdapter.addAll(SuggItems);
                                    suggItemAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
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
}