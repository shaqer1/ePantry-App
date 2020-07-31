package com.jjkaps.epantry;


import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjkaps.epantry.models.LoggedInUser;
import com.jjkaps.epantry.ui.loginSignup.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(this.getSupportActionBar() != null){
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setDisplayShowCustomEnabled(true);
            //getSupportActionBar().setIcon(new ColorDrawable(getColor(R.color.colorWhite)));
            getSupportActionBar().setCustomView(R.layout.custom_action_bar);

            View view = getSupportActionBar().getCustomView();
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            //TextView name = view.findViewById(R.id.name);
        }

        //Greets user and make sure user exists in database.
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        LoggedInUser user = documentSnapshot.toObject(LoggedInUser.class);
                        if (user != null) {
                            Toast toast= Toast.makeText(getBaseContext(), "Hi, " + user.getDisplayName() + "!", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            View vi = toast.getView();
                            vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                            TextView text = vi.findViewById(android.R.id.message);
                            text.setTextColor(Color.BLACK);
                            text.setTextSize(25);
                            toast.show();


                        }
                    } else {
                        Toast toast= Toast.makeText(getBaseContext(), "Hmm, couldn't find this user, please try loggin-in again", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View vi = toast.getView();
                        vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        TextView text = vi.findViewById(android.R.id.message);
                        text.setTextColor(Color.BLACK);
                        text.setTextSize(25);
                        toast.show();

                        //send to main
                        Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
                        MainActivity.this.startActivity(mainIntent);
                        MainActivity.this.finish();
                    }
                }
            });
        }
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_fridge, R.id.navigation_shopping, R.id.navigation_catalog, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

}