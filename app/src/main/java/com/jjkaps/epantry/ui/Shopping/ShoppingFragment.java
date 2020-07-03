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
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.utils.CustomSorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShoppingFragment extends Fragment {
    private static final String TAG = "ShoppingFragment";

    private ImageButton imgBtAdd;
    private ImageButton imgBtRemove;
    private Button sort;
    private Dialog myDialog;
    private TextView txtNullList;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference shopListRef;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ArrayList<ShoppingListItem> sl;
    private ShoppingItemAdapter arrayAdapter;
    private ListView listView_shopItem;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_shopping, container, false);
        myDialog = new Dialog(root.getContext());
        if(getActivity() != null && ((MainActivity)getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_shopping);
        }

        listView_shopItem= root.findViewById(R.id.listView_shopList);
        imgBtAdd = root.findViewById(R.id.ibt_add);
        imgBtRemove = root.findViewById(R.id.ibt_remove);
        txtNullList = root.findViewById(R.id.txt_nullList);

        //Firebase
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user != null) {
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
        }

        arrayAdapter = new ShoppingItemAdapter(root.getContext(),new ArrayList<ShoppingListItem>());
        listView_shopItem.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        getListItems();

        //Add menu
        imgBtAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), imgBtAdd);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_add, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.addManually:
                                try {
                                    showAddPopup();
                                } catch (Exception e) {
                                    Log.d(TAG, "This exception occurs first time opens popup menu.");// DEBUG
                                }
                                return true;
                            case R.id.addFav:
                                Log.d(TAG,"add Fav");//TODO
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        //Remove menu
        imgBtRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked!");
                PopupMenu popup = new PopupMenu(getContext(), imgBtRemove);
                popup.getMenuInflater().inflate(R.menu.popup_menu_remove, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        boolean clicked = false;
                        switch (menuItem.getItemId()) {
                            case R.id.item_removeAll:
                                removeAll();
                                clicked = true;
                                break;
                            case R.id.item_removeChecked:
                                removedChecked();
                                clicked = true;
                                break;
                        }
                        return clicked;
                    }
                });
                popup.show();
            }
        });

        sort = root.findViewById(R.id.sort);

        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.d(TAG, "onClick: Clicked!");
                PopupMenu popupMenu = new PopupMenu(getContext(), sort);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_shoppingsort, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.sortAlpha:
                                txtNullList.setVisibility(View.INVISIBLE);
                                arrayAdapter.setSortMethod("Alpha");
                                arrayAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.sortQuantity:
                                txtNullList.setVisibility(View.INVISIBLE);
                                arrayAdapter.setSortMethod("Qty");
                                arrayAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.sortManual:
                                txtNullList.setVisibility(View.INVISIBLE);
                                arrayAdapter.setSortMethod("None");
                                arrayAdapter.notifyDataSetChanged();
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        return root;
    }

    private void getListItems() {
        if (user != null){
            //Retrieve ShoppingList
            shopListRef.addSnapshotListener(new EventListener<QuerySnapshot>() {//runs in background and waits for updates
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    txtNullList.setVisibility(View.INVISIBLE);
                    arrayAdapter.clear();
                    arrayAdapter.notifyDataSetChanged();
                    sl = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ShoppingListItem item = document.toObject(ShoppingListItem.class);
                            item.setDocID(document.getId());
                            sl.add(item);
                            arrayAdapter.add(item);
                            arrayAdapter.runSorter();
                            arrayAdapter.notifyDataSetChanged();
                        }
                        if(queryDocumentSnapshots.size() == 0){
                            txtNullList.setVisibility(View.VISIBLE);
                        }
                    } else {
                        txtNullList.setVisibility(View.VISIBLE);
                    }
                    listView_shopItem.invalidateViews();
                }
            });
        }
    }

    private void removeAll() {
        ArrayList<ShoppingListItem> arr = sl;
        for (int i = arr.size()-1; i >=0; i--){
            final ShoppingListItem s = arr.get(i);
            shopListRef.document(s.getDocID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    arrayAdapter.remove(s);
                    txtNullList.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "couldn't delete "+ s.getDocID());
                }
            });
        }
    }

    private void removedChecked() {
        ArrayList<ShoppingListItem> arr = sl;
        for (int i = arr.size()-1; i >=0; i--){
            final ShoppingListItem s = arr.get(i);
            if(s.isChecked()){
                shopListRef.document(s.getDocID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        arrayAdapter.remove(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "couldn't delete "+ s.getDocID());
                    }
                });
            }
        }
    }

    private void showAddPopup() {
        myDialog.setContentView(R.layout.popup_add);
        TextView txtClose =  myDialog.findViewById(R.id.txt_close);
        Button btDone =  myDialog.findViewById(R.id.bt_done);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        Button cancel = myDialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });


        final EditText inputItem = myDialog.findViewById(R.id.inputItem);
        final EditText inputQtyItem = myDialog.findViewById(R.id.inputQuantityItem);
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get item
                final String item = inputItem.getText().toString();
                if (item.isEmpty()){
                    inputItem.setError("Can't leave name blank!");
                    return;
                }
                if (inputQtyItem.getText().toString().isEmpty()){
                    inputQtyItem.setError("Can't leave blank!");
                    return;
                }
                final int qty = Integer.parseInt(inputQtyItem.getText().toString());

                //check if item exist
                shopListRef.whereEqualTo("name", item).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().size()!=0){
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Toast.makeText(getContext(), item+" Exists!", Toast.LENGTH_SHORT).show();
                                }
                                inputItem.setText(null);
                            }
                            //if not exist then add
                            else {
                                //check if item is null
//                                if (item.length() == 0) {
//                                    Toast.makeText(getContext(), "Item can't be null!", Toast.LENGTH_SHORT).show();
//                                } else if (inputQtyItem.getText().toString().length() == 0) {
//                                    Toast.makeText(getContext(), "Item quantity can't be null!", Toast.LENGTH_SHORT).show();
//                                }
                                //add non-null item
//                                if (item.length() != 0){
                                    Map<String, Object> shoppingListMap = new HashMap<>();
                                    shoppingListMap.put("name", item);
                                    shoppingListMap.put("quantity", qty);
                                    shoppingListMap.put("checked", false);
                                    shopListRef.add(shoppingListMap)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d(TAG, "onSuccess: "+item+" added.");
                                                    Toast.makeText(getContext(), item+" Added", Toast.LENGTH_SHORT).show();
                                                    inputItem.setText(null);
                                                    inputQtyItem.setText(null);
                                                    //getListItems();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: ",e);
                                                }
                                            });
                                    txtNullList.setVisibility(View.INVISIBLE);
                               // }
                            }
                        }
                    }
                });
            }
        });
        myDialog.show();
    }

}