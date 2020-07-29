package com.jjkaps.epantry.ui.ItemUI;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ProductModels.DietFlag;
import com.jjkaps.epantry.models.ProductModels.DietInfo;
import com.jjkaps.epantry.models.ProductModels.DietLabel;
import com.jjkaps.epantry.ui.Fridge.AddFridgeItem;
import com.jjkaps.epantry.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemActivity extends AppCompatActivity {

    /*
    Image
    Name
    quantity
    expiration
    Brand <text view>
    Ingredients
    package details
    Serving details
    diet chips
    palm oil ingredients
    notes
    add storage type
    nutrition info (maybe image)//TODO nutrition info
    */
    private BarcodeProduct bp;
    private String currentCollection;
    private ImageView imageIV;
    private TextView nameTV, quantityTV, expirationTV, brandTV, ingredientsTV, pkgSizeTV, pkgQtyTV, srvSizeTV, srvUnitTV, palmOilIngredTV;
    private EditText notesET;
    private Button updateItemBT, addShoppingListBT, updateCatalog, editImageBT;
    private Chip veganChip, vegChip, glutenChip;
    private SimpleDateFormat simpleDateFormat;
    private String docRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference shopListRef, fridgeListRef;
    private FirebaseUser user;
    //private Boolean catalogExists = false;
    private DocumentReference catalogRef;
    private CollectionReference catalogListRef;
    private AutoCompleteTextView storgaeDropdown;
    private final String[] storageOptions = new String[] {"Fridge", "Freezer", "Pantry"};
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private LinearLayout storageLL;
    private TextView progText;

    private final int PICK_IMAGE_REQUEST = 71;

    private Uri filePath;
    private boolean addedImage = false;
    private String itemId;
    private StorageReference storageReference;
    private RelativeLayout imageRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        //get catalog
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user != null) {
            catalogListRef = db.collection("users").document(user.getUid()).collection("catalogList");
            fridgeListRef = db.collection("users").document(user.getUid()).collection("fridgeList");
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
        }
        //get bp object
        this.bp = BarcodeProduct.getInstance(getIntent().getSerializableExtra("barcodeProduct"));
        //get doc ref
        this.docRef = getIntent().getStringExtra("docID");

        this.currentCollection = getIntent().getStringExtra("currCollection");
        if (currentCollection != null) {
            Log.d("CURRENT COLLECTION "  ,currentCollection);
        }
        if(docRef != null){
            //Firebase
            db = FirebaseFirestore.getInstance();
        }

        initView();
        if(bp != null){
            //if from catalog tab use doc ref as catalog ref,
            // else if fridge item catalogRef not null use bp catalog ref
            //else it is null (not in catalog)
            catalogRef = (currentCollection.equals("catalogList"))?db.document(docRef):
                            (Utils.isNotNullOrEmpty(bp.getCatalogReference()))?db.document(bp.getCatalogReference()):null;
            //if from catalog tab use set text to remove from catalog,
            // or if fridge item catalogRef not null remove catalog as well
            //else it is null (not in catalog)
            updateCatalog.setText(currentCollection.equals("catalogList") || Utils.isNotNullOrEmpty(bp.getCatalogReference()) ?
                                    "Remove from Catalog":"Readd to Catalog");
            initText();
            /*catalogListRef.whereEqualTo("name", bp.getName())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && task.getResult().size() > 0) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (String.valueOf(document.get("name")).equalsIgnoreCase(bp.getName())) {
                                            catalogRef = document.getReference();
                                            updateCatalog.setText("Remove from Catalog");
                                            catalogExists = true;
                                        } else {
                                            updateCatalog.setText("Readd to Catalog");
                                            catalogExists = false;
                                        }
                                    }
                                }
                            }
                        }
                    });*/
        }

        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        //set action bar name
        if(this.getSupportActionBar() != null){
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setDisplayShowCustomEnabled(true);
            //getSupportActionBar().setIcon(new ColorDrawable(getColor(R.color.colorWhite)));
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);

            //View view = getSupportActionBar().getCustomView();
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
            name.setText(bp != null ? bp.getName().substring(0, Math.min(bp.getName().length(), 15)) : "Item Info");
        }

        // update the exp date
        expirationTV = findViewById(R.id.item_exp);
        expirationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(expirationTV);
            }
        });

        // update the image
        editImageBT = findViewById(R.id.editImageBT);
        editImageBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        // update item info button
        updateItemBT = findViewById(R.id.bt_updateItem);
        if(currentCollection.equals("catalogList")) {
            updateItemBT.setText("FRIDGE LIST");
            updateItemBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //fridgeListRef.add(bp);
                    Intent i = new Intent (getApplicationContext(), addToFridge.class);
                    i.putExtra("itemName", bp.getName());
                    i.putExtra("barcodeProduct", bp);
                    i.putExtra("docRef", docRef);
                    startActivityForResult(i, 2);

                }
            });
        } else {
            updateItemBT.setText("UPDATE ITEM");
            updateItemBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // update the item in database
                    if(db != null && docRef != null && bp != null){
                        boolean changed = false;
                        // if notes changed
                        if(Utils.isNotNullOrEmpty(notesET.getText().toString().trim())){
                            bp.setNotes(notesET.getText().toString().trim());
                            changed = true;
                        }

                        // if storage location changed
                        if (Utils.isNotNullOrEmpty(storgaeDropdown.getText().toString().trim())){
                            bp.setStorageType(storgaeDropdown.getText().toString().trim());
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

                        // update vegan/veg/gluten
                        if(Utils.isNotNullOrEmpty(bp.getDietInfo())){
                            // change gluten free
                            if (bp.getDietInfo().getGluten_free().isIs_compatible() != glutenChip.isChecked()) { // old != new
                                DietInfo di = new DietInfo(new DietLabel("Vegan", bp.getDietInfo().getVegan().isIs_compatible(),
                                                bp.getDietInfo().getVegan().getCompatibility_level(), bp.getDietInfo().getVegan().getConfidence(),
                                                bp.getDietInfo().getVegan().getConfidence_description()),
                                        new DietLabel("Vegetarian", bp.getDietInfo().getVeg().isIs_compatible(),
                                                bp.getDietInfo().getVeg().getCompatibility_level(), bp.getDietInfo().getVeg().getConfidence(),
                                                bp.getDietInfo().getVeg().getConfidence_description()),
                                        new DietLabel("Gluten Free", glutenChip.isChecked(), 2, true, "verified by user"),
                                        bp.getDietInfo().getDietFlags());
                                bp.setDietInfo(di);
                                changed = true;
                            }
                            // change vegetarian
                            if (bp.getDietInfo().getVeg().isIs_compatible() != vegChip.isChecked()) { // old != new
                                DietInfo di = new DietInfo(new DietLabel("Vegan", bp.getDietInfo().getVegan().isIs_compatible(),
                                                bp.getDietInfo().getVegan().getCompatibility_level(), bp.getDietInfo().getVegan().getConfidence(),
                                                bp.getDietInfo().getVegan().getConfidence_description()),
                                        new DietLabel("Vegetarian", vegChip.isChecked(), 2, true, "verified by user"),
                                        new DietLabel("Gluten Free", bp.getDietInfo().getGluten_free().isIs_compatible(),
                                                bp.getDietInfo().getGluten_free().getCompatibility_level(), bp.getDietInfo().getGluten_free().getConfidence(),
                                                bp.getDietInfo().getGluten_free().getConfidence_description()),
                                        bp.getDietInfo().getDietFlags());
                                bp.setDietInfo(di);
                                changed = true;
                            }
                            // change vegan
                            if (bp.getDietInfo().getVegan().isIs_compatible() != veganChip.isChecked()) { // old != new
                                DietInfo di = new DietInfo(new DietLabel("Vegan", veganChip.isChecked(), 2, true, "verified by user"),
                                        new DietLabel("Vegetarian", bp.getDietInfo().getVeg().isIs_compatible(),
                                                bp.getDietInfo().getVeg().getCompatibility_level(), bp.getDietInfo().getVeg().getConfidence(),
                                                bp.getDietInfo().getVeg().getConfidence_description()),
                                        new DietLabel("Gluten Free", bp.getDietInfo().getGluten_free().isIs_compatible(),
                                                bp.getDietInfo().getGluten_free().getCompatibility_level(), bp.getDietInfo().getGluten_free().getConfidence(),
                                                bp.getDietInfo().getGluten_free().getConfidence_description()),
                                        bp.getDietInfo().getDietFlags());
                                bp.setDietInfo(di);
                                changed = true;
                            }
                        }

                        // exp date changed
                        if (Utils.isNotNullOrEmpty(expirationTV.getText().toString().trim())) {
                            // does not check if date entered has passed b/c people still keep food past exp
                            if (!bp.getExpDate().equals(expirationTV.getText().toString().trim())) {
                                bp.setExpDate(expirationTV.getText().toString().trim());
                                changed = true;
                            }
                        }

                        // todo change photo - get code from add manual item
                        if (addedImage) {
                            //uploadImage(itemId, String.valueOf(catalogDocument.get("name")).toLowerCase());

                            uploadImage(docRef.substring(docRef.lastIndexOf('/') + 1), docRef.substring(docRef.lastIndexOf('/') + 1));
                        }

                        if(changed){
                            db.document(docRef).set(bp); // update fields
                            // todo -> update catalog
                        }
                    }
                }
            });
        }

        // todo - make all editable components appear

        // add item to shopping list button
        addShoppingListBT = findViewById(R.id.bt_addShoppingList);
        addShoppingListBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            boolean itemNotExistsInCatalog = true;
                            for (QueryDocumentSnapshot catalogDocument : task.getResult()) {
                                if (String.valueOf(catalogDocument.get("name")).toLowerCase().equals(bp.getName().toLowerCase())) {
                                    Toast.makeText(ItemActivity.this, bp.getName() + " is already in Shopping List", Toast.LENGTH_SHORT).show();
                                    itemNotExistsInCatalog = false;
                                    break;
                                }
                            }
                            if (itemNotExistsInCatalog) {
                                Intent i = new Intent(getApplicationContext(), AddFridgeToShopping.class);
                                i.putExtra("itemName", bp.getName());
                                startActivityForResult(i, 2);
                            }
                        }
                    }
                });
            }
        });

        updateCatalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (catalogRef == null) { //item does not exist in catalog, so add it
                    final DocumentReference dr = Utils.isNotNullOrEmpty(bp.getBarcode())?catalogListRef.document(bp.getBarcode()):catalogListRef.document();
                    dr.set(BarcodeProduct.getCatalogObj(bp)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.document(docRef).update("catalogReference", dr.getPath());
                        }
                    });
                    Toast toast = Toast.makeText(ItemActivity.this, bp.getName()+" readd to Catalog", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                } else { //item does exist in catalog, so delete it
                    //remove catalog list reference if this is item from fridge
                    if(currentCollection.equals("fridgeList")){
                        db.document(docRef).update("catalogReference", "");
                    }
                    catalogRef.delete();
                    Toast toast = Toast.makeText(ItemActivity.this, bp.getName()+" removed from Catalog", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
                finish();
            }
        });
    }

    private void showDateDialog(final TextView date) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                date.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };
        new DatePickerDialog(this, dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageIV.setImageBitmap(bitmap);
                addedImage = true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private void uploadImage(final String fridgeItemID, final String catalogItemID) {
        if(filePath != null){
            imageRL.setVisibility(View.VISIBLE);
            StorageReference ref = storageReference.child("images/"+ user.getUid()+itemId);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRL.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Image Uploaded Successfully "+fridgeItemID, Toast.LENGTH_LONG).show();
                            fridgeListRef.document(fridgeItemID).update("userImage","images/"+ user.getUid() + itemId);
                            catalogListRef.document(catalogItemID).update("userImage","images/"+ user.getUid() + itemId);
                            initText();
                            addedImage = false;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    imageRL.setVisibility(View.GONE);
                    addedImage = false;
                    imageIV.setImageResource(R.drawable.image_not_found);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                            .getTotalByteCount());
                    progText.setText("Uploaded "+(int)progress+"%");
                }
            });
        }
    }

    private void initView() {
        imageIV = findViewById(R.id.item_image);
        nameTV = findViewById(R.id.item_name);
        quantityTV = findViewById(R.id.item_quantity);
        brandTV = findViewById(R.id.item_brand);
        ingredientsTV = findViewById(R.id.item_ingredients);
        pkgSizeTV = findViewById(R.id.item_pkg_size);
        pkgQtyTV = findViewById(R.id.item_pkg_qty);
        srvSizeTV = findViewById(R.id.item_srv_sze);
        srvUnitTV = findViewById(R.id.item_srv_unit);
        notesET = findViewById(R.id.item_notes);
        veganChip = findViewById(R.id.vegan_chip);
        vegChip = findViewById(R.id.veg_chip);
        glutenChip = findViewById(R.id.gluten_chip);
        palmOilIngredTV = findViewById(R.id.palm_oil_ingr);
        expirationTV = findViewById(R.id.item_exp);
        storgaeDropdown = findViewById(R.id.filled_exposed_dropdown);
        updateCatalog = findViewById(R.id.bt_updateCatalog);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu, storageOptions);
        storgaeDropdown.setAdapter(adapter);
        storgaeDropdown.setInputType(InputType.TYPE_NULL);
        storageLL = findViewById(R.id.storage_ll);
        progText = findViewById(R.id.progress_bar_text);
        imageRL = findViewById(R.id.image_upload_RL);
        if(Utils.isNotNullOrEmpty(this.currentCollection) && this.currentCollection.equals("catalogList")){
            storageLL.setVisibility(View.GONE);
        }
        itemId = this.bp.getName().toLowerCase();
        storageReference = storage.getReference();
    }

    private void initText() {
        /*set photo*/
        if(Utils.isNotNullOrEmpty(bp.getUserImage())){
            //load image
            StorageReference imageStorage = storage.getReference("images/"+ user.getUid()+bp.getName().toLowerCase());
            final long OM = 5000 * 500000000L;
            imageStorage.getBytes(OM).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageIV.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }else if(Utils.isNotNullOrEmpty(bp.getFrontPhoto()) && Utils.isNotNullOrEmpty(bp.getFrontPhoto().getDisplay())){
            Picasso.get().load(bp.getFrontPhoto().getDisplay()).into(imageIV);
        }
        /*set name*/
        if(Utils.isNotNullOrEmpty(bp.getName())){
            nameTV.setText(bp.getName());
        }
        /*set quantity*/
        if(Utils.isNotNullOrEmpty(bp.getQuantity()) && bp.getQuantity() != 0){
            quantityTV.setText(String.valueOf(bp.getQuantity()));
        }else {
            findViewById(R.id.quantity_til).setVisibility(View.GONE);
        }
        /*expiration*/
        if(Utils.isNotNullOrEmpty(bp.getExpDate()) ){
            expirationTV.setText(String.valueOf(bp.getExpDate()));
        }else {
            findViewById(R.id.exp_til).setVisibility(View.GONE);
        }
        /*brand*/
        if(Utils.isNotNullOrEmpty(bp.getBrand())){
            brandTV.setText(bp.getBrand());
        }else{
            findViewById(R.id.brand_til).setVisibility(View.GONE);
        }
        /*ingredients*/
        if(Utils.isNotNullOrEmpty(bp.getIngredients())){
            ingredientsTV.setText(bp.getIngredients());
        }else{
            findViewById(R.id.ingredients_til).setVisibility(View.GONE);
        }
        /*notes*/
        if(Utils.isNotNullOrEmpty(bp.getNotes())){
            notesET.setText(bp.getNotes());
        }
        notesET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    if(db != null && docRef != null && Utils.isNotNullOrEmpty(notesET.getText().toString().trim())){
                        db.document(docRef).update("notes", notesET.getText().toString());
                    }
                }
                return false;
            }
        });
        /*gluten chip*/
        if(Utils.isNotNullOrEmpty(bp.getDietInfo().getGluten_free())){
            if(Utils.isNotNullOrEmpty(bp.getDietInfo().getGluten_free().isIs_compatible())){
                glutenChip.setChecked(bp.getDietInfo().getGluten_free().isIs_compatible());
            }
        }else{
            findViewById(R.id.dietView).setVisibility(View.GONE);
        }
        /*vegetarian chip*/
        if(Utils.isNotNullOrEmpty(bp.getDietInfo().getVeg())){
            if(Utils.isNotNullOrEmpty(bp.getDietInfo().getVeg().isIs_compatible())){
                vegChip.setChecked(bp.getDietInfo().getVeg().isIs_compatible());
            }
        }else{
            findViewById(R.id.dietView).setVisibility(View.GONE);
        }
        /*vegan chip*/
        if(Utils.isNotNullOrEmpty(bp.getDietInfo().getVegan())){
            if(Utils.isNotNullOrEmpty(bp.getDietInfo().getVegan().isIs_compatible())){
                veganChip.setChecked(bp.getDietInfo().getVegan().isIs_compatible());
            }
        }else{
            findViewById(R.id.dietView).setVisibility(View.GONE);
        }
        /*pkg*/
        if(Utils.isNotNullOrEmpty(bp.getPackageDetails()) && Utils.isNotNullOrEmpty(bp.getPackageDetails().getQuantity()) && Utils.isNotNullOrEmpty(bp.getPackageDetails().getSize())){
            pkgQtyTV.setText(String.valueOf(bp.getPackageDetails().getQuantity()));
            pkgSizeTV.setText(bp.getPackageDetails().getSize());
        }else{
            findViewById(R.id.package_til).setVisibility(View.GONE);
        }
        /*Serving*/
        if(Utils.isNotNullOrEmpty(bp.getServing()) && Utils.isNotNullOrEmpty(bp.getServing().getSize()) && Utils.isNotNullOrEmpty(bp.getServing().getMeasurement_unit())){
            srvSizeTV.setText(bp.getServing().getSize());
            srvUnitTV.setText(bp.getServing().getMeasurement_unit());
        }else if (Utils.isNotNullOrEmpty(bp.getServing()) && Utils.isNotNullOrEmpty(bp.getServing().getSize_fulltext())){
            srvSizeTV.setText(bp.getServing().getSize_fulltext());
            findViewById(R.id.item_srv_unit).setVisibility(View.GONE);
        }else{
            findViewById(R.id.srv_til).setVisibility(View.GONE);
        }
        /*palm oil chip*/
        if(Utils.isNotNullOrEmpty(bp.getPalm_oil_ingredients()) && bp.getPalm_oil_ingredients().size() > 0){
            palmOilIngredTV.setText(getStringArr(bp.getPalm_oil_ingredients()));
        }else{
            findViewById(R.id.palm_oil_ingr_til).setVisibility(View.GONE);
        }
        /*storage dropdown*/
        if(Utils.isNotNullOrEmpty(bp.getStorageType())){
            storgaeDropdown.setText(bp.getStorageType(), false);
        }
    }

    private String getStringArr(List<String> palm_oil_ingredients) {
        StringBuilder s = new StringBuilder();
        for(String x : palm_oil_ingredients){
            s.append(x).append(", ");
        }
        return s.toString().substring(0,s.length()-2);
    }
}