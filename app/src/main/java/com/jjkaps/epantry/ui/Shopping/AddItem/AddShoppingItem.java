package com.jjkaps.epantry.ui.Shopping.AddItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.utils.Utils;


public class AddShoppingItem extends AppCompatActivity {

    private static final String TAG = "AddItem";
    private EditText inputItem;
    private EditText inputQtyItem;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference shopListRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        initView();

        TextView txtClose = findViewById(R.id.txt_close);
        Button btDone = findViewById(R.id.bt_done);
        inputQtyItem = findViewById(R.id.inputQuantityItem);
        inputItem = findViewById(R.id.inputItem);

        txtClose.setOnClickListener(v -> finish());

        //Firebase
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user != null) {
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
        }

        btDone.setOnClickListener(view -> {
            //get item
            final String name = inputItem.getText().toString();
            if (name.isEmpty()){
                inputItem.setError("Can't leave name blank!");
                return;
            }
            if (inputQtyItem.getText().toString().isEmpty()){
                inputQtyItem.setError("Can't leave blank!");
                return;
            }
            final int qty = Integer.parseInt(inputQtyItem.getText().toString());

            //Check if item exists (with case check), if not add the item.
            shopListRef.whereEqualTo("name", name.toLowerCase()).get().addOnCompleteListener(task -> {//TODO use reference
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().size()==0) {
                        ShoppingListItem sli = new ShoppingListItem(name, qty, false, "");
                        shopListRef.add(sli)
                                .addOnSuccessListener(documentReference -> {
                                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), name+" Added!", Utils.StatusCodes.SUCCESS);
                                    inputItem.getText().clear();
                                    inputQtyItem.getText().clear();
                                }).addOnFailureListener(e -> Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Couldn't add item please try again", Utils.StatusCodes.FAILURE));
                        Intent i = new Intent();
                        i.putExtra("HIDE_NAV", true);
                        setResult(2, i);// this lets activity know to hide the null bar
                    }else{
                        inputItem.setError("Item exists");
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), name+" Exists!", Utils.StatusCodes.FAILURE);
                    }
                }
            });
        });

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