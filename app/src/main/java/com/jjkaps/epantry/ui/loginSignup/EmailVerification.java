package com.jjkaps.epantry.ui.loginSignup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.Tutorial.TutorialActivity;
import com.jjkaps.epantry.utils.Utils;

public class EmailVerification extends AppCompatActivity {

    private static final String TAG = "EmailVerification";
    private Button resendButton;
    private FirebaseAuth mAuth;
    private Handler handler = new Handler();

    private Runnable verificationRunnable = new Runnable() {
        @Override
        public void run() {
            // reload user and check if email verified
            final FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.reload().addOnSuccessListener(aVoid -> {
                    if(user.isEmailVerified()){
                        Intent mainIntent = new Intent(EmailVerification.this, TutorialActivity.class);
                        EmailVerification.this.startActivity(mainIntent);
                        EmailVerification.this.finish();
                    }
                });
            } else {
                Utils.createToast(EmailVerification.this, "Could not find user, Please Login again.", Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                Intent mainIntent = new Intent(EmailVerification.this, LoginActivity.class);
                EmailVerification.this.startActivity(mainIntent);
                EmailVerification.this.finish();
            }
            handler.postDelayed(this,1000); // This time is in millis.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        mAuth = FirebaseAuth.getInstance();
        TextView returnLgn = findViewById(R.id.return_login);
        returnLgn.setOnClickListener(view -> {
            mAuth.signOut();
            handler.removeCallbacks(verificationRunnable);
            Intent mainIntent = new Intent(EmailVerification.this, LoginActivity.class);
            EmailVerification.this.startActivity(mainIntent);
            EmailVerification.this.finish();
        });

        resendButton = findViewById(R.id.resend_button);

        resendButton.setOnClickListener(view -> {
            resendButton.setEnabled(false);
            sendVerifyEmail();
        });

        sendVerifyEmail();
        handler.postDelayed(verificationRunnable, 500);
    }

    private void sendVerifyEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                resendButton.setEnabled(true);
                if(task.isSuccessful()){
                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, findViewById(R.id.container), "Verification email sent to " + user.getEmail(), Utils.StatusCodes.SUCCESS);
                } else {
                    Log.e(TAG, "sendEmailVerification", task.getException());
                    Utils.createStatusMessage(Snackbar.LENGTH_LONG, findViewById(R.id.container), "Failed to send verification email." +
                            (task.getException() != null ? task.getException().getMessage(): "Unknown Error Occurred.") + " Please try again", Utils.StatusCodes.FAILURE);
                }
            });
        } else {
            Utils.createToast(EmailVerification.this, "Could not find user, Please Login again.", Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
            Intent mainIntent = new Intent(EmailVerification.this, LoginActivity.class);
            EmailVerification.this.startActivity(mainIntent);
            EmailVerification.this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(verificationRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(verificationRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(verificationRunnable);
    }
}