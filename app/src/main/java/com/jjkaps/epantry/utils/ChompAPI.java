package com.jjkaps.epantry.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class ChompAPI {

    public static final String API_KEY = "gH7tS2KLq1HATJ";
    public static final String URL = "https://chompthis.com/api/v2/food/branded/barcode.php";
    private static FirebaseAuth mAuth;
    private static String TAG = "API";

    public static RequestHandle addProduct(String rawBarcode) {// pass views for success
        //get json from api
        mAuth = FirebaseAuth.getInstance();
        AsyncHttpClient client = new AsyncHttpClient();
        HashMap<String, String> params = new HashMap<>();
        params.put("code", rawBarcode);
        params.put("api_key", API_KEY);
        return client.get(/*"https://chompthis.com/api/v2/food/branded/barcode.php?code=0021000615261&api_key=gH7tS2KLq1HATJ"*/URL, new RequestParams(params),
                new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //super.onSuccess(statusCode,headers,response);
                FirebaseUser u = mAuth.getCurrentUser();
                if(u != null){
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    BarcodeProduct bp = BarcodeProduct.processJSON(response);
                    db.collection("users").document(u.getUid()).collection("fridgeList").document(bp.getBarcode()).set(bp);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "chompAPI request failed " + statusCode + errorResponse.toString());
            }
        });//TODO wait and set messages
    }
}
