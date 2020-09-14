package com.jjkaps.epantry.ui.ItemUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.utils.Utils;

public class AddFridgeToShopping extends AppCompatActivity {

    private static final String TAG = "AddItem";
    private EditText inputItem;
    private EditText inputQtyItem;

    private String itemName;
    private String docRef;

    private CollectionReference shopListRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fridge_to_shopping);

        TextView txtClose = findViewById(R.id.txt_close);
        Button btDone = findViewById(R.id.bt_done);
        inputQtyItem = findViewById(R.id.inputQuantityItem);
        inputItem = findViewById(R.id.inputItem);
        Bundle nameB = getIntent().getExtras();
        if (nameB != null) {
            itemName = nameB.getString("itemName");
            docRef = nameB.getString("docRef");
        }
        initView();
        initText();
        txtClose.setOnClickListener(v -> finish());
        //Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            shopListRef = Utils.getShoppingListRef(user);
        }

        btDone.setOnClickListener(view -> {
            //get item
            final String itemName = inputItem.getText().toString();
            if (itemName.isEmpty()){
                inputItem.setError("Can't leave name blank!");
                return;
            }
            if (inputQtyItem.getText().toString().isEmpty()){
                inputQtyItem.setError("Can't leave blank!");
                return;
            }
            final int qty = Integer.parseInt(inputQtyItem.getText().toString());

            //Check if item exists (with case check), if not add the item.
            shopListRef.whereEqualTo("name", itemName.toLowerCase()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().size()==0) {
                        ShoppingListItem sli = new ShoppingListItem(itemName, qty, false, "", docRef);
                        shopListRef.add(sli)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "onSuccess: "+itemName+" added.");
                                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), itemName+" added to Shopping List", Utils.StatusCodes.SUCCESS);
                                    finish();
                                }).addOnFailureListener(e -> {
                                    Log.d(TAG, "onFailure: ",e);
                                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Network error occurred", Utils.StatusCodes.FAILURE);
                                });
                        Intent i = new Intent();
                        i.putExtra("HIDE_NAV", true);
                        setResult(2, i);// this lets activity know to hide the null bar
                    }else{
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), itemName+" is already in Shopping List", Utils.StatusCodes.SUCCESS);
                    }
                }
            });
        });

    }

    private void initView() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int) dm.widthPixels;
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

