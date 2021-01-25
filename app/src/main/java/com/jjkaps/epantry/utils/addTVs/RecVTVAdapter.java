package com.jjkaps.epantry.utils.addTVs;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.TVData.TextViewData;

import java.util.ArrayList;
import java.util.HashMap;

public class RecVTVAdapter extends RecyclerView.Adapter<RecVTVAdapter.ViewHolder>{
    private final AddTVItems addTVItems;
    private final ArrayList<TextViewData> itemNames;
    private static final HashMap<TextViewData, EditText> TVDToEditText = new HashMap<>();

    public RecVTVAdapter(AddTVItems addTVItems, ArrayList<TextViewData> itemNames) {
        this.addTVItems = addTVItems;
        this.itemNames = itemNames;
    }

    public ArrayList<TextViewData> getResponse(){
        final boolean[] error = {false};
        TVDToEditText.forEach((k,v) -> {
            if(k.getResponse()==null){
                error[0] = true;
                v.setError("This field is required");
            }
        });
        return (error[0])?null:itemNames;
    }

    @NonNull
    @Override
    public RecVTVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecVTVAdapter.ViewHolder holder, int position) {
        TextViewData currentItem = (TextViewData) getItem(position);
        TVDToEditText.put(currentItem, holder.editText);
        if(currentItem.getUnitText()!= null){
            holder.unitTV.setText(currentItem.getUnitText());
        }
        holder.textInputLayout.setHint(currentItem.getName());
        holder.editText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(currentItem.getMaxLength())
        });
        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentItem.setResponse(s.toString());
            }
        });
    }

    private Object getItem(int position) {
        return itemNames.get(position);
    }

    @Override
    public int getItemCount() {
        return itemNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText editText;
        TextInputLayout textInputLayout;
        TextView unitTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.item_value);
            textInputLayout = itemView.findViewById(R.id.item_til);
            unitTV = itemView.findViewById(R.id.unit_tv);
        }
    }
}
