package com.jjkaps.epantry.utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.HashMap;

public class ChompAPI {

    public static final String API_KEY = "gH7tS2KLq1HATJ";
    public static final String URL = "https://chompthis.com/api/v2/food/branded/barcode.php";

    public static void addProduct(String rawBarcode, JsonHttpResponseHandler responseHandler) {// pass views for success
        //get json from api
        AsyncHttpClient client = new AsyncHttpClient();
        HashMap<String, String> params = new HashMap<>();
        params.put("code", rawBarcode);
        params.put("api_key", API_KEY);
        client.get(URL, new RequestParams(params), responseHandler);
    }
}
