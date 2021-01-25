package com.jjkaps.epantry.utils.addTVs;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.TVData.TextViewData;
import com.jjkaps.epantry.utils.Utils;

import java.util.ArrayList;

public class AddTVItems extends AppCompatActivity {
    private TextView txtClose;
    private Button btDone;
    private ArrayList<TextViewData> itemNames;
    private MaterialTextView titleText;
    private String titleName;
    private RecyclerView tvRecV;
    private RecVTVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nutrient_add_activity);

        initView();


        Intent intent = getIntent();
        Bundle nameB = intent.getExtras();
        Bundle bundle = intent.getBundleExtra("bundle");
        if (nameB != null && bundle != null) {
            itemNames = bundle.getParcelableArrayList("textViews");
            titleName = nameB.getString("title");
            titleText.setText(titleName);
            adapter = new RecVTVAdapter(this, itemNames);
            tvRecV.setLayoutManager(new LinearLayoutManager(this));
            tvRecV.setAdapter(adapter);
        }else{
            Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Error getting details", Utils.StatusCodes.FAILURE);
            finish();
        }

        btDone.setOnClickListener(v -> {
            //TODO validation checks TEST?
            if(adapter.getResponse()!=null){
                itemNames = adapter.getResponse();
                Intent i = new Intent();
                Bundle b = new Bundle();
                b.putParcelableArrayList("result", itemNames);
                i.putExtra("bundle", b);
                setResult(RESULT_OK, i);
                finish();

            }
        });

        txtClose.setOnClickListener(v -> finish());
    }

    private void initView() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = (int) (dm.heightPixels - (dm.heightPixels*0.15));
        getWindow().setLayout(width, height);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);

        txtClose = findViewById(R.id.txt_close);
        btDone = findViewById(R.id.bt_done);
        titleText = findViewById(R.id.title);
        tvRecV = findViewById(R.id.tv_rcv);
    }
}
