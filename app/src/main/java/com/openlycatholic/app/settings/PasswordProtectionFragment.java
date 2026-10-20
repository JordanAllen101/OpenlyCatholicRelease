package com.openlycatholic.app.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openlycatholic.app.R;

public class PasswordProtectionFragment extends Fragment {

    private static final String PREFS_NAME = "PasswordProtectionPrefs";
    private static final String KEY_PASSWORD_ENABLED = "password_enabled";
    private static final String KEY_PASSWORD = "password";

    private Switch passwordEnabledSwitch;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button saveButton;
    private TextView statusText;

    private SharedPreferences sharedPreferences;

    public PasswordProtectionFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_password_protection,
                container,
                false
        );

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        passwordEnabledSwitch = view.findViewById(R.id.passwordEnabledSwitch);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        saveButton = view.findViewById(R.id.saveButton);
        statusText = view.findViewById(R.id.statusText);

        boolean isEnabled = sharedPreferences.getBoolean(KEY_PASSWORD_ENABLED, false);
        passwordEnabledSwitch.setChecked(isEnabled);

        if (isEnabled) {
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
            passwordEditText.setText(savedPassword);
            confirmPasswordEditText.setText(savedPassword);
            passwordEditText.setVisibility(View.VISIBLE);
            confirmPasswordEditText.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            statusText.setText("Password protection is enabled");
        } else {
            passwordEditText.setVisibility(View.GONE);
            confirmPasswordEditText.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            statusText.setText("Password protection is disabled");
        }

        passwordEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setVisibility(View.VISIBLE);
                confirmPasswordEditText.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                statusText.setText("Enter a password to enable protection");
            } else {
                // When trying to disable, require password verification
                String currentPassword = sharedPreferences.getString(KEY_PASSWORD, "");
                if (!currentPassword.isEmpty()) {
                    showDisablePasswordDialog(currentPassword, (android.widget.Switch) buttonView);
                } else {
                    passwordEditText.setVisibility(View.GONE);
                    confirmPasswordEditText.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);
                    statusText.setText("Password protection is disabled");
                    disablePasswordProtection();
                }
            }
        });

        saveButton.setOnClickListener(v -> savePassword());

        return view;
    }

    private void savePassword() {
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(requireContext(), "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PASSWORD_ENABLED, true);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();

        Toast.makeText(requireContext(), "Password saved successfully", Toast.LENGTH_SHORT).show();
        statusText.setText("Password protection is enabled");
    }

    private void disablePasswordProtection() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PASSWORD_ENABLED, false);
        editor.putString(KEY_PASSWORD, "");
        editor.apply();

        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
    }

    private void showDisablePasswordDialog(String currentPassword, Switch switchView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Current Password");

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            if (currentPassword.equals(password)) {
                passwordEditText.setVisibility(View.GONE);
                confirmPasswordEditText.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                statusText.setText("Password protection is disabled");
                disablePasswordProtection();
                Toast.makeText(requireContext(), "Password protection disabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                switchView.setChecked(true); // Re-enable the switch
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            switchView.setChecked(true); // Re-enable the switch
        });

        builder.setCancelable(false);
        builder.show();
    }

    public static boolean isPasswordEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_PASSWORD_ENABLED, false);
    }

    public static boolean verifyPassword(Context context, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPassword = prefs.getString(KEY_PASSWORD, "");
        return savedPassword.equals(password);
    }
}
