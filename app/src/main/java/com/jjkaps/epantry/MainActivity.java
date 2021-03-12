package com.jjkaps.epantry;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.models.FridgeItem;
import com.jjkaps.epantry.models.LoggedInUser;
import com.jjkaps.epantry.ui.Fridge.ItemAdapter;
import com.jjkaps.epantry.ui.loginSignup.LoginActivity;
import com.jjkaps.epantry.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageButton ib, ibSort, searchButton;
    private AutoCompleteTextView searchView;
    private RecyclerView searchRecycler;
    private ItemAdapter searchAdapter;
    private CollectionReference fridgeListRef;
    private List<FridgeItem> allItems;
    private LoggedInUser loggedInuser;
    private FirebaseUser firebaseUser;
    private TextView name;
    private Button closeBT;
    private String namePlaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.getSupportActionBar() != null){
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View view = getSupportActionBar().getCustomView();
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            ib = view.findViewById(R.id.btn_update);
            ibSort = view.findViewById(R.id.secondaryUpdate);
            searchButton = view.findViewById(R.id.secondaryButtonLeft);
            searchButton.setImageResource(R.drawable.ic_search_24);
            searchButton.setVisibility(View.VISIBLE);
            name = view.findViewById(R.id.name);
            closeBT = view.findViewById(R.id.txt_close);
        }
        setContentView(R.layout.activity_main);

        searchRecycler = findViewById(R.id.search_recv);
        searchView = findViewById(R.id.search_view);
        searchAdapter = new ItemAdapter(new ArrayList<>(), searchRecycler);
        searchRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        searchRecycler.setAdapter(searchAdapter);
        searchButton.setOnClickListener(v -> {
            searchView.setVisibility(View.VISIBLE);
            searchRecycler.setVisibility(View.VISIBLE);
            //name.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            namePlaceHolder = name.getText().toString();
            name.setText("Search Items");
            closeBT.setVisibility(View.VISIBLE);
            closeBT.setOnClickListener(v1 -> hideSearchItems());
            ibSort.setVisibility(View.GONE);
            //ib.setVisibility(View.GONE);
            //actionBarLayout.setVisibility(View.GONE);
            searchView.requestFocusFromTouch();
            searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().equals("")) {
                        searchAdapter.getFilter().filter(s);
                    }else {
                        searchAdapter.getFilter().filter(s);
                        if (searchAdapter.getItemCount()==0){
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!Utils.isNotNullOrEmpty(loggedInuser.getRecentSearches())){
                        loggedInuser.setRecentSearches(new ArrayList<>());
                    }
                    if(s.toString().length()> 4 && loggedInuser.getRecentSearches().parallelStream().noneMatch(s.toString().trim()::equalsIgnoreCase)){
                        loggedInuser.getRecentSearches().add(s.toString());
                        searchView.setAdapter(new ArrayAdapter<>(MainActivity.this,
                                android.R.layout.simple_dropdown_item_1line, loggedInuser.getRecentSearches()));
                    }
                    Utils.getUserRef(firebaseUser).update("recentSearches", loggedInuser.getRecentSearches());
                }
            });
        });
        allItems = new ArrayList<>();
        //Greets user and make sure user exists in database.
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            fridgeListRef = Utils.getFridgeListRef(firebaseUser);
            Utils.getUserRef(firebaseUser).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    loggedInuser = documentSnapshot.toObject(LoggedInUser.class);
                    if (loggedInuser != null) {
                        if(Utils.isNotNullOrEmpty(loggedInuser.getRecentSearches()) && loggedInuser.getRecentSearches().size() > 0){
                            searchView.setAdapter(new ArrayAdapter<>(this,
                                    android.R.layout.simple_dropdown_item_1line, loggedInuser.getRecentSearches()));
                        }else{
                            searchView.setAdapter(new ArrayAdapter<>(this,
                                    android.R.layout.simple_dropdown_item_1line, new String [0]));
                        }
                        Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Hi, " + loggedInuser.getDisplayName() + "!", Utils.StatusCodes.MESSAGE);
                    }
                } else {
                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Hmm, couldn't find this user, please try logging-in again", Utils.StatusCodes.MESSAGE);

                    //send to main
                    Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
                    MainActivity.this.startActivity(mainIntent);
                    MainActivity.this.finish();
                }
            });
            fridgeListRef.addSnapshotListener((documents, error) -> {
                if(documents != null && !documents.isEmpty()){
                    searchAdapter.clear();
                    allItems.clear();
                    searchAdapter.notifyDataSetChanged();
                    for(QueryDocumentSnapshot document: documents){
                        BarcodeProduct bp = document.toObject(BarcodeProduct.class);
                        allItems.add(new FridgeItem(bp, document.getReference()));
                    }
                    searchAdapter.addAll(allItems);
                    searchAdapter.notifyDataSetChanged();
                    searchRecycler.setAdapter(searchAdapter);
                }
            });
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_fridge, R.id.navigation_shopping, R.id.navigation_recipes, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        /*navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                ibSort.setVisibility(View.INVISIBLE);
                ib.setVisibility(View.INVISIBLE);
                return true;
            }
        });*/
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void hideSearchItems() {
        Utils.hideKeyboard(this);
        searchView.setVisibility(View.GONE);
        searchRecycler.setVisibility(View.GONE);
        //name.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        closeBT.setVisibility(View.GONE);
        //ib.setVisibility(View.VISIBLE);
        ibSort.setVisibility(View.VISIBLE);
        name.setText(namePlaceHolder);
    }

    @Override
    public void onBackPressed() {

        if(searchView.getVisibility() == View.VISIBLE){
            hideSearchItems();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ibSort.setVisibility(View.INVISIBLE);
        ib.setVisibility(View.INVISIBLE);
    }
}