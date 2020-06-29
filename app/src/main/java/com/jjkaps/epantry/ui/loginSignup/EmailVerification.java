package com.jjkaps.epantry.ui.loginSignup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;

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
                user.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(user.isEmailVerified()){
                            Intent mainIntent = new Intent(EmailVerification.this, MainActivity.class);
                            EmailVerification.this.startActivity(mainIntent);
                            EmailVerification.this.finish();
                        }
                    }
                });
            } else {
                Toast.makeText(EmailVerification.this, "Could not find user, Please Login again.", Toast.LENGTH_LONG).show();
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
        returnLgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                handler.removeCallbacks(verificationRunnable);
                Intent mainIntent = new Intent(EmailVerification.this, LoginActivity.class);
                EmailVerification.this.startActivity(mainIntent);
                EmailVerification.this.finish();
            }
        });

        resendButton = findViewById(R.id.resend_button);

        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendButton.setEnabled(false);
                sendVerifyEmail();
            }
        });

        sendVerifyEmail();
        handler.postDelayed(verificationRunnable, 500);
    }

    private void sendVerifyEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification(ActionCodeSettings
                    .newBuilder()
                    .setUrl("google.com")
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName("ePantry", false, null)
                    .build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    resendButton.setEnabled(true);
                    if(task.isSuccessful()){
                        Toast.makeText(EmailVerification.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(EmailVerification.this, "Failed to send verification email." +
                                (task.getException() != null ? task.getException().getMessage(): "Unknown Error Occurred."),Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(EmailVerification.this, "Could not find user, Please Login again.", Toast.LENGTH_LONG).show();
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