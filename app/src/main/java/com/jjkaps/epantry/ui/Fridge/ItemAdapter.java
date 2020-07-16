package com.jjkaps.epantry.ui.Fridge;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.jjkaps.epantry.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private ArrayList<FridgeItem> itemList;
    //private final AdapterView.OnItemClickListener incListener;
    private ItemClickListener mClickListener;


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
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
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
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            onItemClick(view, getAdapterPosition());
        }
    }

    public String getItem(int id){
        return itemList.get(id).getTvFridgeItemName();
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number " + getItem(position) + ", which is at cell position " + position);
    }

}
