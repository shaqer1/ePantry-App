package com.jjkaps.epantry.ui.Shopping;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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

public class SuggItemAdapter extends ArrayAdapter<SuggItem> {
    private FirebaseAuth mAuth;
    private CollectionReference shopListRef;

    private Context context;

    public SuggItemAdapter(Context context, ArrayList<SuggItem> items) {
        super(context, 0, items);
        this.context = context;
    }




    private static class ViewHolder {
        CheckBox suggItemTV;
        EditText suggItemQtyET;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final SuggItem SuggItem = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shopping_list_sugg_item, parent, false);
            viewHolder.suggItemTV = convertView.findViewById(R.id.checkedTV_addSugg);
            viewHolder.suggItemQtyET = convertView.findViewById(R.id.shopping_suggItem_quantity);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (SuggItem != null && user != null) {
            //initialize the sugg list
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
            viewHolder.suggItemTV.setText(SuggItem.getBarcodeProduct().getName());
            viewHolder.suggItemTV.setChecked(SuggItem.isChecked());
            viewHolder.suggItemQtyET.setText(String.valueOf(SuggItem.getQuantity()));
            //set enable to false if this item already exists in the shopping list.
            shopListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.get("name") != null && document.get("name").toString().toLowerCase().equals(SuggItem.getBarcodeProduct().getName().toLowerCase())) {
                                viewHolder.suggItemTV.setEnabled(false);
                                viewHolder.suggItemQtyET.setEnabled(false);
                            }
                        }
                    }
                }
            });

            //toast message to notify the user if this item already existed in SL, otherwise update checked.
            viewHolder.suggItemTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SuggItem.setChecked(viewHolder.suggItemTV.isChecked());
                }
            });

            viewHolder.suggItemQtyET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        int qty = Integer.parseInt(viewHolder.suggItemQtyET.getText().toString());
                        if (qty > 0 && qty < 100) {
                            SuggItem.setQuantity(qty);
                        } else
                            viewHolder.suggItemQtyET.setError("Invalid Quantity!");
                    }
                    return false;
                }
            });
        }


        return convertView;
    }



}
