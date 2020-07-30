package com.jjkaps.epantry.ui.Shopping;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.ProductModels.DietInfo;
import com.jjkaps.epantry.models.ProductModels.DietLabel;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class EditShoppingItem extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    private CollectionReference shopListRef;

    private String docRef;
    private String name;
    private int quantity;
    private String notes;

    private TextView nameTV;
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
        if(docRef != null){
            //Firebase
            db = FirebaseFirestore.getInstance();
        }

        //init
        nameTV = findViewById(R.id.item_name);
        quantityET = findViewById(R.id.item_quantity);
        notesET = findViewById(R.id.item_notes);

        //set action bar name

        if (this.getSupportActionBar() != null){
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            TextView name = findViewById(R.id.name);
            Button backButton = findViewById(R.id.txt_close);
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            /*
            name.setText(db != null ? db.getName().substring(0, Math.min(bp.getName().length(), 15)) : "Item Info");
            ImageButton updateButton = findViewById(R.id.btn_update);
            updateButton.setImageResource(R.drawable.ic_update_check);
            updateButton.setVisibility(View.VISIBLE);
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // update the item in database
                    if(db != null && docRef != null && bp != null){
                        boolean changed = false;
                        // if notes changed
                        if(Utils.isNotNullOrEmpty(notesET.getText().toString().trim())){
                            bp.setNotes(notesET.getText().toString().trim());
                            changed = true;
                        }

                        // if quantity changed
                        if (Utils.isNotNullOrEmpty(quantityTV.getText().toString().trim())) {
                            // verify new quantity is valid
                            String quantity = quantityTV.getText().toString().trim();
                            Pattern containsNum = Pattern.compile("^[0-9]+$");
                            Matcher isNum = containsNum.matcher(quantity);
                            if (!((quantity.equals("")) || !isNum.find() || (Integer.parseInt(quantity) <= 0) || (Integer.parseInt(quantity) > 99))) { // if it is valid, mark as changed
                                bp.setQuantity(Integer.parseInt(quantity));
                                changed = true;
                            }
                        }
                    }
                }
            }); */
        }
    }
}
