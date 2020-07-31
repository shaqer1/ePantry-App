package com.jjkaps.epantry.ui.Fridge;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.ui.ItemUI.AddFridgeToShopping;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> implements ItemTouchHelperAdapter{

    private static final String TAG = "ItemAdapter";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");
    private CollectionReference catalogListRef = db.collection("users").document(user.getUid()).collection("catalogList");
    private CollectionReference shoppingListRef = db.collection("users").document(user.getUid()).collection("shoppingList");

    private ArrayList<FridgeItem> itemList;
    //private final AdapterView.OnItemClickListener incListener;
    private ItemClickListener mClickListener;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private boolean fav;



    public void clear() {
        itemList.clear();
    }

    public void addAll(ArrayList<FridgeItem> readinFridgeList) {
        itemList.addAll(readinFridgeList);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
      //  Collections.s
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(itemList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(itemList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
//        Collections.swap(itemList, fromPosition, toPosition);
//        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener, ItemTouchHelperViewHolder{
        public TextView tvItemName;
        public TextView tvItemQuantity;
        public TextView tvItemNotes;
        public TextView tvExpDate;
        private Button incButton;
        private Button decButton;
        private ImageView itemImage;
        private ImageButton favoriteButton;
        private BarcodeProduct catalogRefBP;

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
            //itemView.setOnClickListener(this);
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
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        final FridgeItem currentItem = itemList.get(position);
        holder.tvItemName.setText(currentItem.getTvFridgeItemName());
        holder.tvExpDate.setText(currentItem.getTvFridgeItemExpDate());
        holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());
        holder.tvItemNotes.setText(currentItem.getTvFridgeItemNotes());

        Log.d(TAG, "onBindViewHolder: quantity: "+currentItem.getTvFridgeItemQuantity());
        //GET Product Info for the fridge item
        BarcodeProduct bp = itemList.get(position).getBarcodeProduct();
        if(itemList.get(position).getBarcodeProduct() != null){
            initItem(holder, position, bp);
        }else{
            //NOTE: FAILSAFE this should never happen, get object again if null
            itemList.get(position).getFridgeItemRef().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    itemList.get(position).setBarcodeProduct(documentSnapshot.toObject(BarcodeProduct.class));
                    BarcodeProduct bp = itemList.get(position).getBarcodeProduct();
                    initItem(holder, position, bp);
                }
            });
        }

        //favButton
        holder.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if not in catalog say can't favorite
                if(itemList.get(position).getBarcodeProduct() != null && !Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                    Utils.createToast(holder.itemView.getContext(), "Cannot favorite an item not in catalog list.", Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                    return;
                }
                //else adjust icon
                if ((Boolean) holder.favoriteButton.getTag()){
                    holder.favoriteButton.setImageResource(R.drawable.ic_empty_heart_24dp);
                    holder.favoriteButton.setTag(Boolean.FALSE);
                }else{
                    holder.favoriteButton.setImageResource(R.drawable.ic_filled_heart_24dp);
                    holder.favoriteButton.setTag(Boolean.TRUE);
                }
                //update in collection, prompt on fail
                if(itemList.get(position).getBarcodeProduct() != null && Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                    db.document(itemList.get(position).getBarcodeProduct().getCatalogReference()).update("favorite", holder.favoriteButton.getTag())
                            .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //toast
                            Utils.createToast(holder.itemView.getContext(), "Could not favorite item, Please try again.",
                                                    Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);

                            boolean isFav = (boolean) holder.favoriteButton.getTag();
                            holder.favoriteButton.setImageResource(!isFav ? R.drawable.ic_filled_heart_24dp : R.drawable.ic_empty_heart_24dp);
                            holder.favoriteButton.setTag(!isFav ? Boolean.TRUE : Boolean.FALSE);
                        }
                    });
                }
            }
        });

        // incrementing the quantity
        holder.incButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // increment on the UI
                currentItem.incTvFridgeItemQuantity();
                holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());

                //final String[] docId = new String[1];

                // find the item thats quantity is being updated
                fridgeListRef.document(currentItem.getDocID()).update("quantity", Integer.parseInt(currentItem.getTvFridgeItemQuantity()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // item is not suggested
                        if(itemList.get(position).getBarcodeProduct() != null && Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                            db.document(itemList.get(position).getBarcodeProduct().getCatalogReference()).update("suggested", false);
                        }
                    }
                });
            }
        });


        // decrementing the quantity
        holder.decButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // suggest items that have been decremented to 1 and are NOT favorites
                if (currentItem.getTvFridgeItemQuantity().contentEquals("2")) {
                    if(itemList.get(position).getBarcodeProduct() != null && Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                        fav = (boolean) holder.favoriteButton.getTag();
                        if (!fav) { db.document(itemList.get(position).getBarcodeProduct().getCatalogReference()).update("suggested", true); }
                    }
                }
                // cannot decrement to 0
                if (!currentItem.getTvFridgeItemQuantity().contentEquals("1")) {
                    currentItem.decTvFridgeItemQuantity();
                    holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());

                    //final String[] docId = new String[1];

                    // find the item thats quantity is being updated
                    fridgeListRef.document(currentItem.getDocID()).update("quantity", Integer.parseInt(currentItem.getTvFridgeItemQuantity()));

                } else { // remove item from fridgeList when quantity reaches zero
                    //get fav boolean and make item suggested if not a fav
                    if(itemList.get(position).getBarcodeProduct() != null && Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                        fav = (boolean) holder.favoriteButton.getTag();
                        if (!fav) { db.document(itemList.get(position).getBarcodeProduct().getCatalogReference()).update("suggested", true); }
                    }
                    if(fav){
                        //automatically add item to shopping list
                        addFavToShopping(currentItem, view);

                    }else {
                        Intent intent = new Intent(view.getContext(), AddFridgeToShopping.class);
                        Bundle b = new Bundle();
                        b.putString("itemName", currentItem.getTvFridgeItemName()); // add item name
                        intent.putExtras(b); // associate name with intent
                        view.getContext().startActivity(intent);
                    }

                    // remove item from the fridge
                    //final String[] docId = new String[1];
                    fridgeListRef.document(currentItem.getDocID()).delete();

                }
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
                    i.putExtra("currCollection", "fridgeList");
                    i.putExtra("docID", currentItem.getFridgeItemRef().getPath());
                    c.startActivity(i);
                }
            }
        });
    }

    private void initItem(final ItemViewHolder holder, int position, BarcodeProduct bp) {
        setProductImage(holder, itemList.get(position).getBarcodeProduct());
        //listens for updates to the doc with the favorite field
        if(Utils.isNotNullOrEmpty(bp.getCatalogReference())){
            db.document(bp.getCatalogReference()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null){
                        holder.catalogRefBP = value.toObject(BarcodeProduct.class);
                        if (holder.catalogRefBP != null && Utils.isNotNullOrEmpty(holder.catalogRefBP.getFavorite())) {
                            holder.favoriteButton.setImageResource(holder.catalogRefBP.getFavorite() ? R.drawable.ic_filled_heart_24dp : R.drawable.ic_empty_heart_24dp);
                            holder.favoriteButton.setTag(holder.catalogRefBP.getFavorite() ? Boolean.TRUE : Boolean.FALSE);
                        }
                    }
                }
            });
        } else{
            Log.d(TAG, "This fridge item is not in catalog list! Won't be able to favorite");
        }
    }

    private void setProductImage(final ItemViewHolder holder, BarcodeProduct bp) {
        if(Utils.isNotNullOrEmpty(bp.getUserImage())){
            //load image
            StorageReference imageStorage = storage.getReference("images/"+ user.getUid()+bp.getName().toLowerCase());
            final long OM = 5000 * 500000000L;
            imageStorage.getBytes(OM).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    holder.itemImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
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

    public void addFavToShopping(FridgeItem currentItem, final View view){
        final String itemName = currentItem.getTvFridgeItemName();

        shoppingListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean itemNotExists = true;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.get("name").toString().toLowerCase().equals(itemName.toLowerCase())) {
                        Utils.createToast(view.getContext(), itemName + " is already in the Shopping List", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                        itemNotExists = false;
                    }
                }
                if (itemNotExists) {
                    Map<String, Object> shoppingListMap = new HashMap<>();
                    shoppingListMap.put("name", itemName);
                    shoppingListMap.put("quantity", 1);
                    shoppingListMap.put("checked", false);
                    shoppingListRef.add(shoppingListMap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "onSuccess: " + itemName + " added.");
                                    Utils.createToast(view.getContext(), itemName +" added to Shopping List", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: ", e);
                                }
                            });
                }
            }
        }
    });
    }



}
