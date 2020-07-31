package com.jjkaps.epantry.ui.loginSignup;

import android.content.Intent;
import android.graphics.Color;
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

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.Tutorial.TutorialActivity;
import com.jjkaps.epantry.utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;

    private EditText emailText;
    private EditText passwordText;
    private Button loginButton;
    private ProgressBar progressBar;
    private SignInButton googleLoginBT;

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
        googleLoginBT = findViewById(R.id.btn_googleLogin);
        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });


        // Configure Google Sign In
        /*GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();*/
        // Build a GoogleSignInClient with the options specified by gso.
        //GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleLoginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
                startActivityForResult(AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setAvailableProviders(providers).build(),RC_SIGN_IN);
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
                            Utils.createToast(getBaseContext(),"Email sent.",Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                        } else {
                            Utils.createToast(getBaseContext(),"Failed to send password reset email.",Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    //get user id and update firebase user collection
                    String id = user.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> userObj = new HashMap<>();
                    if(user.getEmail()!=null){
                        userObj.put("displayName", user.getEmail().substring(0, user.getEmail().indexOf("@")));
                        userObj.put("email", user.getEmail());
                    }
                    DocumentReference userDoc = db.collection("users").document(id);
                    userDoc.set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent mainIntent = new Intent(LoginActivity.this, TutorialActivity.class); // todo send to tutorial
                            LoginActivity.this.startActivity(mainIntent);
                            LoginActivity.this.finish();
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });

                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Utils.createToast(getBaseContext(), "Oops! Seems like there was a problem: "+ (response != null ?
                        ((response.getError()!=null)? response.getError().getErrorCode():"") : ""), Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
            }
            /*Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }*/
            /*GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data); // get users data
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account.getIdToken());*/
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Utils.createToast(LoginActivity.this, "Google sign-in successful.", Toast.LENGTH_LONG);

                            // logged in - redirect to main
                            FirebaseUser user = mAuth.getCurrentUser();
                            user = mAuth.getCurrentUser();
                            if (user != null) {

                                //get user id and update firebase user collection
                                String id = user.getUid();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> userObj = new HashMap<>();
                                userObj.put("displayName", user.getDisplayName());
                                userObj.put("email", user.getEmail());
                                DocumentReference userDoc = db.collection("users").document(id);
                                userDoc.set(userObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class); // todo send to tutorial
                                        LoginActivity.this.startActivity(mainIntent);
                                        LoginActivity.this.finish();
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });

                            } else {
                                Utils.createToast(getBaseContext(), "Oops! Seems like there was a problem", Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
                            }
                        }
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
                        Utils.createToast(LoginActivity.this, "Could not find user, Please Login again.", Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);

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
        Utils.createToast(getBaseContext(), msg, Toast.LENGTH_LONG, Gravity.CENTER_VERTICAL, Color.LTGRAY);
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