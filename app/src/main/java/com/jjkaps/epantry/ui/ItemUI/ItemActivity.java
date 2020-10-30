package com.jjkaps.epantry.ui.ItemUI;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ProductModels.DietLabel;
import com.jjkaps.epantry.ui.ItemUI.NutrientUI.NutrientGridAdapter;
import com.jjkaps.epantry.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    nutrition info (maybe image)//DONE? TEST
    */
    private String docRef;
    private CollectionReference shopListRef, fridgeListRef;
    private FirebaseUser user;
    private FirebaseFirestore db;
    //private DocumentReference catalogRef;
    private String currentCollection;

    private BarcodeProduct bp;
    private String itemName;

    private SimpleDateFormat simpleDateFormat;
    private ImageView imageIV;
    private RelativeLayout imageRL;
    private TextView nameTV, quantityTV, expirationTV, brandTV, ingredientsTV, pkgSizeTV, pkgQtyTV, srvSizeTV, srvUnitTV, palmOilIngredTV;
    private EditText notesET;
    private Button addFridgeListBT;
    private Button addShoppingListBT, addRemoveCatalog, editImageBT;
    private Chip veganChip, vegChip, glutenChip;
    private AutoCompleteTextView storgaeDropdown;
    private final String[] storageOptions = new String[] {"Fridge", "Freezer", "Pantry"};
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private TextView progText;
    private ImageView nutImageIV;
    private Button nutImageEditBut;
    private GridView nutGridV;

    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private boolean addedImage = false;
    private StorageReference storageReference;
    //private ExpandableListView expListView;
    //private CustomItemExpViewAdapter expListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        //get catalog
        simpleDateFormat = Utils.getExpDateFormat();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageReference = storage.getReference();
        if (user != null) {
            //catalogListRef = Utils.getCatalogListRef(user);
            shopListRef = Utils.getShoppingListRef(user);
            fridgeListRef = Utils.getFridgeListRef(user);
        }
        //get bp object
        this.bp = BarcodeProduct.getInstance(getIntent().getSerializableExtra("barcodeProduct"));
        //get doc ref
        this.docRef = getIntent().getStringExtra("docID");

        this.currentCollection = getIntent().getStringExtra("currCollection");
        if (currentCollection != null) {
            Log.d("CURRENT COLLECTION "  ,currentCollection);
        }

        initView();
        if(bp != null){
            //if from catalog tab use doc ref as catalog ref,
            // else if fridge item catalogRef not null use bp catalog ref
            //else it is null (not in catalog)

            //if from catalog tab use set text to remove from catalog,
            // or if fridge item catalogRef not null remove catalog as well
            //else it is null (not in catalog)
            if(!bp.isInStock()){

                addRemoveCatalog.setVisibility(View.VISIBLE);
            }
            initText();
        }


        //set action bar name
        if (this.getSupportActionBar() != null){
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            TextView name = findViewById(R.id.name);
            Button backButton = findViewById(R.id.txt_close);
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(view -> finish());
            name.setText(bp != null ? Utils.toSentCase(bp.getName().substring(0, Math.min(bp.getName().length(), 15))) : "Item Info");
            ImageButton updateButton = findViewById(R.id.btn_update);
            updateButton.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_update_check));
            updateButton.setVisibility(View.VISIBLE);
            updateButton.setOnClickListener(view -> { // update the item in database
                if(db != null && docRef != null && bp != null){
                    boolean changed = false;
                    // if notes changed
                    if(Utils.isNotNullOrEmpty(notesET.getText().toString().trim()) && !notesET.getText().toString().trim().equals(bp.getNotes())){
                        bp.setNotes(notesET.getText().toString().trim());
                        changed = true;
                    }

                    // if storage location changed
                    if (Utils.isNotNullOrEmpty(storgaeDropdown.getText().toString().trim()) && !storgaeDropdown.getText().toString().trim().equals(bp.getNotes())){
                        bp.setStorageType(storgaeDropdown.getText().toString().trim());
                        changed = true;
                    }

                    // if quantity changed
                    if (Utils.isNotNullOrEmpty(quantityTV.getText().toString().trim()) && !quantityTV.getText().toString().trim().equals(bp.getNotes())) {
                        // verify new quantity is valid
                        String quantity = quantityTV.getText().toString().trim();
                        Pattern containsNum = Pattern.compile("^[0-9]+$");
                        Matcher isNum = containsNum.matcher(quantity);
                        if (!((quantity.equals("")) || !isNum.find() || (Integer.parseInt(quantity) <= 0) || (Integer.parseInt(quantity) > 99))) { // if it is valid, mark as changed
                            bp.getInventoryDetails().setQuantity(Integer.parseInt(quantity));//TODO
                            changed = true;
                        }
                    }

                    // update vegan/veg/gluten
                    if(Utils.isNotNullOrEmpty(bp.getDietInfo())){
                        // change gluten free
                        if (bp.getDietInfo().getGluten_free().isIs_compatible() != glutenChip.isChecked()) { // old != new
                            bp.getDietInfo().setGluten_free(new DietLabel("Gluten Free", glutenChip.isChecked(), 2, true, "verified by user"));
                            changed = true;
                        }
                        // change vegetarian
                        if (bp.getDietInfo().getVeg().isIs_compatible() != vegChip.isChecked()) { // old != new
                            bp.getDietInfo().setVeg(new DietLabel("Vegetarian", vegChip.isChecked(), 2, true, "verified by user"));
                            changed = true;
                        }
                        // change vegan
                        if (bp.getDietInfo().getVegan().isIs_compatible() != veganChip.isChecked()) { // old != new
                            bp.getDietInfo().setVegan(new DietLabel("Vegan", veganChip.isChecked(), 2, true, "verified by user"));
                            changed = true;
                        }
                    }

                    // exp date changed
                    if (Utils.isNotNullOrEmpty(expirationTV.getText().toString().trim())) {
                        // does not check if date entered has passed b/c people still keep food past exp
                        if (!simpleDateFormat.format(bp.getInventoryDetails().getExpDate()).equals(expirationTV.getText().toString().trim())) {
                            try {
                                bp.getInventoryDetails().setExpDate(simpleDateFormat.parse(expirationTV.getText().toString().trim()));
                                changed = true;
                            } catch (ParseException e) {
                                Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container), "Could not parse date", Utils.StatusCodes.FAILURE);
                            }
                        }
                    }

                    if (addedImage) {
                        uploadImage();
                    }

                    if(changed){
                        db.document(docRef).set(bp); // update fields
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Item updated!", Utils.StatusCodes.SUCCESS);
                    } else {
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Item is up to date.", Utils.StatusCodes.INFO);
                    }
                }
            });
        }

        // update the exp date
        expirationTV.setOnClickListener(view -> showDateDialog(expirationTV));

        // update the image
        editImageBT = findViewById(R.id.editImageBT);
        editImageBT.setOnClickListener(view -> chooseImage());

        // update item info button
        addFridgeListBT = findViewById(R.id.bt_addFridgeList);
        if(!currentCollection.equals("fridgeList")) {//DONE check doc ref instead of name in activity
            fridgeListRef.document(Utils.getDocId(docRef)).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    BarcodeProduct bp = task.getResult().toObject(BarcodeProduct.class);
                    if (bp == null || bp.isInStock()){
                        addFridgeListBT.setVisibility(View.GONE);
                        addShoppingListBT.setVisibility(View.VISIBLE);
                    }else {
                        addFridgeListBT.setOnClickListener(view -> {
                            Intent i = new Intent(getApplicationContext(), addToFridge.class);
                            i.putExtra("itemName", bp.getName());
                            i.putExtra("barcodeProduct", bp);
                            i.putExtra("docRef", docRef);
                            startActivityForResult(i, 2);
                        });

                    }
                } else {
                    addFridgeListBT.setVisibility(View.GONE);
                    addShoppingListBT.setVisibility(View.VISIBLE);
                }
            });
        } else {
            addFridgeListBT.setVisibility(View.GONE);
            expirationTV.setVisibility(View.VISIBLE);
        }

        // todo - make all editable components appear

        // add item to shopping list button
        addShoppingListBT = findViewById(R.id.bt_addShoppingList);
        checkIfItemInShopping();
        addShoppingListBT.setOnClickListener(view -> {//DONE? if in fridge don't let it add to shopping
            if(addShoppingListBT.isEnabled()){
                goToAddItemActivity(docRef);
            }
        });

        addRemoveCatalog.setOnClickListener(view -> {
            /*if (catalogRef == null) { //item does not exist in catalog, so add it
                final DocumentReference dr = Utils.isNotNullOrEmpty(bp.getBarcode())?catalogListRef.document(bp.getBarcode()):catalogListRef.document();
                dr.set(BarcodeProduct.getCatalogObj(bp)).addOnSuccessListener(aVoid -> {
                        db.document(docRef).update("catalogReference", dr.getPath());
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), bp.getName()+" added to Catalog", Utils.StatusCodes.SUCCESS);
                });
                catalogRef = dr;
                addRemoveCatalog.setText("Remove from Catalog");
            } else { //item does exist in catalog, so delete it*/
                //remove catalog list reference if this is item from fridge
            db.document(docRef).delete();
                /*if(currentCollection.equals("fridgeList")){
                    db.document(docRef).update("catalogReference", "");
                    catalogRef = null;
                    addRemoveCatalog.setText("Add to Catalog");
                }*/
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), bp.getName()+" removed from Account", Utils.StatusCodes.SUCCESS);
            /*}*/
            //if(currentCollection.equals("catalogList")){
                finish();
            //}
        });
    }

    private void checkIfItemInShopping() {
        shopListRef.whereEqualTo("docReference", docRef).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (task.getResult().size()==0){
                    addShoppingListBT.setEnabled(true);
                }else{
                    addShoppingListBT.setEnabled(false);
                    //Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), bp.getName()+ " is already in your Shopping List", Utils.StatusCodes.FAILURE);
                }
            }else{
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Network error occurred.", Utils.StatusCodes.FAILURE);
            }
        });
    }

    private void goToAddItemActivity(String docRef) {
        Intent i = new Intent(getApplicationContext(), AddFridgeToShopping.class);
        i.putExtra("itemName", bp.getName());
        i.putExtra("docRef", docRef);
        startActivityForResult(i, 2);
    }

    private void showDateDialog(final TextView date) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            date.setText(simpleDateFormat.format(calendar.getTime()));
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
                && data != null && data.getData() != null ) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageIV.setImageBitmap(bitmap);
                addedImage = true;
                uploadImage();
            } catch (IOException e){
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Could not parse image as bitmap.", Utils.StatusCodes.FAILURE);
            }
        }
    }
    private void uploadImage() {
        if(filePath != null){
            imageRL.setVisibility(View.VISIBLE);
            StorageReference ref = storageReference.child("images/"+ user.getUid()+ itemName);
            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Image Uploaded Successfully!", Utils.StatusCodes.SUCCESS);
                        imageRL.setVisibility(View.GONE);
                        bp.setUserImage("images/"+ user.getUid() + itemName);
                        bp.setUserImageDateModified(Calendar.getInstance().getTime());
                        db.document(docRef).update("userImage", bp.getUserImage());
                        db.document(docRef).update("userImageDateModified", bp.getUserImageDateModified());
                        /*switch (location) {//DONE? this is redundant, use docRef, the filename is always same, need to figure out how to trigger refresh list
                            case (FRIDGE):
                                fridgeListRef.document(docRef).update("userImage","");
                                fridgeListRef.document(docRef).update("userImage","images/"+ user.getUid() + itemName);
                                break;
                            case (CATALOG):
                                catalogListRef.document(docRef).update("userImage","");
                                catalogListRef.document(docRef).update("userImage","images/"+ user.getUid() + itemName);
                                break;
                        }*/
                        //initText();
                        addedImage = false;
                    }).addOnFailureListener(e -> {
                        imageRL.setVisibility(View.GONE);
                        addedImage = false;
                        Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container), "Could not upload image", Utils.StatusCodes.FAILURE);
                        imageIV.setImageResource(R.drawable.image_not_found);
                    }).addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progText.setText(("Uploaded "+(int)progress+"%"));
                    });
        }
    }

    private void initView() {
        /*expListView = findViewById(R.id.productIngredientsELV);
        List<String> expListTitles = new ArrayList<>();*/

        //image
        imageRL = findViewById(R.id.image_upload_RL);
        imageIV = findViewById(R.id.item_image);
        //name
        nameTV = findViewById(R.id.item_name);
        itemName = this.bp.getName().toLowerCase();
        //qty
        quantityTV = findViewById(R.id.item_quantity);
        //exp
        expirationTV = findViewById(R.id.item_exp);
        //brand
        brandTV = findViewById(R.id.item_brand);
        //ingred
        //expListTitles.add("Ingredients");
        ingredientsTV = findViewById(R.id.item_ingredients);
        palmOilIngredTV = findViewById(R.id.palm_oil_ingr);
        //pkg
        pkgSizeTV = findViewById(R.id.item_pkg_size);
        pkgQtyTV = findViewById(R.id.item_pkg_qty);
        //serving
        srvSizeTV = findViewById(R.id.item_srv_sze);
        srvUnitTV = findViewById(R.id.item_srv_unit);
        //notes
        notesET = findViewById(R.id.item_notes);
        //diet
        veganChip = findViewById(R.id.vegan_chip);
        vegChip = findViewById(R.id.veg_chip);
        glutenChip = findViewById(R.id.gluten_chip);
        //storage
        storgaeDropdown = findViewById(R.id.filled_exposed_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu, storageOptions);
        storgaeDropdown.setAdapter(adapter);
        storgaeDropdown.setInputType(InputType.TYPE_NULL);
        LinearLayout storageLL = findViewById(R.id.storage_ll);
        /*if(Utils.isNotNullOrEmpty(this.currentCollection) && this.currentCollection.equals("catalogList")){
            storageLL.setVisibility(View.GONE);
        }*/
        //buttons
        addRemoveCatalog = findViewById(R.id.bt_add_remove_catalog);
        //progress
        progText = findViewById(R.id.progress_bar_text);
        //nutrition
        nutImageIV = findViewById(R.id.item_nut_image);
        nutImageEditBut = findViewById(R.id.editImageNutBT);
        nutGridV = findViewById(R.id.grid_nut);


        /*expListViewAdapter = new CustomItemExpViewAdapter(this, expListTitles, new BPAdapterItem(bp, docRef));
        setGroupIndicatorToRight();
        expListView.setAdapter(expListViewAdapter);*/
    }

    /*private void setGroupIndicatorToRight() {
        *//* Get the screen width *//*
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        expListView.setIndicatorBounds(width - getDipsFromPixel(35), width - getDipsFromPixel(5));
    }
    // Convert pixel to dip
    public int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }*/

    private void initText() {
        /*set photo*/
        if(Utils.isNotNullOrEmpty(bp.getUserImage())){
            //load image
            StorageReference imageStorage = storage.getReference("images/"+ user.getUid()+bp.getName().toLowerCase());
            final long OM = 5000 * 500000000L;
            imageStorage.getBytes(OM).addOnSuccessListener(bytes -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageIV.setImageBitmap(bitmap);
            }).addOnFailureListener(e ->
                    Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container), "Could not load image", Utils.StatusCodes.FAILURE)
            );
        }else if(Utils.isNotNullOrEmpty(bp.getFrontPhoto()) && Utils.isNotNullOrEmpty(bp.getFrontPhoto().getDisplay())){
            Picasso.get().load(bp.getFrontPhoto().getDisplay()).into(imageIV);
        }else {
            imageIV.setImageResource(R.drawable.image_not_found);
        }
        /*set name*/
        if(Utils.isNotNullOrEmpty(bp.getName())){
            nameTV.setText(Utils.toSentCase(bp.getName()));
        }
        /*set quantity*/
        if(Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && bp.getInventoryDetails().getQuantity() != 0){
            quantityTV.setText(String.valueOf(bp.getInventoryDetails().getQuantity()));
        }else {
            findViewById(R.id.quantity_til).setVisibility(View.GONE);
        }
        /*expiration*/
        if(Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && Utils.isNotNullOrEmpty(bp.getInventoryDetails().getExpDate()) ){
            expirationTV.setText(simpleDateFormat.format(bp.getInventoryDetails().getExpDate()));
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
        notesET.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                if(db != null && docRef != null && Utils.isNotNullOrEmpty(notesET.getText().toString().trim())){
                    db.document(docRef).update("notes", notesET.getText().toString());
                }
            }
            return false;
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
            palmOilIngredTV.setText(Utils.getStringArr(bp.getPalm_oil_ingredients()));
        }else{
            findViewById(R.id.palm_oil_ingr_til).setVisibility(View.GONE);
        }
        /*storage dropdown*/
        if(Utils.isNotNullOrEmpty(bp.getStorageType())){
            storgaeDropdown.setText(bp.getStorageType(), false);
        }
        /*nutrition*/
        if(Utils.isNotNullOrEmpty(bp.getNutritionPhoto()) && Utils.isNotNullOrEmpty(bp.getNutritionPhoto().getDisplay())){
            Picasso.get().load(bp.getNutritionPhoto().getDisplay()).into(nutImageIV);
        }else {
            nutImageIV.setVisibility(View.GONE);
            nutImageEditBut.setVisibility(View.GONE);
        }
        if(Utils.isNotNullOrEmpty(bp.getNutrients()) && bp.getNutrients().size()>0){
            NutrientGridAdapter nutrientGridAdapter = new NutrientGridAdapter(this, bp.getNutrients());
            nutGridV.setAdapter(nutrientGridAdapter);
        }else{
            nutGridV.setVisibility(View.GONE);
        }
    }
}