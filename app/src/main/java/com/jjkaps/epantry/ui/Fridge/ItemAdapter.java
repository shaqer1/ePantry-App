package com.jjkaps.epantry.ui.Fridge;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.mlkit.vision.barcode.Barcode;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.ui.ItemUI.AddFridgeToShopping;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

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

    public class ItemViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
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
                if ((Boolean) holder.favoriteButton.getTag()){
                    holder.favoriteButton.setImageResource(R.drawable.ic_empty_heart_24dp);
                    holder.favoriteButton.setTag(Boolean.FALSE);
                }else{
                    holder.favoriteButton.setImageResource(R.drawable.ic_filled_heart_24dp);
                    holder.favoriteButton.setTag(Boolean.TRUE);
                }
                if(itemList.get(position).getBarcodeProduct() != null && Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                    db.document(itemList.get(position).getBarcodeProduct().getCatalogReference()).update("favorite", holder.favoriteButton.getTag())
                            .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //toast
                            Toast.makeText(holder.itemView.getContext(), "Could not favorite item, Please try again.", Toast.LENGTH_LONG).show();
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

                final String[] docId = new String[1];

                // find the item thats quantity is being updated
                fridgeListRef.whereEqualTo("name", currentItem.getTvFridgeItemName())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult() != null && task.getResult().size() != 0) {
                                        docId[0] = task.getResult().getDocuments().get(0).getId(); // this identifies the document we want to change

                                        // update this document's quantity
                                        db.collection("users").document(uid).collection("fridgeList").document(docId[0])
                                                .update("quantity", Integer.parseInt(currentItem.getTvFridgeItemQuantity()));
                                    }
                                }
                            }
                        });
            }
        });

        // decrementing the quantity
        holder.decButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cannot decrement below 0
                if (!currentItem.getTvFridgeItemQuantity().contentEquals("1")) {
                    currentItem.decTvFridgeItemQuantity();
                    holder.tvItemQuantity.setText(currentItem.getTvFridgeItemQuantity());

                    final String[] docId = new String[1];

                    // find the item thats quantity is being updated
                    fridgeListRef.whereEqualTo("name", currentItem.getTvFridgeItemName())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult() != null && task.getResult().size() != 0) {
                                            docId[0] = task.getResult().getDocuments().get(0).getId(); // this identifies the document we want to change

                                            // update this document's quantity
                                            db.collection("users").document(uid).collection("fridgeList").document(docId[0])
                                                    .update("quantity", Integer.parseInt(currentItem.getTvFridgeItemQuantity()));
                                        }
                                    }
                                }
                            });
                } else { // remove item from fridgeList when quantity reaches zero
                    //get fav boolean
                    if(itemList.get(position).getBarcodeProduct() != null && Utils.isNotNullOrEmpty(itemList.get(position).getBarcodeProduct().getCatalogReference())){
                        fav = (boolean) holder.favoriteButton.getTag();

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
                    final String[] docId = new String[1];
                    fridgeListRef.whereEqualTo("name", currentItem.getTvFridgeItemName())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult() != null && task.getResult().size() != 0) {
                                            docId[0] = task.getResult().getDocuments().get(0).getId(); // this identifies the document we want to delete

                                            // delete from the database
                                            db.collection("users").document(uid).collection("fridgeList").document(docId[0])
                                                    .delete();
                                        }
                                    }
                                }
                            });
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
            holder.itemImage.setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.image_not_found, null));
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


    public void onItemClick(View view, int position) {
        Log.i("TAG", "####You clicked number " + getItem(position) + ", which is at cell position " + position);
        //Toast.makeText(view.getContext(), "Item can't be null!", Toast.LENGTH_SHORT).show();
    }

    public void addFavToShopping(FridgeItem currentItem, final View view){
        final String itemName = currentItem.getTvFridgeItemName();

        shoppingListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                boolean itemNotExists = true;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.get("name").toString().toLowerCase().equals(itemName.toLowerCase())) {
                        Toast toast = Toast.makeText(view.getContext(), itemName + " is already in the Shopping List", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();

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
                                     Toast toast = Toast.makeText(view.getContext(), itemName +" added to Shopping List", Toast.LENGTH_SHORT);
                                     toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                     toast.show();
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
