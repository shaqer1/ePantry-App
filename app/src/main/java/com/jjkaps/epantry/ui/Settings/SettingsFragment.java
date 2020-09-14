package com.jjkaps.epantry.ui.Settings;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjkaps.epantry.MainActivity;
import com.jjkaps.epantry.R;
import com.jjkaps.epantry.ui.Tutorial.TutorialActivity;
import com.jjkaps.epantry.ui.loginSignup.LoginActivity;
import com.jjkaps.epantry.utils.Utils;

import java.util.Objects;

public class SettingsFragment extends Fragment {
    private static final String TAG = "Settings";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Dialog reauthDialog;
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_settings, container, false);
        reauthDialog = new Dialog(root.getContext());
        if(getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() !=null){
            View view = Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).getCustomView();
            TextView name = view.findViewById(R.id.name);
            name.setText(R.string.nav_settings_txt);
            ImageButton ib = view.findViewById(R.id.btn_update);
            ImageButton ibSort = view.findViewById(R.id.secondaryUpdate);
            ibSort.setVisibility(View.INVISIBLE);
            ib.setVisibility(View.INVISIBLE);
        }
        Utils.hideKeyboard(root.getContext());

        Button changePassword = root.findViewById(R.id.bt_change_password);
        Button settingFavList = root.findViewById(R.id.bt_favList);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        settingFavList.setOnClickListener(view -> {
            Intent i = new Intent(root.getContext(), SettingFavList.class);
            startActivity(i);
        });

        //tutorial
        Button bt_tutorial = root.findViewById(R.id.bt_tutorial);
        bt_tutorial.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), TutorialActivity.class);
            startActivity(i);
        });


        changePassword.setOnClickListener(view -> {
            Log.d("CHANGE PASSWORD", "setting selected");
            openReauthDialog();
        });
        Button logout = root.findViewById(R.id.bt_logout);
        logout.setOnClickListener(view -> {
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            startActivity(new Intent(getActivity(),LoginActivity.class));
            getActivity().finish();
        });
        return root;
    }

    public void openReauthDialog() {
        Button buttonChangePass;
        TextInputEditText reauthOldPass;
        TextInputEditText reauthNewPass;

        reauthDialog.setContentView(R.layout.reauthentication_dialog);

        buttonChangePass = reauthDialog.findViewById(R.id.confirm_change_pass);
        reauthOldPass = reauthDialog.findViewById(R.id.reauth_old);
        reauthNewPass = reauthDialog.findViewById(R.id.reauth_new);
        buttonChangePass.setOnClickListener(view -> {
            final String email = user.getEmail();
            final String old_password = reauthOldPass.getEditableText().toString();
            final String new_password = reauthNewPass.getEditableText().toString();
            //final String email = user.getEmail();
            //prompt for sign in and new password
            if(Utils.isNotNullOrEmpty(email) && Utils.isNotNullOrEmpty(old_password) && Utils.isNotNullOrEmpty(new_password)){
                AuthCredential credential = EmailAuthProvider.getCredential(email, old_password);
                if (user != null) {
                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(new_password).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, root, "Password updated!", Utils.StatusCodes.SUCCESS);
                                    Log.d(TAG, "Password updated.");
                                } else {
                                    Utils.createStatusMessage(Snackbar.LENGTH_SHORT, root, "Oops! Something went wrong.", Utils.StatusCodes.FAILURE);
                                    Log.d(TAG, "Error, password not updated.");
                                }
                                reauthDialog.dismiss();
                            });
                        } else {
                            Log.d(TAG, "Error, authentication failed.");
                            reauthNewPass.getEditableText().clear();
                            reauthOldPass.getEditableText().clear();
                            Utils.createStatusMessage(Snackbar.LENGTH_SHORT, root, "Authentication failed.", Utils.StatusCodes.FAILURE);
                        }
                    });
                }
            }
            Utils.createStatusMessage(Snackbar.LENGTH_SHORT, root, "Fields cannot be empty!", Utils.StatusCodes.FAILURE);
        });
        reauthDialog.show();
    }
}