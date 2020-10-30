package com.jjkaps.epantry.ui.Fridge;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.ProductModels.InventoryDetails;
import com.jjkaps.epantry.models.ShoppingListItem;
import com.jjkaps.epantry.ui.ItemUI.AddFridgeToShopping;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private static final String TAG = "ItemAdapter";
    private final RecyclerView rv;
    private final FirebaseUser user;
    private CollectionReference fridgeListRef;
    private CollectionReference shoppingListRef;

    private final ArrayList<FridgeItem> itemList;
    private ItemClickListener mClickListener;

    private final FirebaseStorage storage;




    public void clear() {
        itemList.clear();
    }

    public void addAll(ArrayList<FridgeItem> readInFridgeList) {
        itemList.addAll(readInFridgeList);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener, ItemTouchHelperViewHolder{
        public TextView tvItemName;
        public TextView tvItemQuantity;
        public TextView tvItemNotes;
        public TextView tvExpDate;
        private final Button incButton;
        private final Button decButton;
        private final ImageView itemImage;
        private final ImageButton favoriteButton;
        //private BarcodeProduct catalogRefBP;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_fridgeItem);
            tvItemQuantity = itemView.findViewById(R.id.tv_fridgeItemQuantity);
            tvItemNotes = itemView.findViewById(R.id.tv_notes);
            tvExpDate = itemView.findViewById(R.id.tv_expdate);
            incButton = itemView.findViewById(R.id.btn_inc);
            decButton = itemView.findViewById(R.id.btn_dec);
            itemImage = itemView.findViewById(R.id.tv_fridgeImage);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            //NOTE Tag keeps track of if the item is favorite
            favoriteButton.setTag(Boolean.FALSE);
        }


        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            //onItemClick(view, getAdapterPosition());
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    public ItemAdapter(ArrayList<FridgeItem> itemList, RecyclerView rv) {
        this.itemList = itemList;
        this.rv = rv;
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fridgeListRef = Utils.getFridgeListRef(user);
            shoppingListRef = Utils.getShoppingListRef(user);
        }
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_item, parent, false); // assigns the fridge_item layout to rv
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        final FridgeItem currentItem = itemList.get(position);
        holder.tvItemName.setText(Utils.toSentCase(currentItem.getTvFridgeItemName()));
        holder.tvExpDate.setText(currentItem.getTvFridgeItemExpDate());
        holder.tvItemQuantity.setText((currentItem.getTvFridgeItemQuantity()+""));
        if(currentItem.getTvFridgeItemQuantity() == 1){
            holder.decButton.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_delete_24dp, null));
        }else{
            holder.decButton.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_minus_24dp, null));
        }
        holder.tvItemNotes.setText(currentItem.getTvFridgeItemNotes());

        Log.d(TAG, "onBindViewHolder: quantity: "+currentItem.getTvFridgeItemQuantity());
        //GET Product Info for the fridge item
        BarcodeProduct bp = itemList.get(position).getBarcodeProduct();
        if(itemList.get(position).getBarcodeProduct() != null){
            initItem(holder, position, bp);
        }else{
            //NOTE: FAILSAFE this should never happen, get object again if null
            itemList.get(position).getFridgeItemRef().get().addOnSuccessListener(documentSnapshot -> {
                itemList.get(position).setBarcodeProduct(documentSnapshot.toObject(BarcodeProduct.class));
                BarcodeProduct bp1 = itemList.get(position).getBarcodeProduct();
                initItem(holder, position, bp1);
            });
        }

        //favButton
        holder.favoriteButton.setOnClickListener(view -> {
            //if not in catalog say can't favorite
            /*if(itemList.get(position).getBarcodeProduct() != null && !Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                Utils.createStatusMessage(SnackBar.LENGTH_LONG, ItemAdapter.this.rv, "Cannot favorite an item not in catalog list.", Utils.StatusCodes.FAILURE);
                return;
            }*/
            //else adjust icon
            if ((Boolean) holder.favoriteButton.getTag()){
                holder.favoriteButton.setImageResource(R.drawable.ic_empty_heart_24dp);
                holder.favoriteButton.setTag(Boolean.FALSE);
            }else{
                holder.favoriteButton.setImageResource(R.drawable.ic_filled_heart_24dp);
                holder.favoriteButton.setTag(Boolean.TRUE);
            }
            //update in collection, prompt on fail
            //if(itemList.get(position).getBarcodeProduct() != null && Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                itemList.get(position).getFridgeItemRef().update("favorite", holder.favoriteButton.getTag())
                        .addOnFailureListener(e -> {
                            //toast
                            Utils.createStatusMessage(Snackbar.LENGTH_LONG, ItemAdapter.this.rv, "Could not favorite item, Please try again", Utils.StatusCodes.FAILURE);
                            boolean isFav = (boolean) holder.favoriteButton.getTag();
                            holder.favoriteButton.setImageResource(!isFav ? R.drawable.ic_filled_heart_24dp : R.drawable.ic_empty_heart_24dp);
                            holder.favoriteButton.setTag(!isFav ? Boolean.TRUE : Boolean.FALSE);
                        });
            //}
        });

        // incrementing the quantity
        holder.incButton.setOnClickListener(view -> {
            // increment on the UI

            holder.tvItemQuantity.setText(((currentItem.getTvFridgeItemQuantity() + 1)+""));

            // find the item that's quantity is being updated
            bp.getInventoryDetails().setQuantity(currentItem.getTvFridgeItemQuantity() + 1);
            fridgeListRef.document(currentItem.getDocID()).update("inventoryDetails", bp.getInventoryDetails())
            .addOnSuccessListener(aVoid -> {
                // item is not suggested
                if(itemList.get(position).getBarcodeProduct() != null){
                    itemList.get(position).getFridgeItemRef().update("suggested", false);
                }
            });
        });


        // decrementing the quantity
        holder.decButton.setOnClickListener(view -> {
            boolean fav = false;
            // suggest items that have been decremented to 1 and are NOT favorites
            if (currentItem.getTvFridgeItemQuantity()==2) {
                if(itemList.get(position).getBarcodeProduct() != null){
                    fav = (boolean) holder.favoriteButton.getTag();
                    if (!fav) { itemList.get(position).getFridgeItemRef().update("suggested", true); }
                }
            }
            // cannot decrement to 0
            if (currentItem.getTvFridgeItemQuantity()!=1) {
                holder.tvItemQuantity.setText(((currentItem.getTvFridgeItemQuantity() - 1)+""));
                // find the item that's quantity is being updated
                bp.getInventoryDetails().setQuantity(currentItem.getTvFridgeItemQuantity() - 1);
                fridgeListRef.document(currentItem.getDocID()).update("inventoryDetails", bp.getInventoryDetails());

            } else { // remove item from fridgeList when quantity reaches zero
                //get fav boolean and make item suggested if not a fav
                holder.decButton.setBackground(ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.ic_delete_24dp, null));
                if(itemList.get(position).getBarcodeProduct() != null){
                    fav = (boolean) holder.favoriteButton.getTag();
                    if (!fav) { itemList.get(position).getFridgeItemRef().update("suggested", true); }
                }
                if(fav){
                    //automatically add item to shopping list
                    addFavToShopping(currentItem);
                }else {
                    Intent intent = new Intent(view.getContext(), AddFridgeToShopping.class);
                    Bundle b = new Bundle();
                    b.putString("itemName", currentItem.getTvFridgeItemName()); // add item name
                    b.putString("docRef", currentItem.getFridgeItemRef().getPath()); // add item ref
                    intent.putExtras(b); // associate name with intent
                    view.getContext().startActivity(intent);
                }

                // remove item from the fridge
                bp.setInventoryDetails(new InventoryDetails(null, 0));
                fridgeListRef.document(currentItem.getDocID()).update("inventoryDetails", bp.getInventoryDetails(), "inStock", false);

            }
        });


        holder.itemView.setOnClickListener(view -> {
            BarcodeProduct bp1 = currentItem.getBarcodeProduct();
            Context c = holder.itemView.getContext();
            if(bp1 != null) {
                Intent i = new Intent(c, ItemActivity.class);
                i.putExtra("barcodeProduct", bp1);
                i.putExtra("currCollection", "fridgeList");
                i.putExtra("docID", currentItem.getFridgeItemRef().getPath());
                c.startActivity(i);
            }
        });
    }

    private void initItem(final ItemViewHolder holder, int position, BarcodeProduct bp) {
        setProductImage(holder, itemList.get(position).getBarcodeProduct());
        //listens for updates to the doc with the favorite field
        holder.favoriteButton.setImageResource(bp.getFavorite() ? R.drawable.ic_filled_heart_24dp : R.drawable.ic_empty_heart_24dp);
        holder.favoriteButton.setTag(bp.getFavorite() ? Boolean.TRUE : Boolean.FALSE);
        /*if(Utils.isNotNullOrEmpty(bp.getCatalogReference())){
            db.document(bp.getCatalogReference()).addSnapshotListener((value, error) -> {
                if (value != null){
                    holder.catalogRefBP = value.toObject(BarcodeProduct.class);
                    if (holder.catalogRefBP != null && Utils.isNotNullOrEmpty(holder.catalogRefBP.getFavorite())) {
                        holder.favoriteButton.setImageResource(holder.catalogRefBP.getFavorite() ? R.drawable.ic_filled_heart_24dp : R.drawable.ic_empty_heart_24dp);
                        holder.favoriteButton.setTag(holder.catalogRefBP.getFavorite() ? Boolean.TRUE : Boolean.FALSE);
                    }else{
                        holder.catalogRefBP = null;
                        bp.setCatalogReference("");
                        holder.favoriteButton.setImageResource(R.drawable.ic_empty_heart_24dp);
                        fridgeListRef.document().update("catalogReference", "");
                    }
                } else if(error!= null){
                    holder.catalogRefBP = null;
                    bp.setCatalogReference("");
                    holder.favoriteButton.setImageResource(R.drawable.ic_empty_heart_24dp);
                    fridgeListRef.document().update("catalogReference", "");
                }
            });
        } else{
            Log.d(TAG, "This fridge item is not in catalog list! Won't be able to favorite");
            holder.favoriteButton.setImageResource(R.drawable.ic_empty_heart_24dp);
        }*/
    }

    private void setProductImage(final ItemViewHolder holder, BarcodeProduct bp) {
        if(Utils.isNotNullOrEmpty(bp.getUserImage())){
            //load image
            StorageReference imageStorage = storage.getReference("images/"+ user.getUid()+bp.getName().toLowerCase());
            final long OM = 5000 * 500000000L;
            imageStorage.getBytes(OM).addOnSuccessListener(bytes -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.itemImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
            }).addOnFailureListener(e ->
                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, rv, "Couldn't download custom image.", Utils.StatusCodes.FAILURE));
        }else if(Utils.isNotNullOrEmpty(bp.getFrontPhoto()) && Utils.isNotNullOrEmpty(bp.getFrontPhoto().getThumb())){
            Picasso.get().load(bp.getFrontPhoto().getThumb()).into(holder.itemImage);
        }else{
            holder.itemImage.setImageResource(R.drawable.image_not_found);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);

    }

    public String getItem(int id){
        return itemList.get(id).getTvFridgeItemName();
    }

    public FridgeItem getItemAll(int id){
        return itemList.get(id);
    }


    public void onItemClick(View view, int position) {
        Log.i("TAG", "####You clicked number " + getItem(position) + ", which is at cell position " + position);
    }

    public void addFavToShopping(FridgeItem currentItem){
        final String itemName = currentItem.getTvFridgeItemName();

        shoppingListRef.whereEqualTo("docReference",currentItem.getFridgeItemRef().getPath()).get().addOnCompleteListener(task -> {//TODO change with doc ref TEST?
            if (task.isSuccessful() && task.getResult() != null) {
                if (task.getResult().size()==0) {
                    ShoppingListItem sli = new ShoppingListItem(itemName.toLowerCase(), 1, false, "", currentItem.getFridgeItemRef().getPath());
                    shoppingListRef.add(sli).addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "onSuccess: " + itemName + " added.");
                            Utils.createStatusMessage(Snackbar.LENGTH_SHORT, ItemAdapter.this.rv, itemName +" added to Shopping List", Utils.StatusCodes.SUCCESS);
                        }).addOnFailureListener(e -> {
                                Log.d(TAG, "onFailure: adding fav to shopping list", e);
                                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, ItemAdapter.this.rv, "Failed to add "+itemName +" to Shopping List", Utils.StatusCodes.FAILURE);
                                }
                        );
                }else{
                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, ItemAdapter.this.rv, itemName + " is already in the Shopping List", Utils.StatusCodes.FAILURE);
                }
            }
        });
    }



}
