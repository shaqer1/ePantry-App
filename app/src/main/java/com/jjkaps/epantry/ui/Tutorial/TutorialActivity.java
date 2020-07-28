package com.jjkaps.epantry.ui.Tutorial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;

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
        slideModels.add(new SlideModel("https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Un1.svg/1200px-Un1.svg.png", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel("https://i.redd.it/1xfefhlbk7u21.png", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel("https://s3.amazonaws.com/tinycards/image/d8a6c3a4abdd2935828d078c06a61655", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel("https://blognumbers.files.wordpress.com/2010/09/4.jpgasdwqdqw", ScaleTypes.CENTER_CROP));
        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);


    }

    @Override
    public boolean onSupportNavigateUp(){
        Intent mainIntent = new Intent(TutorialActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
        return true;
    }
}