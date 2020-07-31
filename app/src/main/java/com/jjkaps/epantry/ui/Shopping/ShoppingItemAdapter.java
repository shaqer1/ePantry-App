package com.jjkaps.epantry.ui.Shopping;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.utils.CustomSorter;

import java.util.ArrayList;

public class ShoppingItemAdapter extends ArrayAdapter<ShoppingListItem> {
    private FirebaseAuth mAuth;
    private Context c;
    private String sortMethod;

    public void runSorter() {
        CustomSorter cs = new CustomSorter(sortMethod);
        this.sort(cs.getSorter());
    }

    public String getSortMethod() {
        return sortMethod;
    }

    public void setSortMethod(String sortMethod) {
        this.sortMethod = sortMethod;
        runSorter();
    }

    private static class ViewHolder {
        CheckBox itemTV;
        EditText itemQuantityET;
    }

    public ShoppingItemAdapter(Context c, ArrayList<ShoppingListItem> arr){
        super(c, 0, arr);
        this.c = c;
        this.sortMethod = "None";
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
        if (shoppingListItem != null && firebaseUser != null) {
            CollectionReference fridgeListRef = db.collection("users").document(firebaseUser.getUid()).collection("fridgeList");
            fridgeListRef.whereEqualTo("name", shoppingListItem.getName())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String fridgeQuantity = document.get("quantity")+"";
                                            viewHolder.itemTV.setText((shoppingListItem.getName() + " (" +  fridgeQuantity + " in Fridge)"));
                                        }
                                    }
                                }
                            });
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
            viewHolder.itemTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.collection("users").document(firebaseUser.getUid())
                            .collection("shoppingList").document(shoppingListItem.getDocID())
                            .update("checked", viewHolder.itemTV.isChecked()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            runSorter();
                            notifyDataSetChanged();
                        }
                    });
                }
            });
            viewHolder.itemTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //BarcodeProduct bp = new BarcodeProduct();
                    //bp.setName(shoppingListItem.getName());
                    //bp.setQuantity(shoppingListItem.getQuantity());
                    Context c = viewHolder.itemTV.getContext();
                    String shoppingListDocID = db.collection("users").document(firebaseUser.getUid())
                            .collection("shoppingList").document(shoppingListItem.getDocID()).getPath();
                    if(shoppingListDocID != null) {
                        Intent i = new Intent(c, EditShoppingItem.class);
                        i.putExtra("docID", shoppingListItem.getDocID());
                        i.putExtra("name", shoppingListItem.getName());
                        i.putExtra("quantity", Integer.toString(shoppingListItem.getQuantity()));
                        i.putExtra("notes", shoppingListItem.getNotes());
                        c.startActivity(i);
                    }
                    // update document
                    /*
                    db.collection("users").document(firebaseUser.getUid())
                            .collection("shoppingList").document(shoppingListItem.getDocID())
                            .update("checked", viewHolder.itemTV.isChecked()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            runSorter();
                            notifyDataSetChanged();
                        }
                    });*/

                    return true;
                }
            });
            viewHolder.itemQuantityET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if(actionId == EditorInfo.IME_ACTION_DONE) {
                        if(viewHolder.itemQuantityET.getText().toString().length() > 0 && !viewHolder.itemQuantityET.getText().toString().equals("" + shoppingListItem.getQuantity())) {
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
