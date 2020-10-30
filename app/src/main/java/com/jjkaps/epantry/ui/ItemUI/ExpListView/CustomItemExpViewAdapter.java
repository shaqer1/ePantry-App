package com.jjkaps.epantry.ui.ItemUI.ExpListView;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.TextView;

import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.Recipes.BPAdapterItem;
import com.jjkaps.epantry.utils.Utils;

import java.util.List;

public class CustomItemExpViewAdapter extends BaseExpandableListAdapter {
    private Context c;
    private List<String> itemsTitle;
    private BPAdapterItem bpAdapterItem;

    public CustomItemExpViewAdapter(Context c, List<String> itemsTitle, BPAdapterItem bpAdapterItem) {
        this.c = c;
        this.bpAdapterItem = bpAdapterItem;
        this.itemsTitle = itemsTitle;
    }

    @Override
    public int getGroupCount() {
        return itemsTitle.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        return itemsTitle.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return bpAdapterItem;
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int listPosition, boolean b, View convertView, ViewGroup viewGroup) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, viewGroup, false);
        }
        TextView listTitleTextView = convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        /*listTitleTextView.setOnClickListener((View.OnClickListener) view -> {
            ExpandableListView expandableListView = (ExpandableListView) viewGroup;
            if (!b) {
                expandableListView.expandGroup(listPosition);
            }
            else {
                expandableListView.collapseGroup(listPosition);
            }
        });*/
        return convertView;
    }

    @Override
    public View getChildView(int listPosition, int expandedListPosition, boolean b, View convertView, ViewGroup viewGroup) {
        //TODO
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch ((String) getGroup(listPosition)){
                case "Ingredients":
                    convertView = layoutInflater.inflate(R.layout.item_ingredients_exp, viewGroup, false);
                    break;
                default:
            }
        }
        //process Ingredients
        //ingred
        EditText ingredientsTV = convertView.findViewById(R.id.item_ingred);
        ingredientsTV.setScroller(new Scroller(c));
        ingredientsTV.setVerticalScrollBarEnabled(true);
        EditText palmOilIngredTV = convertView.findViewById(R.id.palm_oil_ingr);
        /*ingredients*/
        if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getIngredients())){
            ingredientsTV.setText(bpAdapterItem.getBarcodeProduct().getIngredients());
        }
        /*palm oil chip*/
        if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getPalm_oil_ingredients())
                && bpAdapterItem.getBarcodeProduct().getPalm_oil_ingredients().size() > 0){
            palmOilIngredTV.setText(Utils.getStringArr(bpAdapterItem.getBarcodeProduct().getPalm_oil_ingredients()));
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
