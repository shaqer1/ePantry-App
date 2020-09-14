package com.jjkaps.epantry.ui.Settings;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;

import java.util.ArrayList;


public class SettingFavList extends AppCompatActivity {
    private TextView txtNullFavList;
    private ListView listView_favItem;
    private SettingFavItemAdapter favItemAdapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private CollectionReference catalogRef;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_fav_list);
        initView();

        TextView txtClose = findViewById(R.id.bt_setting_favList_close);
        txtNullFavList = findViewById(R.id.txt_setting_emptyFavList);
        listView_favItem = findViewById(R.id.listView_setting_favList);

        txtClose.setOnClickListener(v -> finish());

        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user!=null) {
            catalogRef = db.collection("users").document(user.getUid()).collection("catalogList");
        }

        favItemAdapter = new SettingFavItemAdapter(getBaseContext(), new ArrayList<>(), listView_favItem);
        listView_favItem.setAdapter(favItemAdapter);
        favItemAdapter.notifyDataSetChanged();
        getFavItemList();
    }



    private void getFavItemList() {
        if (user != null) {
            catalogRef.whereEqualTo("favorite", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (task.getResult().isEmpty()) {
                                txtNullFavList.setVisibility(View.VISIBLE);
                            } else {
                                ArrayList<SettingFavItem> settingFavItems = new ArrayList<>();
                                for(QueryDocumentSnapshot document : task.getResult()) {
                                    BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                                    settingFavItems.add(new SettingFavItem(bp, document.getReference().getPath(), true));
                                }
                                favItemAdapter.addAll(settingFavItems);
                                favItemAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }


    private void initView() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        /* *0.8*/
        int width = dm.widthPixels;
        int height = (int) (dm.heightPixels - (dm.heightPixels*0.15));
        getWindow().setLayout(width, height);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
    }
}