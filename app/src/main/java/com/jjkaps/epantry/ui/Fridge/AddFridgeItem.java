package com.jjkaps.epantry.ui.Fridge;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ProductModels.DietInfo;
import com.jjkaps.epantry.models.ProductModels.DietLabel;
import com.jjkaps.epantry.models.ProductModels.Serving;
import com.jjkaps.epantry.utils.Utils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddFridgeItem extends AppCompatActivity {
    private static final String TAG = "AddFridgeManual";
    private TextView addedExpiration;
    private EditText brandTxt, servingSize, servingUnit, ingredientsTxt;
    private Chip veganChip, vegChip, glutenChip;
    private SimpleDateFormat simpleDateFormat;

    private String id;
    private ImageView imageView;
    private Uri filePath;
    private StorageReference storageReference;

    private final int PICK_IMAGE_REQUEST = 71;

    private FirebaseUser user;
    private EditText addedItem;
    private EditText addedQuantity;
    private CollectionReference fridgeListRef;
    private CollectionReference catalogListRef;
    private AutoCompleteTextView storageDropdown;
    private final String[] storageOptions = new String[] {"Fridge", "Freezer", "Pantry"};
    private boolean addedImage = false;
    private TextView progText;
    private RelativeLayout imageRL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fridge_item);
        initView();

        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user != null) {
            fridgeListRef = db.collection("users").document(user.getUid()).collection("fridgeList");
            catalogListRef = db.collection("users").document(user.getUid()).collection("catalogList");
        }

        TextView txtClose = findViewById(R.id.txt_close);
        Button btDone = findViewById(R.id.bt_done);
        storageReference = FirebaseStorage.getInstance().getReference();

        txtClose.setOnClickListener(v -> {
            setResult(2);
            finish();
        });
        addedItem = findViewById(R.id.inputItem);
        addedQuantity = findViewById(R.id.inputQuantity);
        addedExpiration = findViewById(R.id.inputExpiration);
        brandTxt = findViewById(R.id.brand_text);
        servingSize = findViewById(R.id.serving_size);
        servingUnit = findViewById(R.id.serving_unit);
        veganChip = findViewById(R.id.vegan_chip);
        vegChip = findViewById(R.id.veg_chip);
        glutenChip = findViewById(R.id.gluten_chip);
        ingredientsTxt = findViewById(R.id.ingredients);
        storageDropdown = findViewById(R.id.filled_exposed_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu, storageOptions);
        storageDropdown.setAdapter(adapter);
        storageDropdown.setInputType(InputType.TYPE_NULL);
        Button choose = findViewById(R.id.bt_choose);
        choose.setOnClickListener(view -> chooseImage());
        imageView = findViewById(R.id.imgView);
        imageRL = findViewById(R.id.image_upload_RL);
        progText = findViewById(R.id.progress_bar_text);

        addedExpiration.setOnClickListener(view -> showDateDialog(addedExpiration));

        btDone.setOnClickListener(view -> {
            //get item
            String itemName = addedItem.getText().toString().trim();
            String quantity = addedQuantity.getText().toString().trim();
            String expiration = addedExpiration.getText().toString().trim();
            id = itemName.toLowerCase();

            // verify quantity is valid
            Pattern containsNum = Pattern.compile("^[0-9]+$");
            Matcher isNum = containsNum.matcher(quantity);
            Date currentDate = new Date();
            Date enteredDate = null;
            try {
                enteredDate = simpleDateFormat.parse(expiration);
            } catch (ParseException e) {
                Log.d(TAG, "No date for this item");
            }
            if (enteredDate != null && currentDate.after(enteredDate)) {
                addedExpiration.setError("Enter a valid Date!");
            } else if ((quantity.equals("")) || !isNum.find() || ((Integer.parseInt(quantity) <= 0))) {
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Quantity must be greater than zero!", Utils.StatusCodes.MESSAGE);
                addedQuantity.setText(null); // resets just the quantity field
            } else if (itemName.length() == 0) {
                addedItem.setError("Items can't be null!");
            } else if (servingSize.getText().length() > 0 ^ servingUnit.getText().length() > 0) {
                servingSize.setError(servingSize.getText().length() > 0 ? null : "Both serving fields must be filled");
                servingUnit.setError(servingUnit.getText().length() > 0 ? null : "Both serving fields must be filled");
            }else {
                //check if item exist in fridgeList
                fridgeListRef.whereEqualTo("name", itemName.toLowerCase()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        //if not exist then add
                        if (task.getResult().size()==0) {
                            //ADD TO CATALOG AS WELL
                            final BarcodeProduct bp = new BarcodeProduct();
                            bp.setName(itemName.toLowerCase());
                            bp.setBrand(brandTxt.getText().toString());
                            bp.setQuantity(Integer.parseInt(quantity));
                            bp.setExpDate(expiration);
                            if(!storageDropdown.getText().toString().trim().isEmpty()) {
                                bp.setStorageType(storageDropdown.getText().toString().trim());
                            }
                            bp.setIngredients(ingredientsTxt.getText().toString());
                            DietInfo di = new DietInfo(new DietLabel("Vegan", veganChip.isChecked(), 2, true, "verified by user"),
                                    new DietLabel("Vegetarian", vegChip.isChecked(), 2, true, "verified by user"),
                                    new DietLabel("Gluten Free", glutenChip.isChecked(), 2, true, "verified by user"),
                                    new ArrayList<>());
                            bp.setDietInfo(di);
                            if (servingSize.getText().length() > 0 && servingUnit.getText().length() > 0) {
                                bp.setServing(new Serving(servingSize.getText().toString(), servingUnit.getText().toString()));
                            }
                            fridgeListRef.add(bp).addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "onSuccess: " + itemName + " added.");
                                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), itemName + " added to fridge", Utils.StatusCodes.MESSAGE);
                                final String fridgeItemID = documentReference.getId();
                                addedItem.getText().clear();
                                addedQuantity.getText().clear();
                                brandTxt.getText().clear();
                                servingSize.getText().clear();
                                servingUnit.getText().clear();
                                glutenChip.setChecked(false);
                                veganChip.setChecked(false);
                                vegChip.setChecked(false);
                                ingredientsTxt.getText().clear();
                                addedExpiration.setText(R.string.exp_date_hint);
                                storageDropdown.getText().clear();
                                catalogListRef.whereEqualTo("name", itemName.toLowerCase()).get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful() && task1.getResult() != null) {
                                        if (task.getResult().size()==0) {
                                            catalogListRef.add(BarcodeProduct.getCatalogObj(bp)).addOnSuccessListener(documentReference1 -> {
                                                bp.setCatalogReference(documentReference1.getPath());
                                                fridgeListRef.document(fridgeItemID).update("catalogReference", bp.getCatalogReference());
                                                if(addedImage){
                                                    uploadImage(fridgeItemID, documentReference1.getId());
                                                }
                                            }).addOnFailureListener(e -> Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container),"Failed to add item to catalog.", Utils.StatusCodes.FAILURE));
                                        } else{
                                            // if the item added to fridge is in the catalog, it is not suggested TODO when should you set this?
                                            //catalogDocument.getReference().update("suggested", false);
                                        }
                                    }
                                });
                            }).addOnFailureListener(e -> Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container),"Failed to add item to fridge.", Utils.StatusCodes.FAILURE));

                            catalogListRef.whereEqualTo("name", itemName).get().addOnCompleteListener(task12 -> {//TODO do we need this??
                                if (task12.isSuccessful()) {
                                    if (task12.getResult() != null && task12.getResult().size() != 0) {
                                        int count = 0;
                                        for (QueryDocumentSnapshot document : task12.getResult()) {
                                            if (String.valueOf(document.get("name")).equalsIgnoreCase(itemName)) {
                                                count++;
                                                if (count > 1) {
                                                    document.getReference().delete();
                                                    Log.d(TAG, "removed from catalog");
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                            //txtNullList.setVisibility(View.INVISIBLE);
                        }else{
                            Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container),"A Product with this name already exists", Utils.StatusCodes.FAILURE);
                            addedItem.getText().clear();
                            addedQuantity.getText().clear();
                            addedExpiration.setText(R.string.exp_date_hint);
                            addedItem.setError("Item exists");
                        }
                    }
                });
            }
        });
    }

    private void initView(){
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
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
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
            StorageReference ref = storageReference.child("images/"+ user.getUid()+id);
            ref.putFile(filePath)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRL.setVisibility(View.GONE);
                    Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container), "Image Uploaded Successfully ");
                    fridgeListRef.document(fridgeItemID).update("userImage","images/"+ user.getUid()+id);
                    catalogListRef.document(catalogItemID).update("userImage","images/"+ user.getUid()+id);
                    imageView.setImageResource(R.drawable.image_not_found);
                    addedImage = false;
                }).addOnFailureListener(e -> {
                    imageRL.setVisibility(View.GONE);
                    addedImage = false;
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                            .getTotalByteCount());
                    progText.setText(("Uploaded "+(int)progress+"%"));
                });
        }
    }
}