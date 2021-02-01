package com.jjkaps.epantry.ui.Fridge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jjkaps.epantry.R;

import java.util.Comparator;
import java.util.List;

public class StorageFragement extends Fragment {
    private List<FridgeItem> fridgeItems;
    private RecyclerView rvFridgeList;
    private ItemAdapter rvAdapter;

    public StorageFragement(List<FridgeItem> fridgeItems) {
        this.fridgeItems = fridgeItems;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fridge_tab_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvFridgeList = view.findViewById(R.id.recyclerListFridgeList);
        rvFridgeList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        rvAdapter = new ItemAdapter(fridgeItems, rvFridgeList);
        rvFridgeList.setAdapter(rvAdapter);
    }

    public void sortList(Comparator<FridgeItem> comparatorName) {
        fridgeItems.sort(comparatorName);
        rvAdapter.clear();
        rvAdapter.addAll(fridgeItems);
        rvAdapter.notifyDataSetChanged();
    }

    public void dataChanged() {
        rvAdapter.notifyDataSetChanged();
    }
}
