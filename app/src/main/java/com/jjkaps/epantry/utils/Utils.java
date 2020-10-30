package com.jjkaps.epantry.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.snackbar.SnackBarAction;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static boolean isNotNullOrEmpty(Object o){
        return o != null && !o.equals("null") && !o.equals("");
    }

    //hide keyboard
    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void addAnalytic(String id, String name, String typ, Context c){
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(c);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name.replaceAll(" ","_"));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, typ);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /*//_------------------------DEBUG CRASH ----------------------------------------------
            Button crashButton = view.findViewById(R.id.txt_close);
            crashButton.setVisibility(View.VISIBLE);
            crashButton.setOnClickListener(view12 -> {
                throw new RuntimeException("Test Crash"); // Force a crash
            });
            //-------------------------DEBUG CRASH -----------------------------------------*/

    public static void createToast(Context context, String msg, int length, int position, int color) {
        Toast toast = Toast.makeText(context, msg, length);
        toast.setGravity(position, 0, 0);
        View vi = toast.getView();
        if (vi != null) {
            vi.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            TextView text = vi.findViewById(android.R.id.message);
            text.setTextColor(Color.BLACK);
            text.setTextSize(25);
            toast.show();
        }
    }

    public static SimpleDateFormat getExpDateFormat(){
        return new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    }

    public static String toSentCase(String s){
        if (s.isEmpty()) {return "";}
        if (s.length() == 1) {return s.toUpperCase();}
        StringBuilder res = new StringBuilder();
        for (String str : s.split(" ")) {
            if(str.length() > 1){
                res.append(str.substring(0, 1).toUpperCase()).append(str.substring(1).toLowerCase()).append(" ");
            }else{
                res.append(str).append(" ");
            }
        }
        return res.toString().trim();
    }

    @Nullable
    public static CollectionReference getCatalogListRef() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? db.collection("users").document(user.getUid()).collection("catalogList"):null;
    }

    @Nullable
    public static CollectionReference getFridgeListRef() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? db.collection("users").document(user.getUid()).collection("fridgeList"):null;
    }

    @Nullable
    public static CollectionReference getShoppingListRef() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? db.collection("users").document(user.getUid()).collection("shoppingList"):null;
    }

    public static CollectionReference getCatalogListRef(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("users").document(user.getUid()).collection("catalogList");
    }

    public static CollectionReference getFridgeListRef(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("users").document(user.getUid()).collection("fridgeList");
    }

    public static CollectionReference getShoppingListRef(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("users").document(user.getUid()).collection("shoppingList");
    }

    public static String getDocId(String docRef) {
        return docRef.substring(docRef.lastIndexOf("/")+1);
    }

    public enum StatusCodes {
        SUCCESS, FAILURE, MESSAGE, INFO
    }
    public static void createStatusMessage(View parentView, String msg){
        createStatusMessage(Snackbar.LENGTH_SHORT, parentView, msg, StatusCodes.MESSAGE, new ArrayList<>());
    }
    public static void createStatusMessage(int length, View parentView, String msg){
        createStatusMessage(length, parentView, msg, StatusCodes.MESSAGE, new ArrayList<>());
    }
    public static void createStatusMessage(int length, View parentView, String msg, StatusCodes status) {
        createStatusMessage(length, parentView, msg, status, new ArrayList<>());

    }

    private static void createStatusMessage(int length, View parentView, String msg, StatusCodes status, ArrayList<SnackBarAction> actions) {
        int textColor = Color.WHITE, bgColor;
        switch (status){
            case INFO:
                bgColor = Color.BLUE;
                break;
            case FAILURE:
                bgColor = Color.RED;
                break;
            case SUCCESS:
                bgColor = Color.parseColor("#027548");
                break;
            case MESSAGE:
            default:
                bgColor = ResourcesCompat.getColor(parentView.getResources(), R.color.colorPrimary, null);
        }
        Snackbar snackbar = Snackbar.make(parentView, msg, length);
        View view = snackbar.getView();
        view.setBackgroundColor(bgColor);
        snackbar.setTextColor(textColor);
        view.setPadding(0,0,0,0);
        TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        if(actions.size()==0){
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        for (SnackBarAction s : actions) {
            snackbar.setAction(s.getName(), s.getListener());
        }
        FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
        snackbar.show();
    }

    public static void createToast(Context context, String msg, int length) {
        createToast(context, msg, length, Gravity.CENTER_VERTICAL, Color.LTGRAY);
    }

    public static String getStringArr(List<String> palm_oil_ingredients) {
        StringBuilder s = new StringBuilder();
        for(String x : palm_oil_ingredients){
            s.append(x).append(", ");
        }
        return s.substring(0,s.length()-2);
    }
}
