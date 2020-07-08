package com.jjkaps.epantry.ui.scanCode;

import android.Manifest;
import android.media.Image;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.utils.ChompAPI;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.nio.ByteBuffer;
import java.util.List;

public class ScanItem extends AppCompatActivity {

    private CameraView cameraView;
    private BarcodeDetector detector;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);
        cameraView = findViewById(R.id.camera_view_scan);
        statusTextView = findViewById(R.id.barcodeStatus);

        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        setupCamera();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

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

    private void processBarcodes(List<Barcode> barcodes) {
        for (Barcode barcode: barcodes) {
            // See API reference for complete list of supported types
            switch (barcode.getValueType()) {
                case Barcode.TYPE_PRODUCT:
                    ChompAPI.addProduct(padUAN13(barcode.getRawValue()));statusTextView.setText(barcodes.get(0).getRawValue());
                    break;
                case Barcode.TYPE_UNKNOWN:
                    statusTextView.setText(("Couldn't find " + barcode.getRawValue() + " . Please try again"));
                    break;
            }
        }

    }

    private String padUAN13(String rawValue) {
        StringBuilder rawValueBuilder = new StringBuilder(rawValue);
        while(rawValueBuilder.length() < 13){
            rawValueBuilder.insert(0, "0");
        }
        rawValue = rawValueBuilder.toString();
        return rawValue;
    }
}