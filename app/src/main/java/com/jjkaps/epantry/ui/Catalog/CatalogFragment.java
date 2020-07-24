package com.jjkaps.epantry.ui.Catalog;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.ui.Fridge.ItemAdapter;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CatalogFragment extends Fragment implements ItemAdapter.ItemClickListener{

    private CatalogViewModel catalogViewModel;
    private static final String TAG = "CatalogFragment";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView txt_empty;
    private SearchView searchView;
    private TextView noItemFound;
    private CatalogAdapter arrayAdapter;
    private ListView listView_catalogItem;
    private CollectionReference catalogListRef = db.collection("users").document(uid).collection("catalogList");
    private CollectionReference shopListRef = db.collection("users").document(uid).collection("shoppingList");
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");

    private Dialog myDialog;
    private Button sortBarcode;
    private ImageButton imgBtRemove;
    private Boolean sorted = false;
    Button btEdit;
    DocumentReference itemRef;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        catalogViewModel = new ViewModelProvider(this).get(CatalogViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        //final TextView textView = root.findViewById(R.id.text_catalog);
        //catalog banner
        myDialog = new Dialog(root.getContext());
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_catalog);
        }
        imgBtRemove = root.findViewById(R.id.ibt_remove);


        txt_empty = root.findViewById(R.id.txt_emptyList);
        searchView = root.findViewById(R.id.search_view);
        noItemFound = root.findViewById(R.id.txt_noItemFound);

        listView_catalogItem = root.findViewById(R.id.listView_catalogItem);

        arrayAdapter = new CatalogAdapter(root.getContext(), new ArrayList<CatalogAdapterItem>());
        listView_catalogItem.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        //get catalog items
        syncCatalogList(root);

        //ON ITEM CLICK
        listView_catalogItem.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                BarcodeProduct bp = ((CatalogAdapterItem) adapter.getItemAtPosition(position)).getBarcodeProduct();
                String itemRefPath = ((CatalogAdapterItem) adapter.getItemAtPosition(position)).getDocReference();
                if(bp != null) {
                    Intent i = new Intent(v.getContext(), ItemActivity.class);
                    i.putExtra("barcodeProduct", bp);
                    if(itemRefPath != null) {
                        i.putExtra("currCollection", "catalogList");
                        i.putExtra("docID", itemRefPath);
                    }
                    v.getContext().startActivity(i);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) { return false; }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.equals("")) {
                    Log.d(TAG, "onQueryTextChange: " );
                    noItemFound.setVisibility(View.INVISIBLE);
                    //retrieveCatalogList(root);
                    arrayAdapter.getFilter().filter(s);
                }else {
                    arrayAdapter.getFilter().filter(s);
                    if (arrayAdapter.isEmpty()){
//                    Log.d(TAG, "onQueryTextChange: "+s);
                        noItemFound.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });

        //CLEAR CATALOG
        imgBtRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked!");
                PopupMenu popup = new PopupMenu(getContext(), imgBtRemove);
                popup.getMenuInflater().inflate(R.menu.popup_menu_clearcat, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.item_removeAll) {
                            // case R.id.item_removeAll:
                            clearCatalog();
                            //Toast.makeText(getContext(), "hit remove all", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();

            }
        });

        //SORT BARCODE
        sortBarcode = root.findViewById(R.id.sortBarcode);
        sortBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                arrayAdapter.toggleScannedOnly(searchView.getQuery().toString());
                if(arrayAdapter.isScannedOnly()){
                    sortBarcode.setText("All");
                }else{
                    sortBarcode.setText("Scanned");
                }
                /*arrayAdapter.clear();
                if (sorted) {
                    catalogListRef.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    // List<String> catalogItem = new ArrayList<>();

                                    if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                                        txt_empty.setVisibility(View.INVISIBLE);
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                                            arrayAdapter.add(new CatalogAdapterItem(bp, document.getReference().getPath()));
                                        }
                                        sortBarcode.setText("Scanned");
                                        sorted = false;
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                }
                            });
                } else {
                    catalogListRef.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    // List<String> catalogItem = new ArrayList<>();

                                    if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                                        txt_empty.setVisibility(View.INVISIBLE);
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                                            // catalogItem.add(document.get("name").toString());
                                            if (document.get("barcode") != null) {
                                                arrayAdapter.add(new CatalogAdapterItem(bp, document.getReference().getPath()));
                                            }
                                        }
                                        sortBarcode.setText("All");
                                        sorted = true;
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());

                                    }
                                }
                            });
                }
                arrayAdapter.notifyDataSetChanged();*/
            }
        });
        return root;
    }

    public void syncCatalogList(final View root) {
        //retrieve from db live sync, listens for updates on whole collection no need to refresh list each time
        catalogListRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null){
                    List<CatalogAdapterItem> catalogItems = new ArrayList<>();
                    txt_empty.setVisibility(View.INVISIBLE);
                    arrayAdapter.clear();
                    arrayAdapter.notifyDataSetChanged();
                    for (QueryDocumentSnapshot document : value) {
                        BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                        catalogItems.add(new CatalogAdapterItem(bp, document.getReference().getPath()));
                    }
                    arrayAdapter.addAll(catalogItems);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });

    }
    private void clearCatalog() {
        TextView txtClose;
        Button btRemoveAll;
        myDialog.setContentView(R.layout.popup_removeall);
        txtClose =  myDialog.findViewById(R.id.txt_removeAll_close);
        btRemoveAll = myDialog.findViewById(R.id.bt_removeAllYes);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        btRemoveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            catalogListRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().size() == 0) {
                            Toast toast = Toast.makeText(getContext(), "No Items!", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            View vi = toast.getView();
                            TextView text = vi.findViewById(android.R.id.message);
                            text.setTextColor(Color.BLACK);
                            text.setTextSize(25);
                            toast.show();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                catalogListRef.document(document.getId()).delete();
                            }
                            Toast toast = Toast.makeText(getContext(), "Your catalog is now empty!", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            View vi = toast.getView();
                            TextView text = vi.findViewById(android.R.id.message);
                            text.setTextColor(Color.BLACK);
                            text.setTextSize(25);
                            toast.show();
                        }
                    }
                    }
                });
            myDialog.dismiss();
            }
        });
        myDialog.show();
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number "  + ", which is at cell position " );
        Toast.makeText(view.getContext(), "item clicked!", Toast.LENGTH_SHORT).show();
    }
}