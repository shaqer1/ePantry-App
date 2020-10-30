package com.jjkaps.epantry.ui.Shopping.AddItem;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.ui.Shopping.FavItem;
import com.jjkaps.epantry.ui.Shopping.FavItemAdapter;
import com.jjkaps.epantry.utils.Utils;

import java.util.ArrayList;

public class AddFavItem extends Activity {

    private static final String TAG = "AddFavItem";
    private TextView txtClose;
    private TextView txtNullFavList;
    private Button bt_done;
    private ListView listView_favItem;
    private FavItemAdapter favItemAdapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private CollectionReference shopListRef;
    private FirebaseUser user;
    private CollectionReference fridgeListRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fav_item);
        initView();

        txtClose = findViewById(R.id.txt_addFav_close);
        bt_done = findViewById(R.id.bt_addFav_done);
        txtNullFavList = findViewById(R.id.txt_emptyFavList);

        listView_favItem = findViewById(R.id.listView_favList);

        txtClose.setOnClickListener(v -> finish());

        user = mAuth.getCurrentUser();
        if (user!=null) {
            fridgeListRef = Utils.getFridgeListRef(user);
            shopListRef = Utils.getShoppingListRef(user);
        }

        favItemAdapter = new FavItemAdapter(getBaseContext(), new ArrayList<>());
        listView_favItem.setAdapter(favItemAdapter);
        favItemAdapter.notifyDataSetChanged();
        getFavItemList();


        bt_done.setOnClickListener(view -> {
            boolean itemAdded = false;
            for(int i=0; i<favItemAdapter.getCount(); i++) {
                final FavItem item = favItemAdapter.getItem(i);
                if (item != null && item.isChecked()) {
                    ShoppingListItem sli = new ShoppingListItem(item.getBarcodeProduct().getName(), item.getQuantity(), false, "", item.getDocReference());
                    shopListRef.add(sli);
                    itemAdded = true;
                }
            }
            if (itemAdded) {
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Items Added!", Utils.StatusCodes.SUCCESS);
            } else {
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Could not add items. Please try again", Utils.StatusCodes.FAILURE);
            }
            finish();
        });
    }

    private void getFavItemList() {
        if (user != null) {
            fridgeListRef.whereEqualTo("favorite", true)
            .get()
            .addOnCompleteListener(task -> {
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