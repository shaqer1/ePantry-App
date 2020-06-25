package com.jjkaps.epantry.ui.Shopping;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;

import io.opencensus.metrics.LongGauge;

public class ShoppingFragment extends Fragment {
    private static final String TAG = "ShoppingFragment";
    private String item;

     ShoppingViewModel shoppingViewModel;
     Button btClearAll;
     ImageButton imgBtAdd;
     Dialog myDialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myDialog = new Dialog(this.getContext());
        shoppingViewModel = new ViewModelProvider(this).get(ShoppingViewModel.class);
        View root = inflater.inflate(R.layout.fragment_shopping, container, false);
        btClearAll = root.findViewById(R.id.bt_clearAll);
        imgBtAdd = root.findViewById(R.id.ibt_add);


        imgBtAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(getActivity().getApplicationContext(), imgBtAdd);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.addManually:
                                ShowAddPopup(view);
                                return true;
                            case R.id.addFav:
                                Log.d(TAG,"add Fav");
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });


//        final TextView textView = root.findViewById(R.id.text_shopping);
//        shoppingViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }
    public void ShowAddPopup(View v) {
        TextView txtClose;
        Button btDone;
        final EditText inputItem;
        myDialog.setContentView(R.layout.popup_add);
        txtClose =  myDialog.findViewById(R.id.txt_close);
        btDone =  myDialog.findViewById(R.id.bt_done);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        inputItem = myDialog.findViewById(R.id.inputItem);
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item = inputItem.getText().toString();

                Log.d(TAG,item);
            }
        });
        myDialog.show();
    }
}