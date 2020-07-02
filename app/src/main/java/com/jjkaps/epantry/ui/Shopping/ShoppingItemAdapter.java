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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.ShoppingListItem;

import java.util.ArrayList;

public class ShoppingItemAdapter extends ArrayAdapter<ShoppingListItem> {
    private ArrayList<ShoppingListItem> items;
    private FirebaseAuth mAuth;
    private Context c;

    private static class ViewHolder {
        CheckBox itemTV;
        EditText itemQuantityET;
    }

    public ShoppingItemAdapter(Context c, ArrayList<ShoppingListItem> arr){
        super(c, 0, arr);
        this.c = c;
        this.items = arr;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Firebase
        mAuth = FirebaseAuth.getInstance();
        //Firebase
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Get the data item for this position
        final ShoppingListItem shoppingListItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shopping_list_item, parent, false);
            // Lookup view for data population
            viewHolder.itemTV = convertView.findViewById(R.id.checkedTV_shopping);
            viewHolder.itemQuantityET = convertView.findViewById(R.id.shopping_item_quantity);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.itemQuantityET.setText("");
        // Populate the data into the template view using the data object
        if (shoppingListItem != null) {
            viewHolder.itemTV.setText(shoppingListItem.getName());
            viewHolder.itemTV.setChecked(shoppingListItem.isChecked());
            viewHolder.itemQuantityET.setText(String.valueOf(shoppingListItem.getQuantity()));
            viewHolder.itemTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (firebaseUser != null){
                        db.collection("users").document(firebaseUser.getUid())
                                .collection("shoppingList").document(shoppingListItem.getDocID())
                                .update("checked", viewHolder.itemTV.isChecked());
                    }
                }
            });
            viewHolder.itemQuantityET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if(actionId == EditorInfo.IME_ACTION_DONE) {
                        if(viewHolder.itemQuantityET.getText().toString().length() > 0
                                && !viewHolder.itemQuantityET.getText().toString().equals(""+shoppingListItem.getQuantity())
                                && firebaseUser != null) {
                            int qty = Integer.parseInt(viewHolder.itemQuantityET.getText().toString());
                            db.collection("users").document(firebaseUser.getUid())
                                    .collection("shoppingList").document(shoppingListItem.getDocID())
                                    .update("quantity", qty);
                        }
                    }
                    return false; // so that default actions are also done i.e. dismiss keyboard
                }
            });
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
