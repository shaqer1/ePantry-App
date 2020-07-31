package com.jjkaps.epantry.ui.Tutorial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.Settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {
    private ImageSlider imageSlider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        getSupportActionBar().setTitle("Tutorial");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        imageSlider = findViewById(R.id.imageSlider_tutorial);
        List<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/epantry-jjkaps.appspot.com/o/tutorial_image%2Ftutorial_fridge1.png?alt=media&token=837f29ce-b137-4946-9f70-2dff611780e4", ScaleTypes.CENTER_INSIDE));
        slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/epantry-jjkaps.appspot.com/o/tutorial_image%2Ftutorial_fridge2.png?alt=media&token=c554a80b-34d6-4e39-9a64-c79f282b006e", ScaleTypes.CENTER_INSIDE));
        slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/epantry-jjkaps.appspot.com/o/tutorial_image%2Ftutorial_shopping.png?alt=media&token=d0bb1816-e5a6-402a-98cb-526c45880b1e", ScaleTypes.CENTER_INSIDE));
        slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/epantry-jjkaps.appspot.com/o/tutorial_image%2Ftutorial_catalog.png?alt=media&token=85cfe93a-091d-4db2-9851-26dcc14de036", ScaleTypes.CENTER_INSIDE));
        slideModels.add(new SlideModel("https://firebasestorage.googleapis.com/v0/b/epantry-jjkaps.appspot.com/o/tutorial_image%2Ftutorial_settings.png?alt=media&token=9cb56358-f9a1-44c5-aa7f-d7d0f809a3f7", ScaleTypes.CENTER_INSIDE));

        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_INSIDE);


    }

    @Override
    public boolean onSupportNavigateUp(){

            Intent mainIntent = new Intent(TutorialActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();

        return true;
    }
}