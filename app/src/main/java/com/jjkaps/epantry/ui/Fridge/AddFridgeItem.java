package com.jjkaps.epantry.ui.Fridge;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import com.jjkaps.epantry.models.ProductModels.Serving;
import com.jjkaps.epantry.ui.scanCode.ScanItem;

import java.text.ParseException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddFridgeItem extends AppCompatActivity {
    private static final String TAG = "AddFridgeManual";
    private TextView txtClose;
    private String item;
    private String quantity;
    private String expiration;
    private TextView addedExpiration;
    private EditText brandTxt, servingSize, servingUnit, ingredientsTxt;
    private Chip veganChip, vegChip, glutenChip;
    private SimpleDateFormat simpleDateFormat;
    private Button btDone;

    private String id;
    private Button choose, upload, yes;
    private TextView close;
    private ImageView imageView;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private final int PICK_IMAGE_REQUEST = 71;



    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    private FirebaseFirestore db;
    private EditText addedItem;
    private EditText addedQuantity;
    private CollectionReference fridgeListRef;
    private CollectionReference catalogListRef;
    private int flag;

    BarcodeProduct bpOld;
    DocumentReference itemRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fridge_item);
        initView();


        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        //Firebase
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user != null) {
            fridgeListRef = db.collection("users").document(user.getUid()).collection("fridgeList");
            catalogListRef = db.collection("users").document(user.getUid()).collection("catalogList");
        }

        txtClose =  findViewById(R.id.txt_close);
        btDone = findViewById(R.id.bt_done);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(2);
                finish();
            }
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

        addedExpiration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(addedExpiration);
            }
        });

        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get item
                item = addedItem.getText().toString().trim();
                quantity = addedQuantity.getText().toString().trim();
                expiration = addedExpiration.getText().toString().trim();
                id = item.toLowerCase();
                flag = 0;

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
                    Toast toast = Toast.makeText(AddFridgeItem.this, "Quantity must be greater than zero!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();

                    addedQuantity.setText(null); // resets just the quantity field
                } else if (item.length() == 0) {
                    addedItem.setError("Items can't be null!");
                } else {
                    // check serving fields
                    if (servingSize.getText().length() > 0 ^ servingUnit.getText().length() > 0) {
                        servingSize.setError(servingSize.getText().length() > 0 ? null : "Both serving fields must be filled");
                        servingUnit.setError(servingUnit.getText().length() > 0 ? null : "Both serving fields must be filled");
                    }

                    //check if item exist in fridgeList
                    fridgeListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                boolean itemNotExists = true;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (String.valueOf(document.get("name")).toLowerCase().equals(item.toLowerCase())) {
                                        addedItem.getText().clear();
                                        addedQuantity.getText().clear();
                                        addedExpiration.setText(R.string.exp_date_hint);
                                        addedItem.setError("Item exists");
                                        itemNotExists = false;
                                        break;
                                    }
                                }
                                //if not exist then add
                                if (itemNotExists) {
                                    //ADD TO CATALOG AS WELL
                                    final BarcodeProduct bp = new BarcodeProduct();
                                    bp.setName(item);
                                    bp.setBrand(brandTxt.getText().toString());
                                    bp.setQuantity(Integer.parseInt(quantity));
                                    bp.setExpDate(expiration);

                                    bp.setIngredients(ingredientsTxt.getText().toString());
                                    DietInfo di = new DietInfo(new DietLabel("Vegan", veganChip.isChecked(), 2, true, "verified by user"),
                                            new DietLabel("Gluten Free", glutenChip.isChecked(), 2, true, "verified by user"),
                                            new DietLabel("Vegetarian", vegChip.isChecked(), 2, true, "verified by user"), new ArrayList<DietFlag>());
                                    bp.setDietInfo(di);
                                    if (servingSize.getText().length() > 0 && servingUnit.getText().length() > 0) {
                                        bp.setServing(new Serving(servingSize.getText().toString(), servingUnit.getText().toString()));
                                    }
                                    catalogListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful() && task.getResult() != null) {
                                                boolean itemNotExistsInCatalog = true;
                                                for (QueryDocumentSnapshot catalogDocument : task.getResult()) {
                                                    if (String.valueOf(catalogDocument.get("name")).toLowerCase().equals(item.toLowerCase())) {
//                                                        addedItem.getText().clear();
//                                                        addedQuantity.getText().clear();
//                                                        addedExpiration.setText(R.string.exp_date_hint);
//                                                        addedItem.setError("Item exists");
                                                        itemNotExistsInCatalog = false;
                                                        break;
                                                    }
                                                }
                                                if (itemNotExistsInCatalog) {
                                                    catalogListRef.add(bp).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d(TAG, "onSuccess: " + item + " added to catalog");
//                                                            bp.setQuantity(Integer.parseInt(quantity));
//                                                            bp.setExpDate(expiration);
                                                            bp.setCatalogReference(documentReference.getPath());
                                                            fridgeListRef.add(bp).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    Log.d(TAG, "onSuccess: " + item + " added.");
                                                                    Toast toast = Toast.makeText(AddFridgeItem.this, item + " added to fridge", Toast.LENGTH_SHORT);
                                                                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                                                    toast.show();

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
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "onFailure: ", e);
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "onFailure: ", e);
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });



                                    catalogListRef.whereEqualTo("name", item)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult() != null && task.getResult().size() != 0) {
                                                            int count = 0;
                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                if (String.valueOf(document.get("name")).equalsIgnoreCase(item)) {
                                                                    count++;
                                                                    if (count > 1) {
                                                                        document.getReference().delete();
                                                                        Log.d(TAG, "removed from catalog");
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                    //txtNullList.setVisibility(View.INVISIBLE);
                                    //TODO: REFRESH PAGE TO LOAD ADDED ITEMS
                                }

                                if (itemNotExists) {
                                    setContentView(R.layout.popup_addimage);
                                    initView();
                                    yes = findViewById(R.id.upload);
                                    close = findViewById(R.id.bt_close);
                                    yes.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            showAddimage();
                                        }
                                    });

                                    close.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent returnIntent = new Intent();
                                            setResult(1, returnIntent);
                                            finish();
                                        }
                                    });
                                }

                            }

                        }
                    });

                }
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
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
//                simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                date.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };
        new DatePickerDialog(this, dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showAddimage(){
        setContentView(R.layout.activity_add_fridge_image);
        choose = findViewById(R.id.bt_choose);
        upload = findViewById(R.id.bt_upload);
        imageView = (ImageView) findViewById(R.id.imgView);

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

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
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ user.getUid()+id);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            Toast toast= Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");

                        }
                    });

        }

    }


}