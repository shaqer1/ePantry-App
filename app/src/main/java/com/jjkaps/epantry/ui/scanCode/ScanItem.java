package com.jjkaps.epantry.ui.scanCode;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import static java.lang.Thread.sleep;

public class ScanItem extends AppCompatActivity {

    private static final String TAG = "ScanItem";
    private CameraView cameraView;
    private BarcodeDetector detector;
    private TextView statusTextView;
    private String lastRawBarcode = "";
    private RelativeLayout apiProgressRL;
    private LinearLayout dataInputLayout;
    private ImageView scanThumb;
    private ProgressBar dataInputProgressBar;
    private TextView progreesBarText;
    private Button updateButton;
    private FirebaseAuth mAuth;
    private EditText expDateEdit;
    private Calendar myCalendar;
    private EditText qtyEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);
        cameraView = findViewById(R.id.camera_view_scan);
        statusTextView = findViewById(R.id.barcodeStatus);
        progreesBarText = findViewById(R.id.progress_bar_text);
        apiProgressRL = findViewById(R.id.api_progress);
        scanThumb = findViewById(R.id.scan_thumb);
        dataInputProgressBar = findViewById(R.id.progress_scanning);
        dataInputLayout = findViewById(R.id.data_layout_scan);
        updateButton = findViewById(R.id.scan_data_update);
        expDateEdit = findViewById(R.id.expDate);
        qtyEdit = findViewById(R.id.scan_Qty);
        myCalendar = Calendar.getInstance();
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
                        expDateEdit.setText(new SimpleDateFormat("MM/dd/yy").format(myCalendar.getTime()));
                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        //Firebase
        mAuth = FirebaseAuth.getInstance();

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
                processImage(frame);
            }
        });
    }

    private void processImage(Frame frame) {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_EAN_13,
                                Barcode.FORMAT_EAN_8,
                                Barcode.FORMAT_UPC_A,
                                Barcode.FORMAT_UPC_E)
                        .build();
        BarcodeScanner s = BarcodeScanning.getClient(options);
        if (frame.getDataClass() == byte[].class) {
            byte[] data = frame.getData();
            s.process(InputImage.fromByteBuffer(ByteBuffer.wrap(data), frame.getSize().getWidth(), frame.getSize().getHeight(), frame.getRotationToView(),frame.getFormat()))
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    if(barcodes.size() > 0){
                        processBarcodes(barcodes);
                    }
                }
            });
            // Process byte array...
        } else if (frame.getDataClass() == Image.class) {
            Image data = frame.getData();
            // Process android.media.Image...\
            s.process(InputImage.fromMediaImage(data, frame.getRotationToView())).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    if(barcodes.size() > 0){
                        processBarcodes(barcodes);
                    }
                }
            });
        }
    }

    private void processBarcodes(List<Barcode> barcodes) {//TODO: add checksum for barcode
        for (final Barcode barcode: barcodes) {
            // See API reference for complete list of supported types
            switch (barcode.getValueType()) {
                case Barcode.TYPE_PRODUCT:
                    //make sure you didn't just scan this
                    if (barcode.getRawValue() != null && !barcode.getRawValue().equals(lastRawBarcode)){
                        //not a current product
                        lastRawBarcode = barcode.getRawValue();
                        apiProgressRL.setVisibility(View.VISIBLE);
                        progreesBarText.setText(("Searching for "+barcode.getRawValue()));
                        final BarcodeProduct bp = new BarcodeProduct();
                        //pad to appropriate length for query
                        String padCode = barcode.getRawValue().length() != 8 ? padUAN13(barcode.getRawValue()): barcode.getRawValue();
                        //call API
                        //TODO: check if exists in firebase with padCode
                        ChompAPI.addProduct(padCode, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                //Received result
                                apiProgressRL.setVisibility(View.GONE);
                                final FirebaseUser u = mAuth.getCurrentUser();
                                if (u != null) {
                                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    final BarcodeProduct bp = BarcodeProduct.processJSON(response);
                                    //add to catalog
                                    db.collection("users").document(u.getUid()).collection("catalogList").document(bp.getBarcode()).set(bp)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //add to fridge with reference
                                                    bp.setCatalogReference(db.collection("users").document(u.getUid()).collection("catalogList").document(bp.getBarcode()));
                                                    statusTextView.setText(String.format("Added %s to your fridge, add details below or continue scanning!", bp.getName()));
                                                    db.collection("users").document(u.getUid()).collection("fridgeList").document(bp.getBarcode()).set(bp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            //successfully added
                                                            updateButton.setEnabled(true);
                                                            //add update on click listener
                                                            updateButton.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    if(expDateEdit.getText().toString().trim().isEmpty()) {
                                                                        expDateEdit.setError("Cannot be empty");
                                                                        return;
                                                                    }
                                                                    if(qtyEdit.getText().toString().trim().isEmpty()) {
                                                                        qtyEdit.setError("Cannot be empty");
                                                                        return;
                                                                    }
                                                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                    imm.hideSoftInputFromWindow(updateButton.getWindowToken(), 0);
                                                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                    if (mAuth.getCurrentUser() != null){
                                                                        bp.setQuantity(Integer.parseInt(qtyEdit.getText().toString().trim()));
                                                                        Date d = null;
                                                                        try {
                                                                            d = new SimpleDateFormat("MM/dd/yy").parse(expDateEdit.getText().toString().trim());
                                                                        } catch (ParseException e) {
                                                                            e.printStackTrace();
                                                                            Log.d(TAG,"could not parse date in scan"+ e.getMessage());
                                                                        }
                                                                        bp.setExpDate(d);
                                                                        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("fridgeList").document(bp.getBarcode())
                                                                                .update("quantity",Integer.parseInt(qtyEdit.getText().toString().trim())
                                                                                        , "expDate", bp.getExpDate()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                statusTextView.setText(String.format("Updated %s in your fridge, you may continue scanning!", bp.getName()));
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                }
                                //update views
                                dataInputLayout.setVisibility(View.VISIBLE);
                                expDateEdit.getText().clear();
                                qtyEdit.getText().clear();
                                updateButton.setEnabled(false);
                                scanThumb.setVisibility(View.VISIBLE); // TODO update with thumb
                                dataInputProgressBar.setVisibility(View.GONE);
                                statusTextView.setText(String.format("Found %s add details below or continue scanning!", bp.getName()));
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                //API returned error
                                apiProgressRL.setVisibility(View.GONE);
                                scanThumb.setVisibility(View.GONE);
                                dataInputProgressBar.setVisibility(View.VISIBLE);
                                dataInputLayout.setVisibility(View.INVISIBLE);
                                updateButton.setOnClickListener(null);
                                if(statusCode == 404){
                                    //product not found
                                    statusTextView.setText(("Could not find "+barcode.getRawValue()+", Please try again."));
                                }else{
                                    //some other error
                                    statusTextView.setText(("Unknown error occurred, please try again."));
                                }
                            }
                        });
                    }
                    break;
                default:
                    //by default reset the views
                    //couldn't classify as product
                    apiProgressRL.setVisibility(View.GONE);
                    scanThumb.setVisibility(View.GONE);
                    dataInputProgressBar.setVisibility(View.VISIBLE);
                    dataInputLayout.setVisibility(View.INVISIBLE);
                    updateButton.setOnClickListener(null);
                    statusTextView.setText(("Couldn't find " + barcode.getRawValue() + " as a product. Please try again"));
                    break;
            }
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