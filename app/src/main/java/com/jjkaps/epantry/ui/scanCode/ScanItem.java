package com.jjkaps.epantry.ui.scanCode;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.utils.ChompAPI;
import com.jjkaps.epantry.utils.Utils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ScanItem extends AppCompatActivity {

    private static final String TAG = "ScanItem";
    private CameraView cameraView;
    private TextView statusTextView;
    private String lastRawBarcode = "";
    private RelativeLayout apiProgressRL;
    private LinearLayout dataInputLayout;
    private LinearLayout storage_ll;

    private ImageView scanThumb;
    private ProgressBar dataInputProgressBar;
    private TextView progressBarText;
    private Button updateButton;

    private Calendar myCalendar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser u;

    private EditText expDateEdit;
    private EditText qtyEdit;
    private SimpleDateFormat expDateFormat;
    private AutoCompleteTextView storageDropdown;
    private final String[] storageOptions = new String[] {"Fridge", "Freezer", "Pantry"};
    private View animScan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);
        customActionBar();
        //send firebase analytic
        Utils.addAnalytic(TAG, "opened scan activity", "text", this);
        //animation scan bar
        Animation animationDown = AnimationUtils.loadAnimation(this, R.anim.scale_animation_y_down);
        Animation animationUp = AnimationUtils.loadAnimation(this, R.anim.scale_animation_y_up);
        animScan = findViewById(R.id.scanBar);
        animScan.post(() -> animScan.startAnimation(animationDown));
        animationDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animScan.startAnimation(animationUp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animScan.startAnimation(animationDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //init views
        cameraView = findViewById(R.id.camera_view_scan);
        statusTextView = findViewById(R.id.barcodeStatus);
        progressBarText = findViewById(R.id.progress_bar_text);
        apiProgressRL = findViewById(R.id.api_progress);
        scanThumb = findViewById(R.id.scan_thumb);
        dataInputProgressBar = findViewById(R.id.progress_scanning);
        dataInputLayout = findViewById(R.id.data_layout_scan);
        storage_ll = findViewById(R.id.storage_ll);
        updateButton = findViewById(R.id.scan_data_update);
        expDateEdit = findViewById(R.id.expDate);
        qtyEdit = findViewById(R.id.scan_Qty);
        //create date picker
        myCalendar = Calendar.getInstance();
        expDateFormat = Utils.getExpDateFormat();
        expDateEdit.setOnClickListener(view -> new DatePickerDialog(ScanItem.this, (datePicker, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            expDateEdit.setText(expDateFormat.format(myCalendar.getTime()));

        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        storageDropdown = findViewById(R.id.filled_exposed_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu, storageOptions);
        storageDropdown.setAdapter(adapter);
        storageDropdown.setInputType(InputType.TYPE_NULL);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        u = mAuth.getCurrentUser();

        //check and ask for permissions
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        setupCamera();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container), "Need camera permission to scan items.", Utils.StatusCodes.FAILURE);
                        finish();
                    }
                }).check();
    }

    private void customActionBar() {
        if(this.getSupportActionBar() != null){
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setDisplayShowCustomEnabled(true);
            //getSupportActionBar().setIcon(new ColorDrawable(getColor(R.color.colorWhite)));
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);

            View customBarView = getSupportActionBar().getCustomView();
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            TextView name = customBarView.findViewById(R.id.name);
            Button close = customBarView.findViewById(R.id.txt_close);
            close.setVisibility(View.VISIBLE);
            name.setText(R.string.ScanName);
            close.setOnClickListener(view1 -> finish());
        }
    }

    private void setupCamera() {
        cameraView.setLifecycleOwner(this);
        //process each frame from camera
        cameraView.addFrameProcessor(this::processImage);
    }

    private void processImage(Frame frame) {
        //create barcode detector
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_CODABAR,
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_EAN_13,
                                Barcode.FORMAT_EAN_8,
                                Barcode.FORMAT_UPC_A,
                                Barcode.FORMAT_UPC_E)
                        .build();
        BarcodeScanner s = BarcodeScanning.getClient(options);
        //get byte array from frame
        // Process camera frame media image and check barcodes
        if (frame.getDataClass() == byte[].class) {
            byte[] data = frame.getData();
            s.process(InputImage.fromByteBuffer(ByteBuffer.wrap(data), frame.getSize().getWidth(), frame.getSize().getHeight(), frame.getRotationToView(),frame.getFormat()))
                    .addOnSuccessListener(barcodes -> {
                        if(barcodes.size() > 0){
                            processBarcode(barcodes.get(0));
                        }
                    });
        } else if (frame.getDataClass() == Image.class) {
            Image data = frame.getData();
            s.process(InputImage.fromMediaImage(data, frame.getRotationToView())).addOnSuccessListener(barcodes -> {
                if(barcodes.size() > 0){
                    processBarcode(barcodes.get(0));
                }
            });
        }
    }

    private void processBarcode(final Barcode barcode) {//TODO: add checksum for barcode
            // See API reference for complete list of supported types
        if (barcode.getValueType() == Barcode.TYPE_PRODUCT) {//to make sure you didn't just scan this
            if (barcode.getRawValue() != null && !barcode.getRawValue().equals(lastRawBarcode)) {
                //not a current product
                apiProgressRL.setVisibility(View.VISIBLE);
                progressBarText.setText(String.format("Searching for %s", barcode.getRawValue()));
                //pad to appropriate length for query
                final String padCode = barcode.getRawValue().length() != 8 ? padUAN13(barcode.getRawValue()) : barcode.getRawValue();
                //call API
                Utils.getFridgeListRef(u).document(padCode).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {//doc exists
                            final BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                            updateItem(bp);
                        } else {
                            //send firebase analytic
                            Utils.addAnalytic(TAG, "Scanned item: " + padCode, "text: barcode", ScanItem.this);
                            addItem(padCode, barcode);
                        }
                    } else { // doc doesn't exist
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });
            }
        } else {//by default reset the views
            //couldn't classify as product
            addFailed("Couldn't find " + barcode.getRawValue() + " as a product. Please try again");
        }
        lastRawBarcode = barcode.getRawValue();
    }

    private void updateItem(final BarcodeProduct bp) {
        //update views
        apiProgressRL.setVisibility(View.GONE);
        dataInputLayout.setVisibility(View.VISIBLE);
        storage_ll.setVisibility(View.VISIBLE);
        if (bp != null) {
            if(bp.getInventoryDetails().getExpDate() != null){
                expDateEdit.setText(Utils.getExpDateFormat().format(bp.getInventoryDetails().getExpDate()));
            }else {
                expDateEdit.getText().clear();
            }
            if(bp.getStorageType() != null){
                storageDropdown.setText(bp.getStorageType(), false);
            }else {
                storageDropdown.getText().clear();
            }
            qtyEdit.setText(String.valueOf(bp.getInventoryDetails().getQuantity()));
            scanThumb.setVisibility(View.VISIBLE);
            setImage(bp);
            Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container),String.format("%s is already added update details below or continue scanning!", bp.getName()),
                    Utils.StatusCodes.SUCCESS);
            statusTextView.setVisibility(View.GONE);

        } else {
            expDateEdit.getText().clear();
            qtyEdit.getText().clear();
            storageDropdown.getText().clear();
        }
        updateButton.setEnabled(true);
        dataInputProgressBar.setVisibility(View.GONE);
        //add update on click listener
        updateButton.setOnClickListener(view -> onClickUpdate(bp));
    }

    private void addFailed(String msg) {
        apiProgressRL.setVisibility(View.GONE);
        scanThumb.setVisibility(View.GONE);
        dataInputProgressBar.setVisibility(View.VISIBLE);
        dataInputLayout.setVisibility(View.INVISIBLE);
        storage_ll.setVisibility(View.GONE);
        updateButton.setOnClickListener(null);
        statusTextView.setText(R.string.ready_to_scan);
        statusTextView.setVisibility(View.VISIBLE);
        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), msg, Utils.StatusCodes.FAILURE);
    }

    private void addItem(String padCode, final Barcode barcode) {
        ChompAPI.addProduct(padCode, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                BarcodeProduct bp = new BarcodeProduct();
                //Received result
                apiProgressRL.setVisibility(View.GONE);
                //add to catalog
                addToFridge(BarcodeProduct.processJSON(response, bp));
                //update views
                dataInputLayout.setVisibility(View.VISIBLE);
                storage_ll.setVisibility(View.VISIBLE);
                expDateEdit.getText().clear();
                storageDropdown.getText().clear();
                qtyEdit.getText().clear();
                updateButton.setEnabled(false);
                scanThumb.setVisibility(View.VISIBLE);
                setImage(bp);
                dataInputProgressBar.setVisibility(View.GONE);
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), String.format("Found %s add details below or continue scanning!", bp.getName()), Utils.StatusCodes.SUCCESS);
                statusTextView.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //API returned error
                if(statusCode == 404){
                    //product not found
                    addFailed("Could not find "+barcode.getRawValue()+", Please try again.");
                }else{
                    //some other error
                    addFailed("Unknown error occurred, please try again.");
                }
            }
        });
    }

    private void addToFridge(final BarcodeProduct bp) {
        Utils.getFridgeListRef(u).document(bp.getBarcode()).set(bp)
            .addOnSuccessListener(aVoid -> {
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), String.format("Added %s to your fridge, add details below or continue scanning!", bp.getName()), Utils.StatusCodes.SUCCESS);
                statusTextView.setVisibility(View.GONE);
                //successfully added
                updateButton.setEnabled(true);
                //add update on click listener
                updateButton.setOnClickListener(view -> onClickUpdate(bp));
            }).addOnFailureListener(e -> addFailed("Error occurred while adding to fridge, please try again later."));
    }

    private void setImage(BarcodeProduct bp) {
        if(Utils.isNotNullOrEmpty(bp.getFrontPhoto()) && Utils.isNotNullOrEmpty(bp.getFrontPhoto().getThumb())){
            Picasso.get().load(bp.getFrontPhoto().getThumb()).into(scanThumb);
        }else{
            scanThumb.setImageResource(R.drawable.barcode_done);
        }
    }

    private void onClickUpdate(final BarcodeProduct bp) {
        if(expDateEdit.getText().toString().trim().isEmpty()) {
            expDateEdit.setError("Cannot be empty");
            return;
        }
        if(storageDropdown.getText().toString().trim().isEmpty()) {
            storageDropdown.setError("Please select Storage type");
            return;
        }
        if(qtyEdit.getText().toString().trim().isEmpty()) {
            qtyEdit.setError("Cannot be empty");
            return;
        }
        qtyEdit.setError(null);
        expDateEdit.setError(null);
        storageDropdown.setError(null);
        //remove keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(updateButton.getWindowToken(), 0);
        if (mAuth.getCurrentUser() != null){
            bp.getInventoryDetails().setQuantity(Integer.parseInt(qtyEdit.getText().toString().trim()));
            //get date
            Date d = null;
            try {
                d = expDateFormat.parse(expDateEdit.getText().toString().trim());
            } catch (ParseException e) {
                e.printStackTrace();
                Log.d(TAG,"could not parse date in scan"+ e.getMessage());
            }
            if (d != null) {
                bp.getInventoryDetails().setExpDate(d);
            }
            //update items
            db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("fridgeList").document(bp.getBarcode())
                    .update("inventoryDetails", bp.getInventoryDetails()
                            , "storageType", storageDropdown.getText().toString().trim())
                    .addOnSuccessListener(aVoid -> {
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), String.format("Updated %s in your fridge, you may continue scanning!", bp.getName()), Utils.StatusCodes.SUCCESS);
                        statusTextView.setVisibility(View.GONE);
                    });
        }
    }

    private String padUAN13(String rawValue) {
        //pad to 13 digits
        StringBuilder rawValueBuilder = new StringBuilder(rawValue);
        while(rawValueBuilder.length() < 13){
            rawValueBuilder.insert(0, "0");
        }
        rawValue = rawValueBuilder.toString();
        return rawValue;
    }
}