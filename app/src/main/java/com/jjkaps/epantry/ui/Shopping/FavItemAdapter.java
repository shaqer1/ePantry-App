package com.jjkaps.epantry.ui.Shopping;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static android.content.ContentValues.TAG;

public class FavItemAdapter extends ArrayAdapter<FavItem> {
    private FirebaseAuth mAuth;
    private CollectionReference shopListRef;
    private ArrayList<FavItem> favItems;
    private Comparator<FavItem> sortFavList;
    private Context context;

    public FavItemAdapter(Context context, ArrayList<FavItem> items) {
        super(context, 0, items);
        this.context = context;
        this.favItems = items;
    }




    private static class ViewHolder {
        CheckBox favItemTV;
        EditText favItemQtyET;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FavItem favItem = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shopping_list_fav_item, parent, false);
            viewHolder.favItemTV = convertView.findViewById(R.id.checkedTV_addFav);
            viewHolder.favItemQtyET = convertView.findViewById(R.id.shopping_favItem_quantity);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (favItem != null && user != null) {
            //initialize the fav list
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
            viewHolder.favItemTV.setText(favItem.getBarcodeProduct().getName());
            viewHolder.favItemTV.setChecked(favItem.isChecked());
            viewHolder.favItemQtyET.setText(String.valueOf(favItem.getQuantity()));
            //set enable to false if this item already exists in the shopping list.
            shopListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.get("name").toString().toLowerCase().equals(favItem.getBarcodeProduct().getName().toLowerCase())) {
                                viewHolder.favItemTV.setEnabled(false);
                                viewHolder.favItemQtyET.setEnabled(false);
                            }
                        }
                    }
                }
            });

            //toast message to notify the user if this item already existed in SL, otherwise update checked.
            viewHolder.favItemTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        favItem.setChecked(viewHolder.favItemTV.isChecked());
                }
            });

            viewHolder.favItemQtyET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        int qty = Integer.parseInt(viewHolder.favItemQtyET.getText().toString());
                        if (qty > 0 && qty < 100) {
                            favItem.setQuantity(qty);
                        } else
                            viewHolder.favItemQtyET.setError("Invalid Quantity!");
                    }
                    return false;
                }
            });
        }


        return convertView;
    }



}
