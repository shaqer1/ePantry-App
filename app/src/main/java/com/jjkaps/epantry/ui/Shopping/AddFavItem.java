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
import com.jjkaps.epantry.ui.Shopping.FavItem;
import com.jjkaps.epantry.utils.Utils;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddFavItem extends AppCompatActivity {

    private static final String TAG = "AddFavItem";
    private TextView txtClose;
    private TextView txtNullFavList;
    private Button bt_cancel;
    private Button bt_done;
    private ListView listView_favItem;
    private FavItemAdapter favItemAdapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private CollectionReference catalogRef;
    private CollectionReference shopListRef;
    private FirebaseUser user;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fav_item);
        initView();

        txtClose = findViewById(R.id.txt_addFav_close);
        bt_cancel = findViewById(R.id.bt_addFav_cancel);
        bt_done = findViewById(R.id.bt_addFav_done);
        txtNullFavList = findViewById(R.id.txt_emptyFavList);

        listView_favItem = findViewById(R.id.listView_favList);

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

        favItemAdapter = new FavItemAdapter(getBaseContext(), new ArrayList<FavItem>());
        listView_favItem.setAdapter(favItemAdapter);
        favItemAdapter.notifyDataSetChanged();
        getFavItemList();


        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean itemAdded = false;
                for(int i=0; i<favItemAdapter.getCount(); i++) {
                    final FavItem item = favItemAdapter.getItem(i);
                    if (item != null && item.isChecked()) {
                        Map<String, Object> shoppingListMap = new HashMap<>();
                        shoppingListMap.put("name", item.getBarcodeProduct().getName());
                        shoppingListMap.put("quantity", item.getQuantity());
                        shoppingListMap.put("checked", false);
                        shopListRef.add(shoppingListMap);
                        itemAdded = true;
                    }
                }
                if (itemAdded) {
                    Utils.createToast(AddFavItem.this, "Items Added", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                } else {
                    Utils.createToast(AddFavItem.this, "No Item Added", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                }
                finish();
            }

        });



    }

    private void getFavItemList() {
        if (user != null) {
            catalogRef.whereEqualTo("favorite", true)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                if (task.getResult().isEmpty()) {
                                    txtNullFavList.setVisibility(View.VISIBLE);
                                } else {
                                    ArrayList<FavItem> FavItems = new ArrayList<>();
                                    for(QueryDocumentSnapshot document : task.getResult()) {
                                        BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                                        FavItems.add(new FavItem(bp, document.getReference().getPath(), 1, false));
                                    }
                                    favItemAdapter.addAll(FavItems);
                                    favItemAdapter.notifyDataSetChanged();
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