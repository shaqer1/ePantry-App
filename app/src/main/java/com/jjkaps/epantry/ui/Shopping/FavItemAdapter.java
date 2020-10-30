package com.jjkaps.epantry.ui.Shopping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.utils.Utils;

import java.util.ArrayList;

public class FavItemAdapter extends ArrayAdapter<FavItem> {
    private CollectionReference shopListRef;
    private ArrayList<FavItem> FavItems;
    private Context context;

    public FavItemAdapter(Context context, ArrayList<FavItem> items) {
        super(context, 0, items);
        this.context = context;
        this.FavItems = items;
    }

    private static class ViewHolder {
        CheckBox favItemTV;
        EditText favItemQtyET;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FavItem FavItem = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shopping_list_fav_item, parent, false);
            viewHolder.favItemTV = convertView.findViewById(R.id.checkedTV_addFav);
            viewHolder.favItemQtyET = convertView.findViewById(R.id.shopping_favItem_quantity);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (FavItem != null && user != null) {
            //initialize the fav list
            shopListRef = Utils.getShoppingListRef(user);
            viewHolder.favItemTV.setText(FavItem.getBarcodeProduct().getName());
            viewHolder.favItemTV.setChecked(FavItem.isChecked());
            viewHolder.favItemQtyET.setText(String.valueOf(FavItem.getQuantity()));
            //set enable to false if this item already exists in the shopping list.
            shopListRef.whereEqualTo("docReference", FavItem.getDocReference()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().size()>0) {
                    viewHolder.favItemTV.setEnabled(false);
                    viewHolder.favItemQtyET.setEnabled(false);
                }
            });

            //if this item already existed in SL, otherwise update checked.
            viewHolder.favItemTV.setOnClickListener(view -> FavItem.setChecked(viewHolder.favItemTV.isChecked()));

            viewHolder.favItemQtyET.setOnEditorActionListener((textView, actionId, keyEvent) -> {//TODO kinda useless remove condition testing completely and test, (restricted by keyboard UI already)
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    int qty = Integer.parseInt(viewHolder.favItemQtyET.getText().toString());
                    if (qty > 0 && qty < 100) {
                        FavItem.setQuantity(qty);
                    } else
                        viewHolder.favItemQtyET.setError("Invalid Quantity!");
                }
                return false;
            });
        }
        return convertView;
    }
}
