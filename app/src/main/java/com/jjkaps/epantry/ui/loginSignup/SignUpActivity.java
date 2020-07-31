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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;

    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private Button signupButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();

        nameText = findViewById(R.id.input_name);
        emailText = findViewById(R.id.input_email);
        progressBar = findViewById(R.id.progressBar);
        passwordText = findViewById(R.id.input_password);
        signupButton = findViewById(R.id.btn_signup);
        TextView loginLink = findViewById(R.id.link_login);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                SignUpActivity.this.startActivity(loginIntent);
                SignUpActivity.this.finish();
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed("Oops! Seems like there was a problem");
            return;
        }

        signupButton.setEnabled(false);

        // progress bar show
        progressBar.setVisibility(View.VISIBLE);

        final String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        //get user id and update firebase user collection
                        String id = user.getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> userObj = new HashMap<>();
                        userObj.put("displayName", name);
                        userObj.put("email", user.getEmail());
                        DocumentReference userDoc = db.collection("users").document(id);
                        userDoc.set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                        //enable button
                        signupButton.setEnabled(true);
                        //send to main
                        Intent mainIntent = new Intent(SignUpActivity.this, EmailVerification.class);
                        SignUpActivity.this.startActivity(mainIntent);
                        SignUpActivity.this.finish();
                    } else {
                        onSignupFailed("There was an error getting your user ID, please try again!");//This should not happen
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    onSignupFailed(Objects.requireNonNull(task.getException()).getMessage());

                }
                //hide progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void onSignupFailed(String msg) {
        Utils.createToast(getBaseContext(), msg, Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
        //TODO make a bottom bar notification create a util
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3 || name.length() > 10) {
            nameText.setError("Must be between 3 and 10 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            passwordText.setError("Greater than 4 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

}