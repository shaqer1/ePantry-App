package com.jjkaps.epantry.ui.loginSignup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;

    private EditText emailText;
    private EditText passwordText;
    private Button loginButton;
    private ProgressBar progressBar;
    private Button forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.btn_login);
        forgotPassword = findViewById(R.id.btn_reset_password);
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
                            Toast.makeText(getBaseContext(),"Email sent.",Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(),"Failed to send password reset email.",Toast.LENGTH_LONG).show();
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
                //overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out); FEATURE: I saw online if we wanna do it
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
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    loginButton.setEnabled(true);
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(mainIntent);
                    LoginActivity.this.finish();
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
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

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