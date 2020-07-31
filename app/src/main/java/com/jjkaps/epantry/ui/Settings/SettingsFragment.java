package com.jjkaps.epantry.ui.Settings;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.Tutorial.TutorialActivity;
import com.jjkaps.epantry.ui.loginSignup.EmailVerification;
import com.jjkaps.epantry.ui.loginSignup.LoginActivity;
import com.jjkaps.epantry.ui.loginSignup.LoginActivity;
import com.jjkaps.epantry.utils.Utils;

import java.util.Objects;

public class SettingsFragment extends Fragment {
    private static final String TAG = "Settings";
    private SettingsViewModel settingsViewModel;
    private Button settingFavList;
    private Button bt_tutorial;
    private FirebaseAuth mAuth;
    private Dialog reauthDialog;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_settings, container, false);

//        final TextView textView = root.findViewById(R.id.text_settings);
//        settingsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
////                textView.setText(s);
//            }
//        });
        reauthDialog = new Dialog(root.getContext());
//        final TextView textView = root.findViewById(R.id.text_settings);
//        settingsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
////                textView.setText(s);
//            }
//        });
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.nav_settings_txt);
        }
        Utils.hideKeyboard(root.getContext());

        Button changePassword = root.findViewById(R.id.bt_change_password);
        settingFavList = root.findViewById(R.id.bt_favList);

        settingFavList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(root.getContext(), SettingFavList.class);
                startActivity(i);
            }
        });

        //tutorial
        bt_tutorial = root.findViewById(R.id.bt_tutorial);
        bt_tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), TutorialActivity.class);
                startActivity(i);
            }
        });


        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CHANGE PASSWORD", "setting selected");
                try {
                    openReauthDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                    //TODO: clean up exception handling
                }
            }
        });
        Button logout = root.findViewById(R.id.bt_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                startActivity(new Intent(getActivity(),LoginActivity.class));
                getActivity().finish();
            }
        });
        return root;
    }

    public void openReauthDialog() {
//        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
//        changePasswordDialog.show(getFragmentManager(), "Change Password Dialog");
        Button buttonChangePass;
        final EditText reauthEmail;
        final EditText reauthOldPass;
        final EditText reauthNewPass;

        reauthDialog.setContentView(R.layout.reauthentication_dialog);

        buttonChangePass = reauthDialog.findViewById(R.id.confirm_change_pass);
        reauthEmail = reauthDialog.findViewById(R.id.reauth_email);
        reauthOldPass = reauthDialog.findViewById(R.id.reauth_old);
        reauthNewPass = reauthDialog.findViewById(R.id.reauth_new);
        buttonChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = reauthEmail.getText().toString();
                final String old_password = reauthOldPass.getText().toString();
                final String new_password = reauthNewPass.getText().toString();
                mAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = mAuth.getCurrentUser();
                //final String email = user.getEmail();
                //prompt for sign in and new password
                try {
                    AuthCredential credential = EmailAuthProvider.getCredential(email, old_password);
                    if (user != null) {
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast toast = Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                                View vi = toast.getView();
                                                vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                                                TextView text = vi.findViewById(android.R.id.message);
                                                text.setTextColor(Color.BLACK);
                                                text.setTextSize(25);
                                                toast.show();
                                                Log.d(TAG, "Password updated.");
                                                reauthDialog.dismiss();
                                            } else {
                                                Toast toast = Toast.makeText(getContext(), "Oops! Something went wrong.", Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                                View vi = toast.getView();
                                                vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                                                TextView text = vi.findViewById(android.R.id.message);
                                                text.setTextColor(Color.BLACK);
                                                text.setTextSize(25);
                                                toast.show();
                                                Log.d(TAG, "Error, password not updated.");
                                                reauthDialog.dismiss();
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "Error, authentication failed.");
                                    Toast toast = Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                    View vi = toast.getView();
                                    vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                                    TextView text = vi.findViewById(android.R.id.message);
                                    text.setTextColor(Color.BLACK);
                                    text.setTextSize(25);
                                    toast.show();
                                    reauthDialog.dismiss();
                                }
                            }
                        });
                    }
                }  catch (IllegalArgumentException e) {
                    Toast toast = Toast.makeText(getContext(), "Fields cannot be empty!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    View vi = toast.getView();
                    vi.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    TextView text = vi.findViewById(android.R.id.message);
                    text.setTextColor(Color.BLACK);
                    text.setTextSize(25);
                    toast.show();
                }
            }
        });
        reauthDialog.show();
    }

//    @Override
//    public void applyTexts(String email, String old_password, String new_password) {
//
//    }
}