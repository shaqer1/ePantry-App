package com.jjkaps.epantry.ui.Fridge;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FridgeFragment extends Fragment implements OnStartDragListener {

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
    private Button sort;
    private Button doneSort;
    private static int sorting = 0;
    private String notes;
    private Dialog fridgeDialog;
    private String item;
    private String quantity;
    private String expiration;
    private EditText addedExpiration;
    private SimpleDateFormat simpleDateFormat;
    private ArrayList<FridgeItem> readinFridgeList;
    private static ArrayList<FridgeItem> readinFridgeListCust = new ArrayList<>();
   // private TextView txtNullList;

    private ItemTouchHelper mItemTouchHelper;
    private static final String TAG = "FridgeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fridgeViewModel = new ViewModelProvider(this).get(FridgeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_fridge, container, false);
        fridgeDialog = new Dialog(root.getContext());
        //send firebase analytic
        Utils.addAnalytic(TAG, "opened fridge fragment", "text", root.getContext());
        if (getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() != null) {
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_fridge);
        }
        Utils.hideKeyboard(root.getContext());
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
                                sorting = 1;
                                startActivityForResult(intent, MANUAL_ITEM_ADDED);
                                return true;
                            case R.id.scanItem:
                                Intent i = new Intent(root.getContext(), ScanItem.class);
                                sorting = 1;
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
       readinFridgeList = new ArrayList<>();
        fridgeListRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documents, @Nullable FirebaseFirestoreException error) {
                if(documents != null){
                    readinFridgeList = new ArrayList<>();
                    rvAdapter.clear();
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
                        //Log.d(TAG,"GAH\n\n\n"+fav);

                        notes = Utils.isNotNullOrEmpty(bp.getNotes())?bp.getNotes():"";
                        readinFridgeList.add(new FridgeItem(item, expiration, quantity, notes, bp, fridgeListRef.document(document.getId()), document.getId()));
                    }
                    if(sorting==1){
                        readinFridgeList.sort(comparatorName);
                    }
                    if(sorting==2){
                        readinFridgeList.sort(comparatorQuantity);
                    }
                    /*if(sorting==3){
                        readinFridgeList.sort(comparatorFav);
                    }*/
                    if(sorting==4){
                        readinFridgeList.sort(comparatorExp);
                    }
                    if(sorting==5){
                        if(readinFridgeListCust.size()!=0) {//TODO this can cause problems fix later
                            copyData(readinFridgeList, readinFridgeListCust);
                            readinFridgeList.clear();
                            readinFridgeList.addAll(readinFridgeListCust);
                        }
                        //readinFridgeList=readinFridgeListCust;
                    }

                    rvAdapter.addAll(readinFridgeList);
                    rvAdapter.notifyDataSetChanged();
                }
            }
        });

        sort = root.findViewById(R.id.sort);
        doneSort = root.findViewById(R.id.doneSort);
        doneSort.setVisibility(View.INVISIBLE);
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.d(TAG, "onClick: Clicked!");
                PopupMenu popupMenu = new PopupMenu(getContext(), sort);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_fridgesort, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.sortAlpha:
                                sorting = 1;
                                readinFridgeList.sort(comparatorName);
                                rvAdapter.clear();
                                rvAdapter.addAll(readinFridgeList);
                                rvAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.sortQuantity:
                                sorting = 2;
                                readinFridgeList.sort(comparatorQuantity);
                                rvAdapter.clear();
                                rvAdapter.addAll(readinFridgeList);
                                rvAdapter.notifyDataSetChanged();
                                return true;
                            /*case R.id.sortFav:
                                sorting = 3;
                                readinFridgeList.sort(comparatorFav);
                                rvAdapter.clear();
                                rvAdapter.addAll(readinFridgeList);
                                rvAdapter.notifyDataSetChanged();
                                return true;*/
                            case R.id.sortExpirationDate:
                                sorting = 4;
                                readinFridgeList.sort(comparatorExp);
                                rvAdapter.clear();
                                rvAdapter.addAll(readinFridgeList);
                                rvAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.sortManual:
                                sorting = 5;
                                doneSort.setVisibility(View.VISIBLE);
                                Toast toast= Toast.makeText(getContext(),"Now you can drag and drop items to sort.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                View vi = toast.getView();
                                TextView text = vi.findViewById(android.R.id.message);
                                text.setTextColor(Color.BLACK);
                                text.setTextSize(25);
                                toast.show();
                                sortManually();

                              //  rvAdapter.notifyDataSetChanged();
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

    private void copyData(ArrayList<FridgeItem> readinFridgeList, ArrayList<FridgeItem> readinFridgeListCust) {
        boolean found = true;
        if(readinFridgeList.size()<readinFridgeListCust.size()){
            for (int i = readinFridgeListCust.size()-1; i >=0; i--) {
                found = false;
                for (int j = 0; j < readinFridgeList.size(); j++) {
                    if(readinFridgeList.get(j).getDocID().equals(readinFridgeListCust.get(i).getDocID())){
                        found = true;
                    }
                }
                if(!found){
                    readinFridgeListCust.remove(i);
                }
            }
        }
        for (int i = 0; i < readinFridgeList.size(); i++) {
            found = false;
            for (int j = readinFridgeListCust.size()-1; j >=0; j--) {
                if(readinFridgeListCust.get(j).getDocID().equals(readinFridgeList.get(i).getDocID())){
                    readinFridgeListCust.set(j,readinFridgeList.get(i));
                    found = true;
                }
            }
            if(!found){
                readinFridgeListCust.add(readinFridgeList.get(i));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == MANUAL_ITEM_ADDED){
            if (rvAdapter != null) {
                rvAdapter.notifyDataSetChanged();
            }
        }

    }
    Comparator<FridgeItem> comparatorName = new Comparator<FridgeItem>() {
        @Override
        public int compare(FridgeItem fridgeItem, FridgeItem t1) {
            return Integer.compare(fridgeItem.getTvFridgeItemName().compareToIgnoreCase(t1.getTvFridgeItemName()), 0);
        }
    };
    Comparator<FridgeItem> comparatorExp = new Comparator<FridgeItem>() {
        @Override
        public int compare(FridgeItem fridgeItem, FridgeItem fridgeItem2) {
            //int time = 0;
            if(fridgeItem.getTvFridgeItemExpDate().equals("") && fridgeItem2.getTvFridgeItemExpDate().equals("")){
                return 0;
            }else if(fridgeItem.getTvFridgeItemExpDate().equals("")){
                return 1;
            } else if (fridgeItem2.getTvFridgeItemExpDate().equals("")){
                return -1;
            } else {
                try{
                    if(Utils.isNotNullOrEmpty(fridgeItem.getBarcodeProduct()) && Utils.isNotNullOrEmpty(fridgeItem.getBarcodeProduct().getExpDate())
                            && Utils.isNotNullOrEmpty(fridgeItem2.getBarcodeProduct()) && Utils.isNotNullOrEmpty(fridgeItem2.getBarcodeProduct().getExpDate())){
                        Date d1 = new SimpleDateFormat("MM/dd/yyyy").parse(fridgeItem.getBarcodeProduct().getExpDate());
                        Date d2 = new SimpleDateFormat("MM/dd/yyyy").parse(fridgeItem2.getBarcodeProduct().getExpDate());
                        if(d1 != null && d2 != null && d2.compareTo(d1) < 0){
                            return 1;
                        }else if (d1 != null && d2 != null){
                            return -1;
                        }
                    }
                }catch (Exception e){
                    Log.d(TAG, "could not parse dates in fridge item exp");
                }
                /*time = Integer.parseInt(fridgeItem.getTvFridgeItemExpDate().replaceAll("[\\D]", ""));
                int time2 = Integer.parseInt(t1.getTvFridgeItemExpDate().replaceAll("[\\D]", ""));
                if (time > time2) {
                    return 1;
                }
                if (time < time2) {
                    return -1;
                }*/
            }
            return 0;
        }
    };
    /*Comparator<FridgeItem> comparatorFav = new Comparator<FridgeItem>() {
        @Override
        public int compare(FridgeItem fridgeItem, FridgeItem t1) {
            if(fridgeItem.getFav() && !t1.getFav()){
                    return -1;
                }
                if(!fridgeItem.getFav() && t1.getFav()){
                    return 1;
                }
                    return 0;

            }
    };*/
    Comparator<FridgeItem> comparatorQuantity = Comparator.comparing(FridgeItem::getTvFridgeItemQuantity);

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void sortManually(){
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(rvAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(rvFridgeList);
        rvAdapter.notifyDataSetChanged();
        doneSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                readinFridgeListCust.clear();
                for(int i=0 ; i<rvAdapter.getItemCount() ; i++){
                    readinFridgeListCust.add(rvAdapter.getItemAll(i));
                    Log.d(TAG,"gah\n\n\n"+rvAdapter.getItem(i)+i);
                }
                rvFridgeList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                rvFridgeList.setAdapter(rvAdapter);
             //   rvAdapter.clear();
               // if(readinFridgeListCust.size()!=0) {
                 //   rvAdapter.addAll(readinFridgeListCust);
                //}
                rvAdapter.notifyDataSetChanged();
                doneSort.setVisibility(View.INVISIBLE);
            }

            });

    }
}

