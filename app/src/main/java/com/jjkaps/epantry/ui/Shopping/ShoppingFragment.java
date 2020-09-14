package com.jjkaps.epantry.ui.Shopping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.ui.Shopping.AddItem.AddFavItem;
import com.jjkaps.epantry.ui.Shopping.AddItem.AddShoppingItem;
import com.jjkaps.epantry.ui.Shopping.AddItem.AddSuggItem;
import com.jjkaps.epantry.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class ShoppingFragment extends Fragment {
    private static final String TAG = "ShoppingFragment";
    private static final int SHOW_NO_ITEMS_TAG = 2;

    private TextView txtNullList;

    private CollectionReference shopListRef;
    private FirebaseUser user;
    private ArrayList<ShoppingAdapterItem> sl;
    private ShoppingAdapter arrayAdapter;
    private ListView listView_shopItem;
    private ImageButton ib, ibSort;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_shopping, container, false);
        Utils.hideKeyboard(root.getContext());

        listView_shopItem= root.findViewById(R.id.listView_shopList);
        txtNullList = root.findViewById(R.id.txt_nullList);

        //Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user != null) {
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
        }

        arrayAdapter = new ShoppingAdapter(root.getContext(), new ArrayList<>()); // TODO make more cohesive
        listView_shopItem.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        getListItems();

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        customActionBar(context);
    }

    private void customActionBar(Context c) {
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() != null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_shopping);
            ib = view.findViewById(R.id.btn_update);
            ib.setVisibility(View.VISIBLE);
            //get menu for shopping
            ib.setOnClickListener(view2 -> {
                PopupMenu popupMenu = new PopupMenu(getContext(), ib);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_add, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    boolean clicked = false;
                    switch (menuItem.getItemId()) {
                        case R.id.addManually:
                            Intent i = new Intent(c, AddShoppingItem.class);
                            startActivityForResult(i, SHOW_NO_ITEMS_TAG);
                            //showAddPopup();
                            return true;
                        case R.id.addFav:
                            Intent addFavIntent = new Intent(c, AddFavItem.class);
                            startActivity(addFavIntent);
                            return true;
                        case R.id.addSugg:
                            Intent addSuggIntent = new Intent(c, AddSuggItem.class);
                            startActivity(addSuggIntent);
                            return true;
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
                });
                popupMenu.show();
            });
            //get menu for sort
            ibSort = view.findViewById(R.id.secondaryUpdate);
            ibSort.setVisibility(View.VISIBLE);
            ibSort.setOnClickListener(view1 -> {
                Log.d(TAG, "onClick: Clicked!");
                PopupMenu popupMenu = new PopupMenu(getContext(), ibSort);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_shoppingsort, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
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
                        case R.id.sortStorage:
                            txtNullList.setVisibility(View.INVISIBLE);
                            arrayAdapter.setSortMethod("None");
                            arrayAdapter.notifyDataSetChanged();
                            return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SHOW_NO_ITEMS_TAG){
            txtNullList.setVisibility(View.INVISIBLE);
        }
    }

    private void getListItems() {
        if (user != null){
            //Retrieve ShoppingList
            //runs in background and waits for updates
            shopListRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
                txtNullList.setVisibility(View.INVISIBLE);
                arrayAdapter.clear();
                arrayAdapter.notifyDataSetChanged();
                sl = new ArrayList<>();
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ShoppingListItem item = document.toObject(ShoppingListItem.class);
                        ShoppingAdapterItem sai = new ShoppingAdapterItem(document.getId(), item);
                        sl.add(sai);
                        arrayAdapter.add(sai);
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
            });
        }
    }

    private void removeAll() {
        ArrayList<ShoppingAdapterItem> arr = sl;
        for (int i = arr.size()-1; i >=0; i--){
            final ShoppingAdapterItem s = arr.get(i);
            shopListRef.document(s.getDocID()).delete().addOnSuccessListener(aVoid -> {
                arrayAdapter.remove(s);
                txtNullList.setVisibility(View.VISIBLE);
            }).addOnFailureListener(e -> Log.d(TAG, "couldn't delete "+ s.getDocID()));
        }
    }

    private void removedChecked() {
        ArrayList<ShoppingAdapterItem> arr = sl;
        for (int i = arr.size()-1; i >=0; i--){
            final ShoppingAdapterItem s = arr.get(i);
            if(s.getShoppingListItem().isChecked()){
                shopListRef.document(s.getDocID()).delete().addOnSuccessListener(aVoid -> arrayAdapter.remove(s))
                        .addOnFailureListener(e -> Log.d(TAG, "couldn't delete "+ s.getDocID()));
            }
        }
    }

}