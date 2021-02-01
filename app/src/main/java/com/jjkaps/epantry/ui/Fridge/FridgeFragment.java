package com.jjkaps.epantry.ui.Fridge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FridgeFragment extends Fragment {

    private static final int MANUAL_ITEM_ADDED = 2;
    private CollectionReference fridgeListRef;


    private int sorting = 0;
    private SimpleDateFormat simpleDateFormat;
    private List<FridgeItem> readinFridgeList;
    private RelativeLayout noItemsRL;
    private Button noItemsImageBT;

    private static final String TAG = "FridgeFragment";
    private ImageButton ib, ibSort;
    private View root;
    private HashSet<String> storageList;
    StoragePagerAdapter storagePagerAapter;
    ViewPager viewPager;
    private TabLayout tabLayout;
    private int currentPage = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_fridge, container, false);
        //send firebase analytic
        Utils.addAnalytic(TAG, "opened fridge fragment", "text", root.getContext());
        Utils.hideKeyboard(root.getContext());
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if(u!= null){
            fridgeListRef = Utils.getFridgeListRef(u);
        }
        storageList = new HashSet<>();
        simpleDateFormat = Utils.getExpDateFormat();
        noItemsRL = root.findViewById(R.id.NoItemsRL);
        noItemsImageBT = root.findViewById(R.id.NoItemsButton);
        noItemsImageBT.setOnClickListener(view -> getAddClickPopup(getActivity(), noItemsImageBT));
        // retrieve fridgeList from Firebase live NOTE:add snapshot listener listens for live changes and format
        // no need to refresh list each time
       readinFridgeList = new ArrayList<>();
       retrieveList();


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        viewPager = view.findViewById(R.id.fridgeTabsPager);
        storagePagerAapter = new StoragePagerAdapter(readinFridgeList, new ArrayList<>(storageList), getChildFragmentManager());
        viewPager.setAdapter(storagePagerAapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                //TODO sort items
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout = view.findViewById(R.id.fridgeTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void getAddClickPopup(Context c, View ib){
        PopupMenu popupMenu = new PopupMenu(getContext(), ib);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_addfridge, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.addManually) {
                Intent intent = new Intent(c, AddFridgeItem.class);
                sorting = 1;
                startActivityForResult(intent, MANUAL_ITEM_ADDED);
                return true;
            } else if (itemId == R.id.scanItem) {
                Intent i = new Intent(c, ScanItem.class);
                sorting = 1;
                startActivity(i);
                Log.d(TAG, "scan Item");
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        customActionBar(context);
    }

    private void retrieveList() {
        fridgeListRef.whereEqualTo("inStock", true).addSnapshotListener((documents, error) -> {
            if(documents != null && !documents.isEmpty()){
                noItemsRL.setVisibility(View.GONE);
                readinFridgeList = new ArrayList<>();
                /*rvAdapter.clear();
                rvAdapter.notifyDataSetChanged();*/
                for (QueryDocumentSnapshot document : documents){
                    BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                    //append the expiration date to the name if expDate exists.
                    StringBuilder sb = new StringBuilder();
                    storageList.add(bp.getStorageType());
                    if (!Utils.isNotNullOrEmpty(bp.getInventoryDetails().getExpDate())) {
                        Log.d(TAG, "expDate length == 0"+document.get("name"));
                    } else {
                        Date date = new Date();
                        String now = simpleDateFormat.format(date.getTime());
                        try {
                            Date t = simpleDateFormat.parse(now);
                            Date exp = bp.getInventoryDetails().getExpDate();
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
                    readinFridgeList.add(new FridgeItem(bp.getName(), sb.toString(), bp.getInventoryDetails().getQuantity(), Utils.isNotNullOrEmpty(bp.getNotes())?bp.getNotes():"", bp, fridgeListRef.document(document.getId()), document.getId()));
                }
                if(sorting==1){
                    readinFridgeList.sort(comparatorName);
                }
                if(sorting==2){
                    readinFridgeList.sort(comparatorQuantity);
                }
                if(sorting==4){
                    readinFridgeList.sort(comparatorExp);
                }
                /*rvAdapter.addAll(readinFridgeList);
                rvAdapter.notifyDataSetChanged();*/
                storagePagerAapter = new StoragePagerAdapter(readinFridgeList, new ArrayList<>(storageList), getChildFragmentManager());
                viewPager.setAdapter(storagePagerAapter);
            }else {
                noItemsRL.setVisibility(View.VISIBLE);
            }
        });
    }

    private void customActionBar(Context c) {
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() != null && ((MainActivity) getActivity()).getSupportActionBar().getCustomView() != null){
            View view = ((MainActivity) getActivity()).getSupportActionBar().getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_fridge);
            ib = view.findViewById(R.id.btn_update);
            ib.setVisibility(View.VISIBLE);
            ib.setOnClickListener(view1 -> getAddClickPopup(c, ib));
            ibSort = view.findViewById(R.id.secondaryUpdate);
            ibSort.setVisibility(View.VISIBLE);
            ibSort.setOnClickListener(v -> {
                Log.d(TAG, "onClick: Clicked!");
                PopupMenu popupMenu = new PopupMenu(getContext(), ibSort);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_fridgesort, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.sortAlpha) {
                        sorting = 1;
                        ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(comparatorName);
                        return true;
                    } else if (itemId == R.id.sortQuantity) {
                        sorting = 2;
                        ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(comparatorQuantity);
                        return true;
                    } else if (itemId == R.id.sortExpirationDate) {
                        sorting = 4;
                        ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(comparatorExp);
                        return true;
                    } else if (itemId == R.id.sortStorage) {//todo Remove
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

        if(resultCode == MANUAL_ITEM_ADDED){
            if (storagePagerAapter != null) {
                ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).dataChanged();
            }
        }

    }
    Comparator<FridgeItem> comparatorName = (fridgeItem, t1) -> Integer.compare(fridgeItem.getTvFridgeItemName().compareToIgnoreCase(t1.getTvFridgeItemName()), 0);
    Comparator<FridgeItem> comparatorExp = (fridgeItem, fridgeItem2) -> {
        if(fridgeItem.getTvFridgeItemExpDate().equals("") && fridgeItem2.getTvFridgeItemExpDate().equals("")){
            return 0;
        }else if(fridgeItem.getTvFridgeItemExpDate().equals("")){
            return 1;
        } else if (fridgeItem2.getTvFridgeItemExpDate().equals("")){
            return -1;
        } else {
            try{
                if(Utils.isNotNullOrEmpty(fridgeItem.getBarcodeProduct()) && Utils.isNotNullOrEmpty(fridgeItem.getBarcodeProduct().getInventoryDetails().getExpDate())
                        && Utils.isNotNullOrEmpty(fridgeItem2.getBarcodeProduct()) && Utils.isNotNullOrEmpty(fridgeItem2.getBarcodeProduct().getInventoryDetails().getExpDate())){
                    Date d1 = fridgeItem.getBarcodeProduct().getInventoryDetails().getExpDate();
                    Date d2 = fridgeItem2.getBarcodeProduct().getInventoryDetails().getExpDate();
                    if(d1 != null && d2 != null && d2.compareTo(d1) < 0){
                        return 1;
                    }else if (d1 != null && d2 != null){
                        return -1;
                    }
                }
            }catch (Exception e){
                Log.d(TAG, "could not parse dates in fridge item exp");
            }
        }
        return 0;
    };
    Comparator<FridgeItem> comparatorQuantity = Comparator.comparing(FridgeItem::getTvFridgeItemQuantity);


}

