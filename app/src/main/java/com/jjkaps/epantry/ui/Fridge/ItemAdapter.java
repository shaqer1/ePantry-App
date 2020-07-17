package com.jjkaps.epantry.ui.Fridge;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private static final String TAG = "ItemAdapter";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");

    private ArrayList<FridgeItem> itemList;
    //private final AdapterView.OnItemClickListener incListener;
    private ItemClickListener mClickListener;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseStorage storage = FirebaseStorage.getInstance();;

    public class ItemViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        public TextView tvItemName;
        public TextView tvItemQuantity;
        public TextView tvItemNotes;
        public TextView tvExpDate;
        private Button incButton;
        private Button decButton;
        private ImageView itemImage;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_fridgeItem);
            tvItemQuantity = itemView.findViewById(R.id.tv_fridgeItemQuantity);
            tvItemNotes = itemView.findViewById(R.id.tv_notes);
            tvExpDate = itemView.findViewById(R.id.tv_expdate);
            incButton = itemView.findViewById(R.id.btn_inc);
            decButton = itemView.findViewById(R.id.btn_dec);
            itemImage = itemView.findViewById(R.id.tv_fridgeImage);
            itemView.setOnClickListener(this);
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
        //load image
        StorageReference imageStorage = storage.getReference("images/"+ user.getUid()+currentItem.getTvFridgeItemName().toLowerCase());
        final long OM = 5000 * 500000000;
        imageStorage.getBytes(OM).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    holder.itemImage.setImageBitmap(bitmap.createScaledBitmap(bitmap, 100, 100, false));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


        Log.d(TAG, "onBindViewHolder: quantity: "+currentItem.getTvFridgeItemQuantity());
        if(itemList.get(position).getBarcodeProduct() != null){
            setProductImage(holder, itemList.get(position).getBarcodeProduct());
        }else{
            itemList.get(position).getFridgeItemRef().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if(value != null){
                        itemList.get(position).setBarcodeProduct(value.toObject(BarcodeProduct.class));
                        if(itemList.get(position).getBarcodeProduct() != null) {
                            setProductImage(holder, itemList.get(position).getBarcodeProduct());
                        }
                    }
                }
            });
        }

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
                    // "do you wish to remove this item?"
                    Intent intent = new Intent(view.getContext(), AddFridgeToShopping.class);
                    Bundle b = new Bundle();
                    b.putString("itemName", currentItem.getTvFridgeItemName()); // add item name
                    intent.putExtras(b); // associate name with intent
                    view.getContext().startActivity(intent);

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

                                            // remove from the recyclerViewer
                                            /*itemList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, itemList.size());
                                            notifyDataSetChanged();*/ //Activity is destroyed dont need this now
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

    private void setProductImage(ItemViewHolder holder, BarcodeProduct bp) {
        if(Utils.isNotNullOrEmpty(bp.getFrontPhoto()) && Utils.isNotNullOrEmpty(bp.getFrontPhoto().getThumb())){
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


}
