package com.jjkaps.epantry.ui.loginSignup;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.Tutorial.TutorialActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;

    private EditText emailText;
    private EditText passwordText;
    private Button loginButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.btn_login);
        TextView forgotPassword = findViewById(R.id.btn_reset_password);
        emailText = findViewById(R.id.input_email);
        progressBar = findViewById(R.id.progressBar);
        passwordText = findViewById(R.id.input_password);
        TextView signupLink = findViewById(R.id.link_signup);
        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString().trim();
                if (email.isEmpty()|| !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailText.setError("enter a valid email address");
                    return;
                }else{
                    emailText.setError(null);
                }

                progressBar.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Log.d(TAG, "Email sent.");
                            Toast toast = Toast.makeText(getBaseContext(),"Email sent.",Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            View vi = toast.getView();
                            vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                            TextView text = vi.findViewById(android.R.id.message);
                            text.setTextColor(Color.BLACK);
                            text.setTextSize(25);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(getBaseContext(),"Failed to send password reset email.",Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            View vi = toast.getView();
                            vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                            TextView text = vi.findViewById(android.R.id.message);
                            text.setTextColor(Color.BLACK);
                            text.setTextSize(25);
                            toast.show();
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }

        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed("Oops! Seems like there was a problem");
            return;
        }

        loginButton.setEnabled(false);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // progress bar show
        progressBar.setVisibility(View.VISIBLE);

        // Implemented authentication logic here.
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    loginButton.setEnabled(true);
                    final FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        //reload user and check if user is verified or user signed in
                        user.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent mainIntent = new Intent(LoginActivity.this, user.isEmailVerified() ? MainActivity.class : EmailVerification.class);
                                LoginActivity.this.startActivity(mainIntent);
                                LoginActivity.this.finish();
                            }
                        });
                    } else {
                        Toast toast = Toast.makeText(LoginActivity.this, "Could not find user, Please Login again.", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        View vi = toast.getView();
                        vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        TextView text = vi.findViewById(android.R.id.message);
                        text.setTextColor(Color.BLACK);
                        text.setTextSize(25);
                        toast.show();

                        Intent mainIntent = new Intent(LoginActivity.this, LoginActivity.class);
                        LoginActivity.this.startActivity(mainIntent);
                        LoginActivity.this.finish();
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    onLoginFailed(Objects.requireNonNull(task.getException()).getMessage());
                }
                //hide progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }


    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginFailed(String msg) {
        Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        View vi = toast.getView();
        vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        TextView text = vi.findViewById(android.R.id.message);
        text.setTextColor(Color.BLACK);
        text.setTextSize(25);
        toast.show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 ) {
            passwordText.setError("must be greater than 6 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}