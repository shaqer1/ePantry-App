package com.jjkaps.epantry.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.loginSignup.EmailVerification;
import com.jjkaps.epantry.ui.loginSignup.LoginActivity;
import com.jjkaps.epantry.ui.loginSignup.SignUpActivity;
import com.jjkaps.epantry.utils.Utils;

public class SplashActivity extends AppCompatActivity {

    public static final int SPLASH_TIME_MILLIS = 1000;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                //firebase auth check and redirect to activity based on that
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // checks if user still has a firebaseAuth account
                    user.reload().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                Log.d(TAG, "user doesn't exist anymore");
                                Utils.createToast(getBaseContext(), "User no longer exists, please sign up!", Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                                //send to signup, user no longer exists, probably deleted by admin
                                Intent mainIntent = new Intent(SplashActivity.this, SignUpActivity.class);
                                SplashActivity.this.startActivity(mainIntent);
                                SplashActivity.this.finish();
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //reload user and check if user is verified or user signed in
                            Intent mainIntent = new Intent(SplashActivity.this,  user.isEmailVerified() ? MainActivity.class : EmailVerification.class);
                            SplashActivity.this.startActivity(mainIntent);
                            SplashActivity.this.finish();
                        }
                    });
                } else {
                    // No user is signed in
                    Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                    SplashActivity.this.finish();
                }

            }
        }, SPLASH_TIME_MILLIS);
    }
}