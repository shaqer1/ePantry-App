package com.jjkaps.epantry.ui.scanCode;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.utils.ChompAPI;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

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
    private TextView progreesBarText;
    private Button updateButton;

    private Calendar myCalendar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser u;

    private EditText expDateEdit;
    private EditText qtyEdit;
    private SimpleDateFormat expDateFormat;
    private AutoCompleteTextView storgaeDropdown;
    private final String[] storageOptions = new String[] {"Fridge", "Freezer", "Pantry"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);
        //init views
        cameraView = findViewById(R.id.camera_view_scan);
        statusTextView = findViewById(R.id.barcodeStatus);
        progreesBarText = findViewById(R.id.progress_bar_text);
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
        expDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        expDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ScanItem.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        expDateEdit.setText(expDateFormat.format(myCalendar.getTime()));

                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        storgaeDropdown = findViewById(R.id.filled_exposed_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu, storageOptions);
        storgaeDropdown.setAdapter(adapter);
        storgaeDropdown.setInputType(InputType.TYPE_NULL);



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
                        Toast.makeText(ScanItem.this, "Need camera permission to scan items.", Toast.LENGTH_LONG).show();
                    }
                }).check();
    }

    private void setupCamera() {
        cameraView.setLifecycleOwner(this);
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                //process each frame from camera
                processImage(frame);
            }
        });
    }

    private void processImage(Frame frame) {
        //create barcode detector
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
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
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    if(barcodes.size() > 0){
                        processBarcodes(barcodes.get(0));
                    }
                }
            });
        } else if (frame.getDataClass() == Image.class) {
            Image data = frame.getData();
            s.process(InputImage.fromMediaImage(data, frame.getRotationToView())).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    if(barcodes.size() > 0){
                        processBarcodes(barcodes.get(0));
                    }
                }
            });
        }
    }

    private void processBarcodes(final Barcode barcode) {//TODO: add checksum for barcode
            // See API reference for complete list of supported types
            switch (barcode.getValueType()) {
                case Barcode.TYPE_PRODUCT:
                    //to make sure you didn't just scan this
                    if (barcode.getRawValue() != null && !barcode.getRawValue().equals(lastRawBarcode)){
                        //not a current product
                        apiProgressRL.setVisibility(View.VISIBLE);
                        progreesBarText.setText(("Searching for "+barcode.getRawValue()));
                        //pad to appropriate length for query
                        final String padCode = barcode.getRawValue().length() != 8 ? padUAN13(barcode.getRawValue()): barcode.getRawValue();
                        //call API
                        db.collection("users").document(u.getUid()).collection("fridgeList").document(padCode).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if(document != null && document.exists()){//doc exists
                                        final BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                                        updateItem(bp);
                                    } else {
                                        addItem(padCode, barcode);
                                    }
                                } else { // doc doesn't exist
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                    break;
                default:
                    //by default reset the views
                    //couldn't classify as product
                    addFailed("Couldn't find " + barcode.getRawValue() + " as a product. Please try again");
                    break;
            }
        lastRawBarcode = barcode.getRawValue();
    }

    private void updateItem(final BarcodeProduct bp) {
        //update views
        apiProgressRL.setVisibility(View.GONE);
        dataInputLayout.setVisibility(View.VISIBLE);
        storage_ll.setVisibility(View.VISIBLE);
        if (bp != null) {
            if(bp.getExpDate() != null){
                expDateEdit.setText(bp.getExpDate());
            }else {
                expDateEdit.getText().clear();
            }
            if(bp.getStorageType() != null){
                storgaeDropdown.setText(bp.getStorageType(), false);
            }else {
                storgaeDropdown.getText().clear();
            }
            qtyEdit.setText(String.valueOf(bp.getQuantity()));
        } else {
            expDateEdit.getText().clear();
            qtyEdit.getText().clear();
            storgaeDropdown.getText().clear();
        }
        updateButton.setEnabled(true);
        scanThumb.setVisibility(View.VISIBLE); // TODO update with thumb
        dataInputProgressBar.setVisibility(View.GONE);
        //add update on click listener
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickUpdate(bp);
            }
        });
        statusTextView.setText(String.format("%s is already added update details below or continue scanning!", bp != null ? bp.getName() : "{NoName}"));
    }

    private void addFailed(String msg) {
        apiProgressRL.setVisibility(View.GONE);
        scanThumb.setVisibility(View.GONE);
        dataInputProgressBar.setVisibility(View.VISIBLE);
        dataInputLayout.setVisibility(View.INVISIBLE);
        storage_ll.setVisibility(View.GONE);
        updateButton.setOnClickListener(null);
        statusTextView.setText(msg);
    }

    private void addItem(String padCode, final Barcode barcode) {
        ChompAPI.addProduct(padCode, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                BarcodeProduct bp = new BarcodeProduct();
                //Received result
                apiProgressRL.setVisibility(View.GONE);
                if (u != null) {
                    //add to catalog
                    addToCatalog(BarcodeProduct.processJSON(response, bp));
                }//TODO handle else
                //update views
                dataInputLayout.setVisibility(View.VISIBLE);
                storage_ll.setVisibility(View.VISIBLE);
                expDateEdit.getText().clear();
                storgaeDropdown.getText().clear();
                qtyEdit.getText().clear();
                updateButton.setEnabled(false);
                scanThumb.setVisibility(View.VISIBLE); // TODO update with thumb
                dataInputProgressBar.setVisibility(View.GONE);
                statusTextView.setText(String.format("Found %s add details below or continue scanning!", bp.getName()));
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

    private void addToCatalog(final BarcodeProduct bp) {
        db.collection("users").document(u.getUid()).collection("catalogList").document(bp.getBarcode()).set(bp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //add to fridge with reference
                        bp.setCatalogReference(db.collection("users").document(u.getUid()).collection("catalogList").document(bp.getBarcode()).getPath());
                        statusTextView.setText(String.format("Added %s to your fridge, add details below or continue scanning!", bp.getName()));
                        addToFridge(bp);

                    }
                });//todo onfail listener
    }

    private void addToFridge(final BarcodeProduct bp) {
        db.collection("users").document(u.getUid()).collection("fridgeList").document(bp.getBarcode()).set(bp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //successfully added
                updateButton.setEnabled(true);
                //add update on click listener
                updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onClickUpdate(bp);

                    }
                });
            }
        });//todo on fail listener
    }

    private void onClickUpdate(final BarcodeProduct bp) {
        if(expDateEdit.getText().toString().trim().isEmpty()) {
            expDateEdit.setError("Cannot be empty");
            return;
        }
        if(storgaeDropdown.getText().toString().trim().isEmpty()) {
            storgaeDropdown.setError("Please select Storage type");
            return;
        }
        if(qtyEdit.getText().toString().trim().isEmpty()) {
            qtyEdit.setError("Cannot be empty");
            return;
        }
        //remove keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(updateButton.getWindowToken(), 0);
        if (mAuth.getCurrentUser() != null){
            bp.setQuantity(Integer.parseInt(qtyEdit.getText().toString().trim()));
            //get date
            Date d = null;
            try {
                d = expDateFormat.parse(expDateEdit.getText().toString().trim());
            } catch (ParseException e) {
                e.printStackTrace();
                Log.d(TAG,"could not parse date in scan"+ e.getMessage());
            }
            if (d != null) {
                bp.setExpDate(expDateFormat.format(d));
            }
            //update items
            db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("fridgeList").document(bp.getBarcode())
                    .update("quantity",Integer.parseInt(qtyEdit.getText().toString().trim())
                            , "expDate", expDateEdit.getText().toString().trim()
                            , "storageType", storgaeDropdown.getText().toString().trim())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    statusTextView.setText(String.format("Updated %s in your fridge, you may continue scanning!", bp.getName()));
                }
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