package com.jjkaps.epantry.ui.ItemUI.NutrientUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ProductModels.Nutrient;
import com.jjkaps.epantry.models.TVData.TextViewData;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.utils.Utils;
import com.jjkaps.epantry.utils.addTVs.AddTVItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutrientRecyclerAdapter extends RecyclerView.Adapter<NutrientRecyclerAdapter.ViewHolder> {

    private static final int NUTRIENT_ACTIVITY_CODE = 75;
    private final Context c;
    private final List<Nutrient> nutrients;
    private ItemActivity activity;
    private final BarcodeProduct bp;
    private final CustomRecyclerView nutGridRecV;
    private ItemClickListener mClickListener;

    public NutrientRecyclerAdapter(Context c, ItemActivity activity, BarcodeProduct bp, CustomRecyclerView nutGridRecV) {
        this.c = c;
        this.activity = activity;
        this.bp = bp;
        this.nutGridRecV = nutGridRecV;
        if(bp.getNutrients()==null){
            bp.setNutrients(new ArrayList<>());
        }
        List<Nutrient> nutrientList = new ArrayList<>(bp.getNutrients());
        nutrientList.add(null);
        this.nutrients = nutrientList;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, FirebaseUser user) {
        if(requestCode == NUTRIENT_ACTIVITY_CODE){
            Bundle b = data.getBundleExtra("bundle");
            if(b != null){
                ArrayList<TextViewData> result = b.getParcelableArrayList("result");
                Nutrient nutNew = new Nutrient(result.get(0).getResponse(), -1,
                        result.get(1).getResponse(), 0, -1, "null");
                bp.getNutrients().add(nutNew);
                nutrients.set(nutrients.size()-1, nutNew);
                //notifyItemChanged(nutrients.size()-1);
                nutrients.add(null);
                //notifyItemInserted(nutrients.size()-1);
                notifyDataSetChanged();
            }else{
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, nutGridRecV, "Could not parse data", Utils.StatusCodes.FAILURE);
            }
        }
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        EditText nutQty;
        TextInputLayout nutTil;
        //EditText nutName;
        TextView nutUnit;
        Button addNewNutBt;

        ViewHolder(View itemView) {
            super(itemView);
            addNewNutBt = itemView.findViewById(R.id.addNutBut);
            nutQty = itemView.findViewById(R.id.nut_item_qty);
            nutTil = itemView.findViewById(R.id.nut_til);
            nutUnit = itemView.findViewById(R.id.nut_unit_tv);
            //nutName = itemView.findViewById(R.id.nut_name);
            //nutUnit = itemView.findViewById(R.id.nut_item_unit);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public Object getItem(int i) {
        return nutrients.get(i);
    }

    @NonNull
    @Override
    public NutrientRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nut_rec_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NutrientRecyclerAdapter.ViewHolder holder, int position) {
        Nutrient currentNut = (Nutrient) getItem(position);
        if(currentNut != null){
            holder.nutUnit.setVisibility(View.VISIBLE);
            holder.nutTil.setVisibility(View.VISIBLE);
            holder.addNewNutBt.setVisibility(View.GONE);
            /*int padding_in_dp = 8;  // 6 dps
            final float scale = c.getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // doesn't work
            if(position %2 ==0){
                holder.nutTil.setPadding(padding_in_px,0, padding_in_px/2,0);
                lp.setMargins(0,0, (int) ((8+ padding_in_px/2) * scale + 0.5f),0);
            }else{
                holder.nutTil.setPadding(padding_in_px/2,0, padding_in_px,0);
                lp.setMargins(0,0, (int) ((8+ padding_in_px) * scale + 0.5f),0);
            }
            holder.nutUnit.setLayoutParams(lp);*/
            holder.nutTil.setHint(currentNut.getName());
            holder.nutQty.setText(currentNut.getPer_100g()!=-1?(currentNut.getPer_100g() + ""):"");
            holder.nutUnit.setText((currentNut.getMeasurement_unit()+""));
        }else{
            holder.nutUnit.setVisibility(View.GONE);
            holder.nutTil.setVisibility(View.GONE);
            holder.addNewNutBt.setVisibility(View.VISIBLE);
            holder.addNewNutBt.setOnClickListener(v -> {
                Intent i = new Intent(c.getApplicationContext(), AddTVItems.class);
                Bundle args = new Bundle();
                args.putParcelableArrayList("textViews", new ArrayList<>(Arrays.asList(new TextViewData("Nutrient Name", 10), new TextViewData("Nutrient Unit", 2))));
                i.putExtra("bundle", args);
                i.putExtra("title", "Add Nutrient");
                activity.startActivityForResult(i, NUTRIENT_ACTIVITY_CODE);
            });
        }
    }

    @Override
    public int getItemCount() {
        return nutrients.size();
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }



}
