package com.jjkaps.epantry.ui.Settings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;

public class SettingFavItemAdapter extends ArrayAdapter<SettingFavItem> {
    private FirebaseAuth mAuth;
    private CollectionReference shopListRef;
    private ArrayList<SettingFavItem> settingFavItems;
    private ListView listView_favItem;
    private Comparator<SettingFavItem> sortFavList;
    private Context context;



    public SettingFavItemAdapter(@NonNull Context context, ArrayList<SettingFavItem> items, ListView listView_favItem) {
        super(context, 0, items);
        this.context = context;
        this.settingFavItems = items;
        this.listView_favItem = listView_favItem;
    }
    private static class ViewHolder {
        TextView favItem;
        ImageButton favoriteButton;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final SettingFavItem settingFavItem = getItem(position);
        final SettingFavItemAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new SettingFavItemAdapter.ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.setting_fav_list, parent, false);
            viewHolder.favItem = convertView.findViewById(R.id.txt_setting_favList);
            viewHolder.favoriteButton = convertView.findViewById(R.id.setting_favoriteButton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SettingFavItemAdapter.ViewHolder) convertView.getTag();
        }


        if (settingFavItem != null && user != null) {
            //initialize the fav list
            shopListRef = db.collection("users").document(user.getUid()).collection("shoppingList");
            viewHolder.favItem.setText(settingFavItem.getBarcodeProduct().getName());
            viewHolder.favoriteButton.setImageResource(R.drawable.ic_filled_heart_24dp);
            settingFavItem.setFavorite(true);

            viewHolder.favoriteButton.setOnClickListener(view -> {
                if (settingFavItem.isFavorite()){
                    viewHolder.favoriteButton.setImageResource(R.drawable.ic_empty_heart_24dp);
                    settingFavItem.setFavorite(false);
                }else{
                    viewHolder.favoriteButton.setImageResource(R.drawable.ic_filled_heart_24dp);
                    settingFavItem.setFavorite(true);
                }
                Log.d("TAG", "onClick: "+settingFavItem.getDocReference());
//                    if(settingFavItem.getBarcodeProduct() != null && Utils.isNotNullOrEmpty(settingFavItem.getBarcodeProduct().getCatalogReference())){
                //Log.d("TAG", "onClick: "+settingFavItem.isFavorite());
                db.document(settingFavItem.getDocReference()).update("favorite", settingFavItem.isFavorite())
                .addOnFailureListener(e -> {
                    //toast
                    Utils.createStatusMessage(Snackbar.LENGTH_LONG, this.listView_favItem, "Could not favorite item, Please try again.", Utils.StatusCodes.FAILURE);
                    boolean isFav = settingFavItem.isFavorite();
                    viewHolder.favoriteButton.setImageResource(!isFav ? R.drawable.ic_filled_heart_24dp : R.drawable.ic_empty_heart_24dp);
                    viewHolder.favoriteButton.setTag(!isFav ? Boolean.TRUE : Boolean.FALSE);
                    settingFavItem.setFavorite(!isFav ? Boolean.TRUE : Boolean.FALSE);
                });
            });
        }

        return convertView;
    }

}
