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
import com.jjkaps.epantry.models.FridgeItem;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.ui.scanCode.ScanItem;
import com.jjkaps.epantry.utils.CustomSorter;
import com.jjkaps.epantry.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    private StoragePagerAdapter storagePagerAapter;
    private ViewPager viewPager;
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
        currentPage = 0;
        viewPager.setAdapter(storagePagerAapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                if (sorting == 1) {
                    ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(CustomSorter.getComparatorName());
                } else if (sorting == 2) {
                    ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(CustomSorter.getComparatorQuantity());
                } else if (sorting == 4) {
                    ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(CustomSorter.getComparatorExp());
                }
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
                Intent intent = new Intent(c, ItemActivity.class);
                intent.putExtra("AddItem", true);
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
                    if(bp.getStorageType() != null){
                        storageList.add(bp.getStorageType());
                    }
                    /*StringBuilder sb = new StringBuilder();
                    if (!Utils.isNotNullOrEmpty(bp.getInventoryDetails().getExpDate())) {
                        Log.d(TAG, "expDate length == 0"+document.get("name"));
                    } else {
                        Date date = new Date();
                        Date exp = bp.getInventoryDetails().getExpDate();
                        if (exp != null) {
                            if (date.getTime() > exp.getTime()) {
                                sb.append("expired!");
                            } else {
                                long diffInMilli = exp.getTime() - date.getTime();
                                int diffDays = (int) TimeUnit.DAYS.convert(diffInMilli,TimeUnit.MILLISECONDS);
                                sb.append("expires in ").append(diffDays).append(" day(s)");
                            }
                        }
                    }*/
                    readinFridgeList.add(new FridgeItem(bp, document.getReference()));
                }
                if(sorting==1){
                    readinFridgeList.sort(CustomSorter.getComparatorName());
                }
                if(sorting==2){
                    readinFridgeList.sort(CustomSorter.getComparatorQuantity());
                }
                if(sorting==4){
                    readinFridgeList.sort(CustomSorter.getComparatorExp());
                }
                /*rvAdapter.addAll(readinFridgeList);
                rvAdapter.notifyDataSetChanged();*/
                //storagePagerAapter = new StoragePagerAdapter(readinFridgeList, new ArrayList<>(storageList), getChildFragmentManager());
                storagePagerAapter.updateItems(readinFridgeList, new ArrayList<>(storageList));
                viewPager.setAdapter(storagePagerAapter);
                viewPager.setCurrentItem(currentPage, false);
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
                        ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(CustomSorter.getComparatorName());
                        return true;
                    } else if (itemId == R.id.sortQuantity) {
                        sorting = 2;
                        ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(CustomSorter.getComparatorQuantity());
                        return true;
                    } else if (itemId == R.id.sortExpirationDate) {
                        sorting = 4;
                        ((StorageFragement) storagePagerAapter.getRegisteredFragment(currentPage)).sortList(CustomSorter.getComparatorExp());
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
}

