package com.jjkaps.epantry.ui.Fridge;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private ArrayList<FridgeItem> itemList;
    //private final AdapterView.OnItemClickListener incListener;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView tvItemName;
        public TextView tvItemQuantity;
        public TextView tvItemNotes;
        private Button incButton;
        private Button decButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_fridgeItem);
            tvItemQuantity = itemView.findViewById(R.id.tv_fridgeItemQuantity);
            tvItemNotes = itemView.findViewById(R.id.tv_notes);
            incButton = itemView.findViewById(R.id.btn_inc);
            decButton = itemView.findViewById(R.id.btn_dec);

        }
    }

    public ItemAdapter(ArrayList<FridgeItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_item, parent, false); // assigns the fridge_item layout to rv
        ItemViewHolder ivh = new ItemViewHolder(v);
        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        final FridgeItem currentItem = itemList.get(position);

        holder.tvItemName.setText(currentItem.getTvFridgeItemName());
        holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());
        holder.tvItemNotes.setText(currentItem.getTvFridgeItemNotes());
        holder.incButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentItem.incTvFridgeItemQuantity();
                holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());
            }
        });
        holder.decButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentItem.decTvFridgeItemQuantity();
                holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());

            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BarcodeProduct bp = currentItem.getBarcodeProduct();
                Context c = holder.itemView.getContext();
                if(bp != null) {
                    Intent i = new Intent(c, ItemActivity.class);
                    i.putExtra("barcodeProduct", bp);
                    i.putExtra("docID", currentItem.getFridgeItemRef().getPath());
                    c.startActivity(i);
                }//TODO else
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
