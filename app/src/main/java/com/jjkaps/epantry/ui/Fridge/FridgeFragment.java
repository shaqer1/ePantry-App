package com.jjkaps.epantry.ui.Fridge;

import android.app.Dialog;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.scanCode.ScanItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FridgeFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");
    private CollectionReference catalogListRef = db.collection("users").document(uid).collection("catalogList");

    private FridgeViewModel fridgeViewModel;
    private RecyclerView rvFridgeList;
    private RecyclerView.Adapter rvAdapter;
    private RecyclerView.LayoutManager rvLayoutManager;
    private ImageButton addItemBtn;
    private Button incItemBtn;
    private Button decItemBtn;
    private String notes;
    private Dialog fridgeDialog;
    private String item;
    private String quantity;
   // private TextView txtNullList;

    private static final String TAG = "FridgeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fridgeViewModel = new ViewModelProvider(this).get(FridgeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_fridge, container, false);
        fridgeDialog = new Dialog(root.getContext());
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_fridge);
        }

        final ArrayList<FridgeItem> readinFridgeList = new ArrayList<>();
        addItemBtn = root.findViewById(R.id.ibt_add);
        // txtNullList = root.findViewById(R.id.txt_nullList);

        //add item to fridge list manually
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), addItemBtn);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_addfridge, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.addManually:
                                try {
                                    addItem();
                                } catch (Exception e) {
                                    Log.d(TAG, "This exception occurs first time opens popup menu.");
                                }
                                return true;
                            case R.id.scanItem:
                                Intent i = new Intent(root.getContext(), ScanItem.class);
                                startActivity(i);
                                Log.d(TAG,"scan Item");
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });


        // retrieve fridgeList from Firebase and format
        fridgeListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> itemName = new ArrayList<>();
                ArrayList<Boolean> itemNotes = new ArrayList<>();

                rvFridgeList = root.findViewById(R.id.recyclerListFridgeList);

                // Update check status
                if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        item = String.valueOf(document.get("name"));
                        quantity = String.valueOf(document.get("quantity"));

                        // todo: sprint 2 fix display of notes
                        Object checkNullNotes = document.get("notes");
                        if (checkNullNotes != null) {
                            notes = checkNullNotes.toString();
                        } else {
                            notes = "";
                        }


                        readinFridgeList.add(new FridgeItem(item, quantity, notes));
                    }
                    rvLayoutManager = new LinearLayoutManager(getActivity());
                    rvAdapter = new ItemAdapter(readinFridgeList);

                    rvFridgeList.setLayoutManager(rvLayoutManager);
                    rvFridgeList.setAdapter(rvAdapter);

                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }

            }
        });

        /*
        // increment item
        incItemBtn = root.findViewById(R.id.btn_inc);
        incItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                rvAdapter.notifyDataSetChanged();
            }
        });


        // decrement item
        decItemBtn = root.findViewById(R.id.btn_dec);
        decItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                rvAdapter.notifyDataSetChanged();
            }
        });
        */



        // example
        //rvFridgeList = root.findViewById(R.id.recyclerListFridgeList);
        //rvLayoutManager = new LinearLayoutManager(getActivity());
        //rvAdapter = new ItemAdapter(exampleFridgeList);

        //rvFridgeList.setLayoutManager(rvLayoutManager);
        //irvFridgeList.setAdapter(rvAdapter);

        return root;
    }

  public void addItem(){
        TextView txtClose;
        Button btDone;
        final EditText addedItem;
        final EditText addedQuantity;
        fridgeDialog.setContentView(R.layout.popup_addfridge);

        txtClose = fridgeDialog.findViewById(R.id.txt_close);
        btDone = fridgeDialog.findViewById(R.id.bt_done);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               fridgeDialog.dismiss();
            }
        });
        addedItem = fridgeDialog.findViewById(R.id.inputItem);
        addedQuantity = fridgeDialog.findViewById(R.id.inputQuantity);
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get item
                item = addedItem.getText().toString();
                quantity = addedQuantity.getText().toString();

                // default quantity 1
                // todo: check for quantity < 1 and force user to enter valid quantity
                Pattern containsNum = Pattern.compile("^[0-9+]$");
                Matcher isNum = containsNum.matcher(quantity);
                if ((quantity.equals("")) || !isNum.find() ||
                        (  (Integer.parseInt(quantity) <= 0))) {
                    quantity = "1";
                }

                 //check if item exist
                 fridgeListRef.whereEqualTo("name", item)
                         .get()
                         .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                             @Override
                             public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                 if (task.isSuccessful()) {
                                    if (task.getResult() != null && task.getResult().size()!=0){
                                         for (QueryDocumentSnapshot document : task.getResult()) {
                                             Toast.makeText(getContext(), item+" Exists!", Toast.LENGTH_SHORT).show();
                                         }
                                        addedItem.setText(null);
                                     }
                                     //if not exist then add
                                     else {
                                         //check if item is null
                                         if (item.length() == 0) {
                                            Toast.makeText(getContext(), "Item can't be null!", Toast.LENGTH_SHORT).show();
                                        }
                                         //add non-null item
                                         if (item.length() != 0 ){
                                             Map<String, Object> fridgeListMap = new HashMap<>();
                                             fridgeListMap.put("name", item);
                                             fridgeListMap.put("quantity", quantity);
                                             fridgeListRef.add(fridgeListMap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                         @Override
                                                         public void onSuccess(DocumentReference documentReference) {
                                                             Log.d(TAG, "onSuccess: "+item+" added.");
                                                             Toast.makeText(getContext(), item+" added to fridge", Toast.LENGTH_SHORT).show();
                                                             addedItem.setText(null);
                                                             addedQuantity.setText(null);
                                                         }
                                                     })
                                                     .addOnFailureListener(new OnFailureListener() {
                                                         @Override
                                                         public void onFailure(@NonNull Exception e) {
                                                             Log.d(TAG, "onFailure: ",e);
                                                         }
                                                     });

                                             //ADD TO CATALOG AS WELL
                                             Map<String, Object> catalogListMap = new HashMap<>();
                                             catalogListMap.put("name", item);
                                             catalogListRef.add(catalogListMap)
                                                     .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                         @Override
                                                         public void onSuccess(DocumentReference documentReference) {
                                                             Log.d(TAG, "onSuccess: "+item+" added.");
                                                             //Toast.makeText(getContext(), item+" added to catalog", Toast.LENGTH_SHORT).show();
                                                             addedItem.setText(null);
                                                             addedQuantity.setText(null);
                                                         }
                                                     })
                                                     .addOnFailureListener(new OnFailureListener() {
                                                         @Override
                                                         public void onFailure(@NonNull Exception e) {
                                                             Log.d(TAG, "onFailure: ",e);
                                                         }
                                                     });
                                             //txtNullList.setVisibility(View.INVISIBLE);
                                             //TODO: REFRESH PAGE TO LOAD ADDED ITEMS
                                         }
                                     }
                                 }
                             }
                         });
             }
       });
         fridgeDialog.show();
    }
}