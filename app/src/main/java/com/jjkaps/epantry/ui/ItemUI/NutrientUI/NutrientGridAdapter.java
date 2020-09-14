package com.jjkaps.epantry.ui.ItemUI.NutrientUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.ProductModels.Nutrient;

import java.util.List;

public class NutrientGridAdapter extends BaseAdapter {

    private final Context c;
    private final List<Nutrient> nutrients;

    public NutrientGridAdapter(Context c, List<Nutrient> nutrients) {
        this.c = c;
        this.nutrients = nutrients;
    }

    @Override
    public int getCount() {
        return nutrients.size();
    }

    @Override
    public Object getItem(int i) {
        return nutrients.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View grid;
        Nutrient currentNut = (Nutrient) getItem(i);
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {

            grid = new View(c);
            grid = inflater.inflate(R.layout.nut_grid_item, viewGroup, false);
            MaterialTextView itemName = grid.findViewById(R.id.item_det_tv);
            TextInputLayout itemTIL = grid.findViewById(R.id.item_det_til);
            itemTIL.setHelperText(currentNut.getName());/* + " per 100g"*/
            itemName.setText((currentNut.getPer_100g()+" "+currentNut.getMeasurement_unit()));
        } else {
            grid = (View) view;
        }
        return grid;
    }
}
