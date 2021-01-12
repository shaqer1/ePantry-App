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
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ProductModels.InventoryDetails;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.ui.ItemUI.ExpListView.CustomExpListView;
import com.jjkaps.epantry.ui.ItemUI.ExpListView.CustomItemExpViewAdapter;
import com.jjkaps.epantry.ui.Recipes.BPAdapterItem;
import com.jjkaps.epantry.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST_ADAPTER = 5;
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

    private BarcodeProduct bp;
    private String itemName;

    private SimpleDateFormat simpleDateFormat;
    private ImageView imageIV;
    private RelativeLayout imageRL;
    private TextView expirationTV;
    private TextInputEditText nameTV, quantityTV, brandTV;
    private Button addFridgeListBT;
    private Button addShoppingListBT, addRemoveCatalog, resetImageBut, editImageBT, qtyIncBut, qtyDecBut;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private TextView progText;

    private static final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private boolean addedImage = false;
    private StorageReference storageReference;
    private CustomExpListView expListView;
    private CustomItemExpViewAdapter expListViewAdapter;
    private boolean changed = false;

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

        //private DocumentReference catalogRef;
        String currentCollection = getIntent().getStringExtra("currCollection");
        initView();
        if(bp != null){
            setListeners();
            if(!bp.isInStock()){
                addRemoveCatalog.setVisibility(View.VISIBLE);
            }
            initText();
        }else{
            Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container), "Could not load details :(", Utils.StatusCodes.FAILURE);
            finish();
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
                    //expListViewAdapter.clearFocus();
                    expListView.clearFocus();
                    /*for(TextInputEditText et: expListViewAdapter.getEditTextMap().values()){
                        *//*et.setFocusable(false);
                        et.setFocusable(true);*//*
                        et.clearFocus();
                    }*/
                    quantityTV.clearFocus();
                    nameTV.clearFocus();
                    brandTV.clearFocus();
                    Utils.hideKeyboard(ItemActivity.this, expListView);

                    changed = expListViewAdapter.getOnClick(changed);

                    // exp date changed
                    if (Utils.isNotNullOrEmpty(expirationTV.getText().toString().trim())) {
                        // does not check if date entered has passed b/c people still keep food past exp
                        if (bp.getInventoryDetails().getExpDate() == null || !simpleDateFormat.format(bp.getInventoryDetails().getExpDate()).equals(expirationTV.getText().toString().trim())) {
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
                        changed = false;
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Item updated!", Utils.StatusCodes.SUCCESS);
                    } else {
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Item is up to date.", Utils.StatusCodes.INFO);
                    }
                }
            });
        }

        // update item info button
        addFridgeListBT = findViewById(R.id.bt_addFridgeList);
        if (currentCollection != null && !currentCollection.equals("fridgeList")) {//DONE check doc ref instead of name in activity
            fridgeListRef.document(Utils.getDocId(docRef)).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    BarcodeProduct bp = task.getResult().toObject(BarcodeProduct.class);
                    if (bp == null || bp.isInStock()) {
                        addFridgeListBT.setVisibility(View.GONE);
                        addShoppingListBT.setVisibility(View.VISIBLE);
                    } else {
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

        // add item to shopping list button
        addShoppingListBT = findViewById(R.id.bt_addShoppingList);
        checkIfItemInShopping();
        addShoppingListBT.setOnClickListener(view -> {//DONE? if in fridge don't let it add to shopping
            if(addShoppingListBT.isEnabled()){
                goToAddItemActivity(docRef);
            }
        });

        addRemoveCatalog.setOnClickListener(view -> {
            db.document(docRef).delete();
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), bp.getName()+" removed from Account", Utils.StatusCodes.SUCCESS);
            finish();

        });
    }

    private void setListeners() {
        // update the exp date
        expirationTV.setOnClickListener(view -> showDateDialog(expirationTV));
        editImageBT.setOnClickListener(view -> chooseImage());
        resetImageBut.setOnClickListener(view -> {
            bp.setCustImage(false);
            db.document(docRef).update("custImage", bp.isCustImage());
        });
        nameTV.setOnFocusChangeListener((view, b) -> {
            if(!b){
                // if notes changed
                if(Utils.isNotNullOrEmpty(nameTV.getText())  && !nameTV.getText().toString().equals(bp.getName())){
                    bp.setName(nameTV.getText().toString().trim());
                    changed = true;
                }
            }
        });
        quantityTV.setOnFocusChangeListener((view, b) -> {
            if(!b){
                // if notes changed
                if(Utils.isNotNullOrEmpty(quantityTV.getText())  && bp.getInventoryDetails()!=null && !quantityTV.getText().toString().trim().equals(bp.getInventoryDetails().getQuantity()+"")){
                    bp.getInventoryDetails().setQuantity(Integer.parseInt(quantityTV.getText().toString().trim()));
                    handleQtyChange(bp);
                    changed = true;
                }
            }
        });
        qtyDecBut.setOnClickListener(view -> {
            bp.getInventoryDetails().setQuantity(bp.getInventoryDetails().getQuantity() - 1);
            handleQtyChange(bp);
        });
        qtyIncBut.setOnClickListener(view1 -> {
            bp.getInventoryDetails().setQuantity(bp.getInventoryDetails().getQuantity() + 1);
            handleQtyChange(bp);
        });
        brandTV.setOnFocusChangeListener((view, b) -> {
            if(!b){
                // if notes changed
                if(Utils.isNotNullOrEmpty(brandTV.getText())  && !brandTV.getText().toString().trim().equals(bp.getBrand())){
                    bp.setBrand(brandTV.getText().toString().trim());
                    changed = true;
                }
            }
        });
    }

    private void checkIfItemInShopping() {
        shopListRef.whereEqualTo("docReference", docRef).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                //Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), bp.getName()+ " is already in your Shopping List", Utils.StatusCodes.FAILURE);
                addShoppingListBT.setEnabled(task.getResult().size() == 0);
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
        if(requestCode == PICK_IMAGE_REQUEST_ADAPTER && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            expListViewAdapter.onActivityResult(requestCode, resultCode, data, user);
        } else if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageIV.setImageBitmap(bitmap);
                addedImage = true;
                uploadImage();//don't need this.
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
                        bp.setCustImage(true);
                        db.document(docRef).update("userImage", bp.getUserImage());
                        db.document(docRef).update("userImageDateModified", bp.getUserImageDateModified());
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
        expListView = findViewById(R.id.productIngredientsELV);
        //image
        imageRL = findViewById(R.id.image_upload_RL);
        imageIV = findViewById(R.id.item_image);
        // update the image
        editImageBT = findViewById(R.id.editImageBT);
        resetImageBut = findViewById(R.id.resetImageBT);
        //name
        nameTV = findViewById(R.id.item_name);
        itemName = this.bp.getName().toLowerCase();
        //qty
        quantityTV = findViewById(R.id.item_quantity);
        qtyDecBut = findViewById(R.id.btn_dec);
        qtyIncBut = findViewById(R.id.btn_inc);
        //exp
        expirationTV = findViewById(R.id.item_exp);
        //brand
        brandTV = findViewById(R.id.item_brand);
        //buttons
        addRemoveCatalog = findViewById(R.id.bt_add_remove_catalog);
        //progress
        progText = findViewById(R.id.progress_bar_text);


        expListViewAdapter = new CustomItemExpViewAdapter(this, findViewById(R.id.container), new BPAdapterItem(bp, docRef), imageRL, progText);
        setGroupIndicatorToRight();
        expListView.setAdapter(expListViewAdapter);
        expListView.setExpanded(true);

    }

    private void handleQtyChange(BarcodeProduct bp) {
        // suggest items that have been decremented to 1 and are NOT favorites
        if (bp != null && Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && bp.getInventoryDetails().getQuantity()<=2 && !bp.getFavorite()) {
            db.document(docRef).update("suggested", true);
        }
        // cannot decrement to 0
        if (bp != null && Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && bp.getInventoryDetails().getQuantity()!=0) {
            //bp.getInventoryDetails().setQuantity(bp.getInventoryDetails().getQuantity() - 1);
            quantityTV.setText((bp.getInventoryDetails().getQuantity()+""));
            // find the item that's quantity is being updated
            db.document(docRef).update("inventoryDetails", bp.getInventoryDetails(), "suggested", false);

        } else if(bp != null && Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && bp.getInventoryDetails().getQuantity()==0) { // remove item from fridgeList when quantity reaches zero
            //get fav boolean and make item suggested if not a fav
            if (!bp.getFavorite()) {
                db.document(docRef).update("suggested", true);
            } else if (bp.getFavorite()) {
                //automatically add item to shopping list
                addFavToShopping(bp);
            } else {
                Intent intent = new Intent(ItemActivity.this, AddFridgeToShopping.class);
                Bundle b = new Bundle();
                b.putString("itemName", bp.getName()); // add item name
                b.putString("docRef", docRef); // add item ref
                intent.putExtras(b); // associate name with intent
                startActivity(intent);
            }

            // remove item from the fridge
            bp.setInventoryDetails(new InventoryDetails(null, 0));
            db.document(docRef).update("inventoryDetails", bp.getInventoryDetails(), "inStock", false);
        }
        if(bp != null && Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && bp.getInventoryDetails().getQuantity()==1){
            qtyDecBut.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_delete_24dp, null));
        }else{
            qtyDecBut.setBackground(ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_minus_24dp, null));
        }

    }

    public void addFavToShopping(BarcodeProduct bp) {
        final String itemName = bp.getName();

        Utils.getShoppingListRef(user).whereEqualTo("docReference", docRef).get().addOnCompleteListener(task -> {//TODO change with doc ref TEST?
            if (task.isSuccessful() && task.getResult() != null) {
                if (task.getResult().size() == 0) {
                    ShoppingListItem sli = new ShoppingListItem(itemName.toLowerCase(), 1, false, "", docRef);
                    Utils.getShoppingListRef(user).add(sli).addOnSuccessListener(documentReference ->
                            Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), itemName + " added to Shopping List", Utils.StatusCodes.SUCCESS)
                    ).addOnFailureListener(e ->
                            Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Failed to add " + itemName + " to Shopping List", Utils.StatusCodes.FAILURE)
                    );
                } else {
                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), itemName + " is already in the Shopping List", Utils.StatusCodes.FAILURE);
                }
            }
        });
    }

    private void setGroupIndicatorToRight() {
        // Get the screen width
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
    }

    private void initText() {
        /*set photo*/
        if(bp.isCustImage()){
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
        if(bp.getInventoryDetails().getQuantity() == 1){
            qtyDecBut.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_delete_24dp, null));
        }else{
            qtyDecBut.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_minus_24dp, null));
        }
        if(Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && bp.getInventoryDetails().getQuantity() != 0){
            quantityTV.setText(String.valueOf(bp.getInventoryDetails().getQuantity()));
        }else {
            findViewById(R.id.quantity_til).setVisibility(View.GONE);
        }
        /*expiration*/
        if(Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && Utils.isNotNullOrEmpty(bp.getInventoryDetails().getExpDate()) ){
            expirationTV.setText(simpleDateFormat.format(bp.getInventoryDetails().getExpDate()));
        }
        /*brand*/
        if(Utils.isNotNullOrEmpty(bp.getBrand())){
            brandTV.setText(bp.getBrand());
        }
    }
}