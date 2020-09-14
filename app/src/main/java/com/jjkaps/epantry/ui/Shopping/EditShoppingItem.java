package com.jjkaps.epantry.ui.Shopping;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.utils.Utils;

public class EditShoppingItem extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    private CollectionReference shopListRef;

    private String docRef;

    private EditText quantityET, notesET;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shopping_item);
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user != null) {
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
        }

        docRef = getIntent().getStringExtra("docID");
        String shoppingItemName = getIntent().getStringExtra("name");
        String shoppingItemQuantity = getIntent().getStringExtra("quantity");
        String shoppingItemNotes = getIntent().getStringExtra("notes");

        //init
        TextView nameTV = findViewById(R.id.item_name);
        nameTV.setText(Utils.toSentCase(shoppingItemName));
        quantityET = findViewById(R.id.item_quantity);
        quantityET.setText(shoppingItemQuantity);
        //quantityET.setText("2");
        notesET = findViewById(R.id.item_notes);
        notesET.setText(shoppingItemNotes);

        //set action bar
        if (this.getSupportActionBar() != null){
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            TextView name = findViewById(R.id.name);
            Button backButton = findViewById(R.id.txt_close);
            backButton.setVisibility(View.VISIBLE);

            /* close */
            backButton.setOnClickListener(view -> finish());

            name.setText("Edit Item");
            ImageButton updateButton = findViewById(R.id.btn_update);
            updateButton.setImageResource(R.drawable.ic_update_check);
            updateButton.setVisibility(View.VISIBLE);

            /* Update item */
            updateButton.setOnClickListener(view -> {
                if (db != null && docRef != null && shopListRef != null) {
                    // notes changed
                    /*if(Utils.isNotNullOrEmpty(notesET.getText().toString().trim())){

                    }*/
                    db.collection("users").document(user.getUid()).collection("shoppingList")
                            .document(docRef).update("notes", notesET.getText().toString().trim());
                    // if quantity changed
                    String qty = quantityET.getText().toString().trim();
                    if(Utils.isNotNullOrEmpty(qty) && Integer.parseInt(qty)>=0 && Integer.parseInt(qty)<=99){
                        db.collection("users").document(user.getUid()).collection("shoppingList")
                                .document(docRef).update("quantity", Integer.parseInt(quantityET.getText().toString().trim()));
                    }else {
                        Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container), "Quantity must be between 0 and 99", Utils.StatusCodes.FAILURE);
                        return;
                    }
                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Item updated!", Utils.StatusCodes.SUCCESS);
                }
            });
        }
    }
}
