package com.jjkaps.epantry.models.snackbar;

import android.view.View;

public class SnackBarAction {
    private String name;
    private View.OnClickListener listener;

    public SnackBarAction(String name, View.OnClickListener listener) {
        this.name = name;
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public View.OnClickListener getListener() {
        return listener;
    }
}
