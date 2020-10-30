package com.jjkaps.epantry.ui.Recipes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.utils.Utils;

import java.util.Objects;

public class RecipesFragment extends Fragment {

    private static final String TAG = "CatalogFragment";
    private TextView txt_empty;
    private SearchView searchView;
    private RecipeAdapter arrayAdapter;
    private ListView listView_catalogItem;
    private CollectionReference catalogListRef;
    private Dialog myDialog;
    private ImageButton ib, ibSort;
    private View root;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_recipe, container, false);
        //catalog popup
        //myDialog = new Dialog(root.getContext());

        //Firebase
        /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!= null){
            catalogListRef = Utils.getCatalogListRef(user);
        }*/

        Utils.hideKeyboard(root.getContext());

        txt_empty = root.findViewById(R.id.txt_emptyList);
        /*searchView = root.findViewById(R.id.search_view);

        listView_catalogItem = root.findViewById(R.id.listView_catalogItem);
        arrayAdapter = new CatalogAdapter(root.getContext(), new ArrayList<>());
        listView_catalogItem.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        //get catalog items
        syncCatalogList(root);*/

        //ON ITEM CLICK
        /*listView_catalogItem.setOnItemClickListener((adapter, v, position, arg3) -> {
            BarcodeProduct bp = ((BPAdapterItem) adapter.getItemAtPosition(position)).getBarcodeProduct();
            String itemRefPath = ((BPAdapterItem) adapter.getItemAtPosition(position)).getDocReference();
            if(bp != null) {
                Intent i = new Intent(v.getContext(), ItemActivity.class);
                i.putExtra("barcodeProduct", bp);
                if(itemRefPath != null) {
                    i.putExtra("currCollection", "catalogList");
                    i.putExtra("docID", itemRefPath);
                }
                v.getContext().startActivity(i);
            }
        });*/

        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) { return false; }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.equals("")) {
                    Log.d(TAG, "onQueryTextChange: " );
                    txt_empty.setVisibility(View.INVISIBLE);
                    //retrieveCatalogList(root);
                    arrayAdapter.getFilter().filter(s);
                }else {
                    arrayAdapter.getFilter().filter(s);
                    if (arrayAdapter.isEmpty()){
//                    Log.d(TAG, "onQueryTextChange: "+s);
                        txt_empty.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });*/
        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        customActionBar(context);
    }

    private void customActionBar(Context c) {
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_recipes);
            /*ib = view.findViewById(R.id.btn_update);
            ib.setVisibility(View.VISIBLE);
            //CLEAR CATALOG
            ib.setOnClickListener(view1 -> {
                Log.d(TAG, "onClick: Clicked!");
                PopupMenu popup = new PopupMenu(getContext(), ib);
                popup.getMenuInflater().inflate(R.menu.popup_menu_clearcat, popup.getMenu());
                popup.setOnMenuItemClickListener(menuItem -> {
                    if(menuItem.getItemId() == R.id.item_removeAll) {
                        // case R.id.item_removeAll:
                        clearCatalog();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
            //SORT BARCODE
            ibSort = view.findViewById(R.id.secondaryUpdate);
            ibSort.setVisibility(View.VISIBLE);
            ibSort.setOnClickListener(view1 -> {
                Log.d(TAG, "filtering view");
                PopupMenu popupMenu = new PopupMenu(getContext(), ibSort);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_catalogsort, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.viewFav:
                            Log.d(TAG, "favorites");
                            arrayAdapter.filterView(searchView.getQuery().toString(), FAVORITES);
                            return true;
                        case R.id.viewScanned:
                            Log.d(TAG, "scanned");
                            arrayAdapter.filterView(searchView.getQuery().toString(), SCANNED);
                            return true;
                        case R.id.viewAll:
                            Log.d(TAG, "all");

                            arrayAdapter.filterView(searchView.getQuery().toString(), NOTHING);
                            return true;
                    }
                    return false;
                });
                popupMenu.show();
            });*/
        }
    }

    public void syncCatalogList(final View root) {
        //retrieve from db live sync, listens for updates on whole collection no need to refresh list each time
        /*catalogListRef.addSnapshotListener((value, error) -> {
            if(value != null){
                List<BPAdapterItem> catalogItems = new ArrayList<>();
                txt_empty.setVisibility(View.INVISIBLE);
                arrayAdapter.clear();
                arrayAdapter.notifyDataSetChanged();
                for (QueryDocumentSnapshot document : value) {
                    BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                    catalogItems.add(new BPAdapterItem(bp, document.getReference().getPath()));
                }
                arrayAdapter.addAll(catalogItems);
                arrayAdapter.notifyDataSetChanged();
            }
        });*/

    }
    /*private void clearCatalog() {
        TextView txtClose;
        Button btRemoveAll;
        myDialog.setContentView(R.layout.popup_removeall);
        txtClose =  myDialog.findViewById(R.id.txt_removeAll_close);
        btRemoveAll = myDialog.findViewById(R.id.bt_removeAllYes);
        txtClose.setOnClickListener(v -> myDialog.dismiss());
        btRemoveAll.setOnClickListener(view -> {
            catalogListRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().size() == 0) {
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, root, "No Items!", Utils.StatusCodes.MESSAGE);
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            catalogListRef.document(document.getId()).delete();
                        }
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, root, "Your catalog is now empty!", Utils.StatusCodes.MESSAGE);
                        txt_empty.setVisibility(View.VISIBLE);
                    }
                }
                });
            myDialog.dismiss();
        });
        myDialog.show();
    }*/

    /*@Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number "  + ", which is at cell position " );
        //Utils.createToast(view.getContext(), "item clicked!", Toast.LENGTH_SHORT);
    }*/
}