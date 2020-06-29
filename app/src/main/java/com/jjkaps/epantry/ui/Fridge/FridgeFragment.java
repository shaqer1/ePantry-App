package com.jjkaps.epantry.ui.Fridge;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjkaps.epantry.R;

import java.util.ArrayList;

public class FridgeFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fridgeListRef = db.collection("users").document(uid).collection("fridgeList");

    private FridgeViewModel fridgeViewModel;
    private RecyclerView rvFridgeList;
    private RecyclerView.Adapter rvAdapter;
    private RecyclerView.LayoutManager rvLayoutManager;

    private static final String TAG = "FridgeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fridgeViewModel = new ViewModelProvider(this).get(FridgeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_fridge, container, false);

        // todo - connect to firebase and get user's actual list
        final ArrayList<FridgeItem> exampleFridgeList = new ArrayList<>();
        //exampleFridgeList.add(new FridgeItem("Bananas", "3"));
        //exampleFridgeList.add(new FridgeItem("Mustard", "1"));
        //exampleFridgeList.add(new FridgeItem("Bread", "1"));

        // retrieve fridgeList from Firebase and format
        fridgeListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> itemName = new ArrayList<>();
                ArrayList<Boolean> itemNotes = new ArrayList<>();

                rvFridgeList = root.findViewById(R.id.recyclerListFridgeList);

                // Update check status
                if (task.isSuccessful() && task.getResult().size() != 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        exampleFridgeList.add(new FridgeItem(document.get("name").toString(), document.get("quantity").toString()));
                    }
                    rvLayoutManager = new LinearLayoutManager(getActivity());
                    rvAdapter = new ItemAdapter(exampleFridgeList);

                    rvFridgeList.setLayoutManager(rvLayoutManager);
                    rvFridgeList.setAdapter(rvAdapter);

                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });

        // example
        //rvFridgeList = root.findViewById(R.id.recyclerListFridgeList);
        //rvLayoutManager = new LinearLayoutManager(getActivity());
        //rvAdapter = new ItemAdapter(exampleFridgeList);

        //rvFridgeList.setLayoutManager(rvLayoutManager);
        //irvFridgeList.setAdapter(rvAdapter);

        return root;
    }
}