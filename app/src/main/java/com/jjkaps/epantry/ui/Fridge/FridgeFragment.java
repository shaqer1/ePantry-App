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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.ui.scanCode.ScanItem;
import com.jjkaps.epantry.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FridgeFragment extends Fragment {

    private static final int MANUAL_ITEM_ADDED = 2;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");
    private CollectionReference catalogListRef = db.collection("users").document(uid).collection("catalogList");

    private FridgeViewModel fridgeViewModel;
    private RecyclerView rvFridgeList;
    private ItemAdapter rvAdapter;
    private RecyclerView.LayoutManager rvLayoutManager;
    private ImageButton addItemBtn;
    private Button incItemBtn;
    private Button decItemBtn;
    private String notes;
    private Dialog fridgeDialog;
    private String item;
    private String quantity;
    private String expiration;
    private EditText addedExpiration;
    private SimpleDateFormat simpleDateFormat;
   // private TextView txtNullList;

    private static final String TAG = "FridgeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fridgeViewModel = new ViewModelProvider(this).get(FridgeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_fridge, container, false);
        fridgeDialog = new Dialog(root.getContext());
        if (getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() != null) {
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_fridge);
        }

        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
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
                                Intent intent = new Intent(root.getContext(), AddFridgeItem.class);
                                startActivityForResult(intent, MANUAL_ITEM_ADDED);
                                return true;
                            case R.id.scanItem:
                                Intent i = new Intent(root.getContext(), ScanItem.class);
                                startActivity(i);
                                Log.d(TAG, "scan Item");
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });


        rvFridgeList = root.findViewById(R.id.recyclerListFridgeList);
        rvFridgeList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        rvAdapter = new ItemAdapter(new ArrayList<FridgeItem>());
        rvFridgeList.setAdapter(rvAdapter);
        // retrieve fridgeList from Firebase live NOTE:add snapshot listener listens for live changes and format
        // no need to refresh list each time
        fridgeListRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documents, @Nullable FirebaseFirestoreException error) {
                if(documents != null){
                    rvAdapter.clear();
                    ArrayList<FridgeItem> readinFridgeList = new ArrayList<>();
                    rvAdapter.notifyDataSetChanged();
                    for (QueryDocumentSnapshot document : documents){
                        BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                        item = String.valueOf(bp.getName());
                        //append the expiration date to the name if expDate exists.
                        StringBuilder sb = new StringBuilder();
                        if (!Utils.isNotNullOrEmpty(bp.getExpDate())) {
                            Log.d(TAG, "expDate length == 0"+document.get("name"));
                        } else {
                            Date date = new Date();
                            String now = simpleDateFormat.format(date.getTime());
                            try {
                                Date t = simpleDateFormat.parse(now);
                                Date exp = simpleDateFormat.parse(bp.getExpDate());
                                if (exp != null) {
                                    if (date.getTime() > exp.getTime()) {
                                        sb.append("expired!");
                                    } else if(t != null){
                                        long diffInMilli = exp.getTime() - t.getTime();
                                        int diffDays = (int) TimeUnit.DAYS.convert(diffInMilli,TimeUnit.MILLISECONDS);
                                        sb.append("expires in ").append(diffDays).append(" day(s)");
                                    }
                                }
                            } catch (ParseException e) {
                                Log.d(TAG, "couldn't parse date");
                            }
                            Log.d(TAG, "item: "+sb.toString());
                        }
                        expiration = sb.toString();
                        quantity = bp.getQuantity() + "";

                        // todo: sprint 2 fix display of notes
                        notes = Utils.isNotNullOrEmpty(bp.getNotes())?bp.getNotes():"";
                        readinFridgeList.add(new FridgeItem(item, expiration, quantity, notes, bp, fridgeListRef.document(document.getId()), document.getId()));
                    }
                    rvAdapter.addAll(readinFridgeList);
                    rvAdapter.notifyDataSetChanged();
                }
            }
        });

        return root;
    }


    /*@Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number "  + ", which is at cell position " );
        Toast.makeText(view.getContext(), "Grid item clicked!", Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == MANUAL_ITEM_ADDED){
            if (rvAdapter != null) {
                rvAdapter.notifyDataSetChanged();
            }
        }

    }
}

