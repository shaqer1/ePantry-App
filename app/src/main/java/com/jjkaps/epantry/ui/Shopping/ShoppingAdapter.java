package com.jjkaps.epantry.ui.Shopping;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.utils.CustomSorter;
import com.jjkaps.epantry.utils.Utils;

import java.util.ArrayList;

public class ShoppingAdapter extends ArrayAdapter<ShoppingAdapterItem> {
    private ListView listView_shopItem;
    private Context c;
    private String sortMethod;

    public void runSorter() {
        CustomSorter cs = new CustomSorter(sortMethod);
        this.sort(cs.getSorter());
    }

    public void setSortMethod(String sortMethod) {
        this.sortMethod = sortMethod;
        runSorter();
    }

    private static class ViewHolder {
        CheckBox itemTV;
        EditText itemQuantityET;
        BarcodeProduct bp;
    }

    public ShoppingAdapter(Context c, ArrayList<ShoppingAdapterItem> arr, ListView listView_shopItem){
        super(c, 0, arr);
        this.c = c;
        this.listView_shopItem = listView_shopItem;
        this.sortMethod = "None";
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //Firebase
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Get the data item for this position
        final ShoppingAdapterItem shoppingListAdaptItem = getItem(position);
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

       // viewHolder.itemQuantityET.setText("");
        // Populate the data into the template view using the data object
        if (shoppingListAdaptItem != null && firebaseUser != null) {
            ShoppingListItem shoppingListItem = shoppingListAdaptItem.getShoppingListItem();
            //CollectionReference fridgeListRef = db.collection("users").document(firebaseUser.getUid()).collection("fridgeList");
            if(Utils.isNotNullOrEmpty(shoppingListItem.getDocReference())) {
                db.document(shoppingListItem.getDocReference()).get()//DONE? reference not name
                        .addOnSuccessListener(documentSnapshot -> {
                            BarcodeProduct bp = documentSnapshot.toObject(BarcodeProduct.class);
                            if(bp != null && Utils.isNotNullOrEmpty(bp.getInventoryDetails()) && Utils.isNotNullOrEmpty(bp.getInventoryDetails().getQuantity()) && bp.getInventoryDetails().getQuantity()!=0){
                                viewHolder.itemTV.setText((shoppingListItem.getName() + " (" +  bp.getInventoryDetails().getQuantity() + " in Fridge)"));
                                viewHolder.bp = bp;
                            }else if (bp!=null){
                                viewHolder.bp = bp;
                            }
                        });
            }

            viewHolder.itemTV.setText(shoppingListItem.getName());
            viewHolder.itemTV.setChecked(shoppingListItem.isChecked());
            viewHolder.itemQuantityET.setText(String.valueOf(shoppingListItem.getQuantity()));
            if(shoppingListItem.isChecked()){// grey out items
                viewHolder.itemTV.setPaintFlags(viewHolder.itemTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );//add crossed out
                viewHolder.itemTV.setAlpha(0.7f);
            }else {
                viewHolder.itemTV.setPaintFlags(viewHolder.itemTV.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG & 0xff));//remove crossed out
                viewHolder.itemTV.setAlpha(1f);
            }
            viewHolder.itemTV.setOnClickListener(view -> db.collection("users").document(firebaseUser.getUid())
                    .collection("shoppingList").document(shoppingListAdaptItem.getDocID())
                    .update("checked", viewHolder.itemTV.isChecked()).addOnSuccessListener(aVoid -> {
                        runSorter();
                        notifyDataSetChanged();
                    }));
            viewHolder.itemTV.setOnLongClickListener(view -> {
                if(viewHolder.bp != null){
                    Intent i = new Intent(c, ItemActivity.class);
                    i.putExtra("docID", shoppingListAdaptItem.getShoppingListItem().getDocReference());
                    i.putExtra("name", shoppingListItem.getName());
                    i.putExtra("currCollection", "shoppingList");
                    i.putExtra("quantity", Integer.toString(shoppingListItem.getQuantity()));
                    i.putExtra("barcodeProduct", viewHolder.bp);
                    c.startActivity(i);
                }else{
                    Utils.createStatusMessage(Snackbar.LENGTH_LONG, ShoppingAdapter.this.listView_shopItem, "Could not load item details, Please try again", Utils.StatusCodes.FAILURE);
                }


                //i.putExtra("notes", shoppingListItem.getNotes());

                return true;
            });
            viewHolder.itemQuantityET.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    if(viewHolder.itemQuantityET.getText().toString().length() > 0 && !viewHolder.itemQuantityET.getText().toString().equals("" + shoppingListItem.getQuantity())) {
                        int qty = Integer.parseInt(viewHolder.itemQuantityET.getText().toString());
                        db.collection("users").document(firebaseUser.getUid())
                                .collection("shoppingList").document(shoppingListAdaptItem.getDocID())
                                .update("quantity", qty);
                    }
                }
                return false; // so that default actions are also done i.e. dismiss keyboard
            });
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
