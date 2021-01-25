package com.jjkaps.epantry.ui.ItemUI.ExpListView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.ItemUI.ItemActivity;
import com.jjkaps.epantry.ui.ItemUI.NutrientUI.CustomRecyclerView;
import com.jjkaps.epantry.ui.ItemUI.NutrientUI.NutrientRecyclerAdapter;
import com.jjkaps.epantry.ui.Recipes.BPAdapterItem;
import com.jjkaps.epantry.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class CustomItemExpViewAdapter extends BaseExpandableListAdapter {
    private static final int PICK_IMAGE_REQUEST = 5;
    private static final int ADD_NUTRIENT_REQUEST = 75;
    private final Context c;
    private final List<String> itemsTitle = Arrays.asList("Ingredients", "PackageDetails", "Nutrients", "PackageServing");
    private final FirebaseFirestore db;
    private final View parentView;
    private final BPAdapterItem bpAdapterItem;
    private final RelativeLayout imageRL;
    private final TextView progText;
    private final List<String> storageOptions = Arrays.asList("Fridge", "Freezer", "Pantry");
    private Chip veganChip, vegChip, glutenChip;
    private EditText ingredientsTV, palmOilIngredTV;
    private EditText notesET, pkgSizeTV, pkgQtyTV, srvSizeTV, srvUnitTV;
    private final HashMap<Integer, View> editTextMap;
    private AutoCompleteTextView storgaeDropdown;
    private ImageView nutImageIV;
    private Button nutImageEditBut, resetImageBut;
    private CustomRecyclerView nutGridRecV;
    private boolean changed;
    private NutrientRecyclerAdapter nutGridAdapter;


    public CustomItemExpViewAdapter(Context c, View parentView, BPAdapterItem bpAdapterItem, RelativeLayout imageRL, TextView progText) {
        this.c = c;
        this.parentView = parentView;
        this.bpAdapterItem = bpAdapterItem;
        this.imageRL = imageRL;
        this.progText = progText;
        this.db = FirebaseFirestore.getInstance();
        changed = false;
        editTextMap = new HashMap<>();
    }

    public BPAdapterItem getBpAdapterItem() {
        return bpAdapterItem;
    }

    public HashMap<Integer, View> getEditTextMap() {
        return editTextMap;
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
        return true;
    }

    @Override
    public int getChildTypeCount() {
        return itemsTitle.size();
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return groupPosition;
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
        listTitleTextView.setOnClickListener(view -> {
            ExpandableListView expandableListView = (ExpandableListView) viewGroup;
            if (!b) {
                expandableListView.expandGroup(listPosition);
            }
            else {
                expandableListView.collapseGroup(listPosition);
            }
        });
        return convertView;
    }

    @Override
    public View getChildView(int listPosition, int expandedListPosition, boolean b, View convertView, ViewGroup viewGroup) {
        //TODO
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch ((String) getGroup(listPosition)){
                case "Ingredients"://TODO listener
                    convertView = layoutInflater.inflate(R.layout.item_ingredients_exp, viewGroup, false);
                    //process Ingredients
                    //ingred
                    ingredientsTV = convertView.findViewById(R.id.item_ingred);
                    editTextMap.put(ingredientsTV.getId(), ingredientsTV);
                    ingredientsTV.setScroller(new Scroller(c));
                    ingredientsTV.setVerticalScrollBarEnabled(true);
                    View finalConvertView = convertView;
                    ingredientsTV.setOnFocusChangeListener((view, b1) -> {
                        if(!b1){
                            // if notes changed
                            ingredientsTV = finalConvertView.findViewById(R.id.item_ingred);
                            if(Utils.isNotNullOrEmpty(ingredientsTV.getText())
                                    && !ingredientsTV.getText().toString().trim().equals(bpAdapterItem.getBarcodeProduct().getIngredients())){
                                bpAdapterItem.getBarcodeProduct().setIngredients(ingredientsTV.getText().toString().trim());
                                changed = true;
                            }
                        }
                    });
                    palmOilIngredTV = convertView.findViewById(R.id.palm_oil_ingr);
                    editTextMap.put(palmOilIngredTV.getId(), palmOilIngredTV);
                    palmOilIngredTV.setOnFocusChangeListener((view, b1) -> {
                        if(!b1){
                            // if notes changed
                            palmOilIngredTV = finalConvertView.findViewById(R.id.palm_oil_ingr);
                            if(Utils.isNotNullOrEmpty(palmOilIngredTV.getText())
                                    && !palmOilIngredTV.getText().toString().trim().equals(Utils.getStringArr(bpAdapterItem.getBarcodeProduct().getPalm_oil_ingredients()))){
                                bpAdapterItem.getBarcodeProduct().setPalm_oil_ingredients(Arrays.asList(palmOilIngredTV.getText().toString().split(", ")));
                                changed = true;
                            }
                        }
                    });
                    //diet
                    veganChip = convertView.findViewById(R.id.vegan_chip);
                    veganChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if(isChecked != bpAdapterItem.getBarcodeProduct().getDietInfo().getVegan().isIs_compatible()){
                            bpAdapterItem.getBarcodeProduct().getDietInfo().getVegan().setIs_compatible(isChecked);
                            changed = true;
                        }
                    });
                    vegChip = convertView.findViewById(R.id.veg_chip);
                    vegChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if(isChecked != bpAdapterItem.getBarcodeProduct().getDietInfo().getVeg().isIs_compatible()){
                            bpAdapterItem.getBarcodeProduct().getDietInfo().getVeg().setIs_compatible(isChecked);
                            changed = true;
                        }
                    });
                    glutenChip = convertView.findViewById(R.id.gluten_chip);
                    glutenChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if(isChecked != bpAdapterItem.getBarcodeProduct().getDietInfo().getGluten_free().isIs_compatible()){
                            bpAdapterItem.getBarcodeProduct().getDietInfo().getGluten_free().setIs_compatible(isChecked);
                            changed = true;
                        }
                    });
                    /*ingredients*/
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getIngredients())){
                        ingredientsTV.setText(bpAdapterItem.getBarcodeProduct().getIngredients());
                    }
                    /*palm oil chip*/
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getPalm_oil_ingredients())
                            && bpAdapterItem.getBarcodeProduct().getPalm_oil_ingredients().size() > 0){
                        palmOilIngredTV.setText(Utils.getStringArr(bpAdapterItem.getBarcodeProduct().getPalm_oil_ingredients()));
                    }
                    /*gluten chip*/
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getDietInfo().getGluten_free())){
                        if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getDietInfo().getGluten_free().isIs_compatible())){
                            glutenChip.setChecked(bpAdapterItem.getBarcodeProduct().getDietInfo().getGluten_free().isIs_compatible());
                        }
                    }
                    /*vegetarian chip*/
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getDietInfo().getVeg())){
                        if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getDietInfo().getVeg().isIs_compatible())){
                            vegChip.setChecked(bpAdapterItem.getBarcodeProduct().getDietInfo().getVeg().isIs_compatible());
                        }
                    }
                    /*vegan chip*/
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getDietInfo().getVegan())){
                        if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getDietInfo().getVegan().isIs_compatible())){
                            veganChip.setChecked(bpAdapterItem.getBarcodeProduct().getDietInfo().getVegan().isIs_compatible());
                        }
                    }
                    break;
                case "PackageDetails":
                    convertView = layoutInflater.inflate(R.layout.misc_details, viewGroup, false);
                    storgaeDropdown = convertView.findViewById(R.id.filled_exposed_dropdown);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(c, R.layout.dropdown_menu, storageOptions);
                    storgaeDropdown.setAdapter(adapter);
                    storgaeDropdown.setInputType(InputType.TYPE_NULL);
                    storgaeDropdown.setOnItemClickListener((adapterView, view, i, l) -> {
                        // if storage location changed
                        if (!storgaeDropdown.getAdapter().getItem(i).equals(bpAdapterItem.getBarcodeProduct().getStorageType())){
                            bpAdapterItem.getBarcodeProduct().setStorageType(storgaeDropdown.getText().toString().trim());
                            changed = true;
                        }
                    });
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getStorageType())){
                        storgaeDropdown.setText(bpAdapterItem.getBarcodeProduct().getStorageType(), false);
                    }
                    notesET = convertView.findViewById(R.id.item_notesA);
                    View finalConvertView1 = convertView;
                    notesET.setOnFocusChangeListener((view, b1) -> {
                        if(!b1){
                            // if notes changed
                            notesET = finalConvertView1.findViewById(R.id.item_notesA);
                            if(Utils.isNotNullOrEmpty(notesET.getText().toString()) && !notesET.getText().toString().trim().equals(bpAdapterItem.getBarcodeProduct().getNotes())){
                                bpAdapterItem.getBarcodeProduct().setNotes(notesET.getText().toString().trim());
                                changed = true;
                            }
                        }

                    });
                    //editTextMap.put(notesET.getId(), notesET);
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getNotes())){
                        notesET.setText(bpAdapterItem.getBarcodeProduct().getNotes());
                    }
                    break;
                case "Nutrients"://TODO listener
                    convertView = layoutInflater.inflate(R.layout.nutrients_detail, viewGroup, false);
                    nutImageIV = convertView.findViewById(R.id.item_nut_image);
                    nutImageEditBut = convertView.findViewById(R.id.editImageNutBT);
                    nutImageEditBut.setOnClickListener(v -> {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        ((Activity) c).startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                    });
                    resetImageBut = convertView.findViewById(R.id.resetImageNutBT);
                    resetImageBut.setOnClickListener(view -> {
                        bpAdapterItem.getBarcodeProduct().getNutritionPhoto().setCustomImage(false);
                        db.document(bpAdapterItem.getDocReference()).update("nutritionPhoto", bpAdapterItem.getBarcodeProduct().getNutritionPhoto());
                    });
                    nutGridRecV = convertView.findViewById(R.id.grid_nut);
                    nutGridRecV.setLayoutManager(new GridLayoutManager(c, 2));
                    nutGridRecV.setExpanded(true);
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getNutritionPhoto()) && Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getNutritionPhoto().getDisplay())){
                        Picasso.get().load(bpAdapterItem.getBarcodeProduct().getNutritionPhoto().getDisplay()).into(nutImageIV);
                    }else if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getNutritionPhoto()) && bpAdapterItem.getBarcodeProduct().isCustImage()){
                        //load image
                        StorageReference imageStorage = FirebaseStorage.getInstance().getReference(bpAdapterItem.getBarcodeProduct().getNutritionPhoto().getUserImage());
                        final long OM = 5000 * 500000000L;
                        imageStorage.getBytes(OM).addOnSuccessListener(bytes -> {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            nutImageIV.setImageBitmap(bitmap);
                        }).addOnFailureListener(e ->
                                Utils.createStatusMessage(Snackbar.LENGTH_LONG, CustomItemExpViewAdapter.this.parentView, "Could not load image", Utils.StatusCodes.FAILURE)
                        );
                    }else{
                        nutImageIV.setImageResource(R.drawable.image_not_found);
                    }
                    //if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getNutrients()) && bpAdapterItem.getBarcodeProduct().getNutrients().size()>0){
                        nutGridAdapter = new NutrientRecyclerAdapter(c, (ItemActivity) c, bpAdapterItem.getBarcodeProduct(), nutGridRecV);
                        nutGridRecV.setAdapter(nutGridAdapter);
                    //}
                    break;
                case "PackageServing"://TODO test listener
                    convertView = layoutInflater.inflate(R.layout.package_serving_det, viewGroup, false);
                    //pkg
                    pkgSizeTV = convertView.findViewById(R.id.item_pkg_size);
                    editTextMap.put(pkgSizeTV.getId(), pkgSizeTV);
                    View finalConvertView2 = convertView;
                    pkgSizeTV.setOnFocusChangeListener((v, hasFocus) -> {
                        if(!hasFocus){
                            // if notes changed
                            if(Utils.isNotNullOrEmpty(pkgSizeTV.getText().toString())
                                    && !pkgSizeTV.getText().toString().trim().equals(bpAdapterItem.getBarcodeProduct().getPackageDetails().getSize())
                                    && Utils.isNotNullOrEmpty(pkgQtyTV.getText().toString())){
                                pkgSizeTV = finalConvertView2.findViewById(R.id.item_pkg_size);
                                bpAdapterItem.getBarcodeProduct().getPackageDetails().setSize(pkgSizeTV.getText().toString());
                                changed = true;
                            }else if(Utils.isNotNullOrEmpty(pkgSizeTV.getText().toString())
                                    && !pkgSizeTV.getText().toString().trim().equals(bpAdapterItem.getBarcodeProduct().getPackageDetails().getSize())
                                    &&!Utils.isNotNullOrEmpty(pkgQtyTV.getText().toString())){
                                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, CustomItemExpViewAdapter.this.parentView, "Package quantity cannot be empty", Utils.StatusCodes.FAILURE);
                            }
                        }
                    });
                    pkgQtyTV = convertView.findViewById(R.id.item_pkg_qty);
                    editTextMap.put(pkgQtyTV.getId(), pkgQtyTV);
                    pkgQtyTV.setOnFocusChangeListener((v, hasFocus) -> {
                        if(!hasFocus){
                            // if notes changed
                            pkgQtyTV = finalConvertView2.findViewById(R.id.item_pkg_qty);
                            if(Utils.isNotNullOrEmpty(pkgQtyTV.getText().toString())
                                    && !pkgQtyTV.getText().toString().trim().equals(""+bpAdapterItem.getBarcodeProduct().getPackageDetails().getQuantity())
                                    && Utils.isNotNullOrEmpty(pkgSizeTV.getText().toString())){
                                bpAdapterItem.getBarcodeProduct().getPackageDetails().setQuantity(Integer.parseInt(pkgQtyTV.getText().toString()));
                                changed = true;
                            }else if(Utils.isNotNullOrEmpty(pkgQtyTV.getText().toString())
                                    && !pkgQtyTV.getText().toString().trim().equals(""+bpAdapterItem.getBarcodeProduct().getPackageDetails().getQuantity())
                                    && !Utils.isNotNullOrEmpty(pkgSizeTV.getText().toString())){
                                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, CustomItemExpViewAdapter.this.parentView, "Package size cannot be empty", Utils.StatusCodes.FAILURE);
                            }
                        }
                    });
                    //serving
                    srvSizeTV = convertView.findViewById(R.id.item_srv_sze);
                    editTextMap.put(srvSizeTV.getId(), srvSizeTV);
                    srvSizeTV.setOnFocusChangeListener((v, b1) -> {
                        if(!b1){
                            // if notes changed
                            srvSizeTV = finalConvertView2.findViewById(R.id.item_srv_sze);
                            if(Utils.isNotNullOrEmpty(srvSizeTV.getText().toString())
                                    && !srvSizeTV.getText().toString().trim().equals(bpAdapterItem.getBarcodeProduct().getServing().getSize())
                                    && (srvUnitTV.getVisibility() == View.VISIBLE && Utils.isNotNullOrEmpty(srvUnitTV.getText().toString()))){
                                bpAdapterItem.getBarcodeProduct().getServing().setSize(srvUnitTV.getText().toString());
                                changed = true;
                            }else if(Utils.isNotNullOrEmpty(srvSizeTV.getText().toString())
                                    && !srvSizeTV.getText().toString().trim().equals(bpAdapterItem.getBarcodeProduct().getServing().getSize())
                                    && !Utils.isNotNullOrEmpty(srvUnitTV.getText().toString())){
                                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, CustomItemExpViewAdapter.this.parentView, "Serving Unit cannot be empty", Utils.StatusCodes.FAILURE);
                            }
                        }
                    });
                    srvUnitTV = convertView.findViewById(R.id.item_srv_unit);
                    editTextMap.put(srvUnitTV.getId(), srvUnitTV);
                    srvUnitTV.setOnFocusChangeListener((v, b2)->{
                        if(!b2){
                            // if notes changed
                            srvUnitTV = finalConvertView2.findViewById(R.id.item_srv_unit);
                            if(Utils.isNotNullOrEmpty(srvUnitTV.getText().toString())
                                    && !srvUnitTV.getText().toString().trim().equals(bpAdapterItem.getBarcodeProduct().getServing().getMeasurement_unit())
                                    && Utils.isNotNullOrEmpty(srvSizeTV.getText().toString())){
                                bpAdapterItem.getBarcodeProduct().getServing().setMeasurement_unit(srvUnitTV.getText().toString());
                                changed = true;
                            }else if(Utils.isNotNullOrEmpty(srvUnitTV.getText().toString())
                                        && !srvUnitTV.getText().toString().trim().equals(bpAdapterItem.getBarcodeProduct().getServing().getMeasurement_unit())
                                        && !Utils.isNotNullOrEmpty(srvSizeTV.getText().toString())){
                                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, CustomItemExpViewAdapter.this.parentView, "Serving size cannot be empty", Utils.StatusCodes.FAILURE);
                            }
                        }
                    });
                    /*pkg*/
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getPackageDetails())
                            && Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getPackageDetails().getQuantity())
                            && Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getPackageDetails().getSize())){
                        pkgQtyTV.setText(String.valueOf(bpAdapterItem.getBarcodeProduct().getPackageDetails().getQuantity()));
                        pkgSizeTV.setText(bpAdapterItem.getBarcodeProduct().getPackageDetails().getSize());
                    }
                    /*Serving*/
                    if(Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getServing())
                            && Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getServing().getSize())
                            && Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getServing().getMeasurement_unit())) {
                        srvSizeTV.setText(bpAdapterItem.getBarcodeProduct().getServing().getSize());
                        srvUnitTV.setText(bpAdapterItem.getBarcodeProduct().getServing().getMeasurement_unit());
                    }else if (Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getServing()) && Utils.isNotNullOrEmpty(bpAdapterItem.getBarcodeProduct().getServing().getSize_fulltext())){
                        srvSizeTV.setText(bpAdapterItem.getBarcodeProduct().getServing().getSize_fulltext());
                        convertView.findViewById(R.id.item_srv_unit).setVisibility(View.GONE);
                    }
                    break;
                default:
            }
        }
        return convertView;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data, @NonNull  FirebaseUser user) {
        if(requestCode == PICK_IMAGE_REQUEST && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                uploadImage(data.getData(), user);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(c.getContentResolver(), filePath);
                nutImageIV.setImageBitmap(bitmap);
                changed = true;
            } catch (IOException e){
                Utils.createStatusMessage(Snackbar.LENGTH_SHORT, CustomItemExpViewAdapter.this.parentView, "Could not parse image as bitmap.", Utils.StatusCodes.FAILURE);
            }
        }else if (requestCode == ADD_NUTRIENT_REQUEST){
            nutGridAdapter.onActivityResult(requestCode, resultCode, data, user);
            if(resultCode == RESULT_OK){
                nutGridRecV.setAdapter(nutGridAdapter);
                nutGridAdapter.notifyDataSetChanged();
                //nutGridAdapter.notifyItemRangeChanged(bpAdapterItem.getBarcodeProduct().getNutrients().size(), bpAdapterItem.getBarcodeProduct().getNutrients().size()+1);
            }
        }else{
            Utils.createToast(c, "Invalid Request, please try again", Toast.LENGTH_LONG);
        }
    }

    private void uploadImage(Uri filePath, @NonNull FirebaseUser user) {
        if(filePath != null){
            imageRL.setVisibility(View.VISIBLE);
            String imageURI = "images/Nutrient-"+ user.getUid()+ "-"+bpAdapterItem.getBarcodeProduct().getName();
            FirebaseStorage.getInstance().getReference()
                    .child(imageURI)
                    .putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, CustomItemExpViewAdapter.this.parentView, "Image Uploaded Successfully!", Utils.StatusCodes.SUCCESS);
                        imageRL.setVisibility(View.GONE);
                        bpAdapterItem.getBarcodeProduct().getNutritionPhoto().setUserImage(imageURI);
                        bpAdapterItem.getBarcodeProduct().getNutritionPhoto().setUserImageDateModified(Calendar.getInstance().getTime());
                        bpAdapterItem.getBarcodeProduct().getNutritionPhoto().setCustomImage(true);
                        db.document(bpAdapterItem.getDocReference()).update("nutritionPhoto", bpAdapterItem.getBarcodeProduct().getNutritionPhoto());
                        //db.document(docRef).update("userImageDateModified", bp.getUserImageDateModified());
                    }).addOnFailureListener(e -> {
                imageRL.setVisibility(View.GONE);
                Utils.createStatusMessage(Snackbar.LENGTH_LONG, CustomItemExpViewAdapter.this.parentView, "Could not upload image", Utils.StatusCodes.FAILURE);
                //nutImageIV.setImageResource(R.drawable.image_not_found);
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                progText.setText(("Uploaded "+(int)progress+"%"));
            });
        }
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    public boolean getOnClick(boolean changed){
        return changed || this.changed;
    }

    public void resetChanged(){changed = false;}
}
