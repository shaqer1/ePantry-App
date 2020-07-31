package com.jjkaps.epantry.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

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
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, typ);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

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

    public static void createToast(Context context, String msg, int length) {
        createToast(context, msg, length, Gravity.CENTER_VERTICAL, Color.LTGRAY);
    }
}
