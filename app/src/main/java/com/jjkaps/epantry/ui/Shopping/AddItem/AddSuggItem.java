package com.jjkaps.epantry.ui.Shopping.AddItem;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.jjkaps.epantry.utils.Utils;

import java.util.ArrayList;

public class AddSuggItem extends Activity {

    private static final String TAG = "AddSuggItem";
    private TextView txtNullSuggList;
    private Button bt_remove;
    private ImageButton bt_add;
    private ListView listView_suggItem;
    private SuggItemAdapter suggItemAdapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //private CollectionReference catalogRef;
    private CollectionReference shopListRef;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CollectionReference fridgeListRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sugg_item);
        initView();

        TextView txtClose = findViewById(R.id.txt_addSugg_close);
        bt_add = findViewById(R.id.bt_addSugg_add);
        bt_remove = findViewById(R.id.bt_addSugg_remove);//TODO swipe action
        txtNullSuggList = findViewById(R.id.txt_emptySuggList);
        listView_suggItem = findViewById(R.id.listView_suggList);

        txtClose.setOnClickListener(v -> finish());

        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user!=null) {
            fridgeListRef = Utils.getFridgeListRef(user);
            shopListRef = Utils.getShoppingListRef(user);
        }

        suggItemAdapter = new SuggItemAdapter(getBaseContext(), new ArrayList<>());
        listView_suggItem.setAdapter(suggItemAdapter);
        suggItemAdapter.notifyDataSetChanged();
        getSuggItemList();


        bt_add.setOnClickListener(view -> {
            boolean itemAdded = false;
            for(int i=0; i<suggItemAdapter.getCount(); i++) {
                final SuggItem item = suggItemAdapter.getItem(i);
                if (item != null && item.isChecked()) {
                    ShoppingListItem sli = new ShoppingListItem(item.getBarcodeProduct().getName(), item.getQuantity(), false, "", item.getDocReference());
                    shopListRef.add(sli);
                    //remove suggested
                    suggItemAdapter.remove(item);
                    suggItemAdapter.notifyDataSetChanged();
                    db.document(item.getDocReference()).update("suggested", false);
                    itemAdded = true;
                }
            }
            if (itemAdded) {
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container),"Items Added!", Utils.StatusCodes.SUCCESS);
            } else {
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Could not add items. Please try again.", Utils.StatusCodes.FAILURE);
            }
            finish();
        });
        bt_remove.setOnClickListener(view -> {
            boolean itemRemoved = false;
            for(int i=0; i<suggItemAdapter.getCount(); i++) {
                final SuggItem item = suggItemAdapter.getItem(i);
                if (item != null && item.isChecked()) {
                    //remove suggested
                    suggItemAdapter.remove(item);
                    suggItemAdapter.notifyDataSetChanged();
                    db.document(item.getDocReference()).update("suggested", false);
                    itemRemoved = true;
                }
            }
            if (itemRemoved) {
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container),"Items Removed From Suggested", Utils.StatusCodes.SUCCESS);
            } else {
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Could not remove items. Please try again.", Utils.StatusCodes.FAILURE);
            }
        });
    }

    private void getSuggItemList() {
        Log.d("sugg", "suggested populated");
        suggItemAdapter.clear();
        if (user != null) {
            fridgeListRef.whereEqualTo("suggested", true)
            .get().addOnCompleteListener(task -> {
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