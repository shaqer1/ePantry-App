package com.jjkaps.epantry.ui.Recipes;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.utils.Utils;

import java.util.Objects;

public class RecipesFragment extends Fragment {

    private static final String TAG = "CatalogFragment";
    private TextView txt_empty;
    private View root;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_recipe, container, false);

        Utils.hideKeyboard(root.getContext());

        txt_empty = root.findViewById(R.id.txt_emptyList);

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        customActionBar(context);
    }

    private void customActionBar(Context c) {
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.title_recipes);
        }
    }


}