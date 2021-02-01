package com.jjkaps.epantry.ui.Fridge;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class StoragePagerAdapter extends FragmentStatePagerAdapter {


    private final List<FridgeItem> items;
    private List<String> storageTypes;
    private final FragmentManager fm;
    private HashMap<String, List<FridgeItem>> storageToItemMap;
    private SparseArray<Fragment> myPagerFragments;

    public StoragePagerAdapter(List<FridgeItem> items, List<String> storageTypes, FragmentManager fm){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.items = items;
        storageTypes.add(0, "All");
        myPagerFragments= new SparseArray<>();
        storageToItemMap = new HashMap<>();
        for(String t : storageTypes){
            storageToItemMap.put(t, items.stream().filter(fridgeItem ->
                    fridgeItem.getBarcodeProduct().getStorageType().equals(t) || t.equals("All")).collect(Collectors.toList())
            );
        }
        this.storageTypes = storageTypes;
        this.fm = fm;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment f = (Fragment) super.instantiateItem(container, position);
        myPagerFragments.put(position, f);
        return f;
    }

    public Fragment getRegisteredFragment(int position) {
        return myPagerFragments.get(position);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        StorageFragement storageFragment = new StorageFragement(storageToItemMap.get(storageTypes.get(position)));

        /*Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
        fragment.setArguments(args);*/
        return storageFragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        myPagerFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return storageTypes.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return storageTypes.get(position);
    }
}
