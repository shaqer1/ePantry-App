package com.jjkaps.epantry.ui.Shopping;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.opencensus.metrics.LongGauge;

public class ShoppingFragment extends Fragment {
    private static final String TAG = "ShoppingFragment";


    private Button btClearAll;
    private ImageButton imgBtAdd;
    private Dialog myDialog;
    private TextView txtNullList;
    private String item;


    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference shopListRef = db.collection("users").document(uid).collection("shoppingList");



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myDialog = new Dialog(this.getContext());

        ShoppingViewModel shoppingViewModel = new ViewModelProvider(this).get(ShoppingViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_shopping, container, false);
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_shopping);
        }

        final ListView listView_shopItem = root.findViewById(R.id.listView_shopItem);
        btClearAll = root.findViewById(R.id.bt_clearAll);
        imgBtAdd = root.findViewById(R.id.ibt_add);
        txtNullList = root.findViewById(R.id.txt_nullList);


        //Retrieve ShoppingList
        shopListRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<String> shoppingItem = new ArrayList<>();
                        List<Boolean> itemChecked = new ArrayList<>();
                        final ListView listView_shopItem = root.findViewById(R.id.listView_shopItem);
                        //Update check status
                        if (task.isSuccessful() && task.getResult().size() != 0) {
                            txtNullList.setVisibility(View.INVISIBLE);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                shoppingItem.add(document.get("Name").toString());
                                itemChecked.add((Boolean) document.get("Checked"));
                            }
                            ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_multiple_choice,shoppingItem);
                            listView_shopItem.setAdapter(arrayAdapter);
                            listView_shopItem.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                            for(int i=0; i<arrayAdapter.getCount(); i++) {
                                listView_shopItem.setItemChecked(i,itemChecked.get(i));
                            }
                            listView_shopItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, final View view, int postition, long l) {
                                    shopListRef.whereEqualTo("Name",listView_shopItem.getItemAtPosition(postition))
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    CheckedTextView v = (CheckedTextView) view;
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                                            shopListRef.document(document.getId()).update("Checked", v.isChecked());
                                                            Log.d(TAG, document.getId() + " => " + document.get("Checked"));
                                                        }
                                                    }
                                                }
                                            });
                                }
                            });
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


        //Add menu
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

        //Remove All items
        btClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopListRef.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        shopListRef.document(document.getId()).delete();
                                    }
                                }
                            }
                        });
                txtNullList.setVisibility(View.VISIBLE);
                root.findViewById(R.id.listView_shopItem).setVisibility(View.INVISIBLE);
            }
        });

        return root;
    }

    //Manual add popup Menu
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
                //get item
                item = inputItem.getText().toString();
                //check if item exist
                shopListRef.whereEqualTo("Name", item)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().size()!=0){
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Toast.makeText(getContext(), item+" Exists!", Toast.LENGTH_SHORT).show();
                                        }
                                        inputItem.setText(null);
                                        return;
                                    }
                                    //if not exist then add
                                    else {
                                        //check if item is null
                                        if (item.length() == 0) {
                                            Toast.makeText(getContext(), "Item can't be null!", Toast.LENGTH_SHORT).show();
                                        }
                                        //add non-null item
                                        if (item.length() != 0){
                                            Map<String, Object> shoppingListMap = new HashMap<>();
                                            shoppingListMap.put("Name", item);
                                            shoppingListMap.put("Checked", false);
                                            shopListRef.add(shoppingListMap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Log.d(TAG, "onSuccess: "+item+" added.");
                                                            Toast.makeText(getContext(), item+" Added", Toast.LENGTH_SHORT).show();
                                                            inputItem.setText(null);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "onFailure: ",e);
                                                        }
                                                    });
                                            txtNullList.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                }
                            }
                        });
            }
        });
        myDialog.show();
    }
}