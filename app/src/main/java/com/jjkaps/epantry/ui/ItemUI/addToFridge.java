package com.jjkaps.epantry.ui.ItemUI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.utils.Utils;

public class addToFridge extends AppCompatActivity {

    private BarcodeProduct bp;
    private static final String TAG = "AddItem";
    private EditText inputItem;
    private EditText inputQtyItem;
    private String itemName;

    private AutoCompleteTextView storageDropdown;
    private final String[] storageOptions = new String[] {"Fridge", "Freezer", "Pantry"};

    private CollectionReference fridgeListRef;
    private String docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_catalog_to_fridge);

        TextView txtClose = findViewById(R.id.txt_close);
        Button btDone = findViewById(R.id.bt_done);
        inputQtyItem = findViewById(R.id.inputQuantityItem);
        inputItem = findViewById(R.id.inputItem);
        //add storage type
        storageDropdown = findViewById(R.id.filled_exposed_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu, storageOptions);
        storageDropdown.setAdapter(adapter);
        storageDropdown.setInputType(InputType.TYPE_NULL);
        Bundle nameB = getIntent().getExtras();
        if (nameB != null) {
            itemName = nameB.getString("itemName");
            this.bp = BarcodeProduct.getInstance(getIntent().getSerializableExtra("barcodeProduct"));
            this.docRef = getIntent().getStringExtra("docRef");
        }
        initView();
        initText();
        txtClose.setOnClickListener(v -> finish());
        //Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fridgeListRef = Utils.getFridgeListRef(user);
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
            if (storageDropdown.getText().toString().isEmpty()){
                storageDropdown.setError("Can't leave blank!");
                return;
            }
            final int qty = Integer.parseInt(inputQtyItem.getText().toString());
            //Check if item exists (with case check), if not add the item.
            /*fridgeListRef.whereEqualTo("name", itemName.toLowerCase()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().size()==0) {*/
                        bp.setQuantity(qty);
                        bp.setCatalogReference(docRef);
                        bp.setStorageType(storageDropdown.getText().toString().trim());
                        DocumentReference dr = Utils.isNotNullOrEmpty(bp.getBarcode())?fridgeListRef.document(bp.getBarcode()):fridgeListRef.document();
                        dr.set(BarcodeProduct.getFridgeObj(bp));

                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), itemName+" added to Fridge List", Utils.StatusCodes.SUCCESS);
                        Intent i = new Intent();
                        i.putExtra("HIDE_NAV", true);
                        setResult(2, i);// this lets activity know to hide the null bar
                    /*}else {
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), itemName+" is already in Fridge.", Utils.StatusCodes.FAILURE);
                    }*/
                    finish();
                /*}
            });*/
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

