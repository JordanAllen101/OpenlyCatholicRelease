package com.openlycatholic.app.examination;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import com.openlycatholic.app.R;

import java.util.concurrent.TimeUnit;



public class ConfessionReminderFragment extends Fragment {



    private static final String PREFS_NAME = "ConfessionReminderPrefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_FREQUENCY = "reminder_frequency";
    private static final String KEY_PENANCE_ENABLED = "penance_enabled";
    private static final String KEY_PENANCE_FREQUENCY = "penance_frequency";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    private Switch reminderEnabledSwitch;

    private TextView frequencyLabel;

    private Spinner frequencySpinner;

    private Switch penanceEnabledSwitch;

    private TextView penanceFrequencyLabel;

    private Spinner penanceFrequencySpinner;

    private SharedPreferences sharedPreferences;

    private int selectedFrequencyDays = 7;

    private int selectedPenanceFrequencyDays = 7;



    public ConfessionReminderFragment() {

    }



    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_confession_reminder,
                container,
                false
        );



        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        reminderEnabledSwitch = view.findViewById(R.id.reminderEnabledSwitch);
        frequencyLabel = view.findViewById(R.id.frequencyLabel);
        frequencySpinner = view.findViewById(R.id.frequencySpinner);
        penanceEnabledSwitch = view.findViewById(R.id.penanceEnabledSwitch);
        penanceFrequencyLabel = view.findViewById(R.id.penanceFrequencyLabel);
        penanceFrequencySpinner = view.findViewById(R.id.penanceFrequencySpinner);



        String[] frequencies = {"Weekly (7 days)", "Every 2 weeks (14 days)", "Monthly (30 days)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                frequencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(adapter);
        penanceFrequencySpinner.setAdapter(adapter);



        boolean isEnabled = sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, false);
        int savedFrequency = sharedPreferences.getInt(KEY_FREQUENCY, 7);
        reminderEnabledSwitch.setChecked(isEnabled);

        if(isEnabled) {
            frequencyLabel.setVisibility(View.VISIBLE);
            frequencySpinner.setVisibility(View.VISIBLE);
        } else {
            frequencyLabel.setVisibility(View.GONE);
            frequencySpinner.setVisibility(View.GONE);
        }



        int frequencyPosition = getFrequencyPosition(savedFrequency);
        frequencySpinner.setSelection(frequencyPosition);
        selectedFrequencyDays = savedFrequency;



        boolean penanceEnabled = sharedPreferences.getBoolean(KEY_PENANCE_ENABLED, false);
        int savedPenanceFrequency = sharedPreferences.getInt(KEY_PENANCE_FREQUENCY, 7);
        penanceEnabledSwitch.setChecked(penanceEnabled);

        if(penanceEnabled) {
            penanceFrequencyLabel.setVisibility(View.VISIBLE);
            penanceFrequencySpinner.setVisibility(View.VISIBLE);
        } else {
            penanceFrequencyLabel.setVisibility(View.GONE);
            penanceFrequencySpinner.setVisibility(View.GONE);
        }



        int penanceFrequencyPosition = getFrequencyPosition(savedPenanceFrequency);
        penanceFrequencySpinner.setSelection(penanceFrequencyPosition);
        selectedPenanceFrequencyDays = savedPenanceFrequency;



        reminderEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                frequencyLabel.setVisibility(View.VISIBLE);
                frequencySpinner.setVisibility(View.VISIBLE);
            } else {
                frequencyLabel.setVisibility(View.GONE);
                frequencySpinner.setVisibility(View.GONE);
            }
            autoSaveReminderSettings();
        });



        penanceEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                penanceFrequencyLabel.setVisibility(View.VISIBLE);
                penanceFrequencySpinner.setVisibility(View.VISIBLE);
            } else {
                penanceFrequencyLabel.setVisibility(View.GONE);
                penanceFrequencySpinner.setVisibility(View.GONE);
            }
            autoSaveReminderSettings();
        });



        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFrequencyDays = getDaysFromPosition(position);
                autoSaveReminderSettings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        penanceFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPenanceFrequencyDays = getDaysFromPosition(position);
                autoSaveReminderSettings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Request notification permission on load if needed
        checkNotificationPermission();

        return view;

    }



    private int getFrequencyPosition(int days) {
        switch(days) {
            case 7: return 0;
            case 14: return 1;
            case 30: return 2;
            default: return 0;
        }
    }



    private int getDaysFromPosition(int position) {
        switch(position) {
            case 0: return 7;
            case 1: return 14;
            case 2: return 30;
            default: return 7;
        }
    }



    private void saveReminderSettings() {
        boolean isEnabled = reminderEnabledSwitch.isChecked();
        boolean penanceEnabled = penanceEnabledSwitch.isChecked();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMINDER_ENABLED, isEnabled);
        editor.putInt(KEY_FREQUENCY, selectedFrequencyDays);
        editor.putBoolean(KEY_PENANCE_ENABLED, penanceEnabled);
        editor.putInt(KEY_PENANCE_FREQUENCY, selectedPenanceFrequencyDays);
        editor.apply();



        if(isEnabled) {
            scheduleReminder();
        } else {
            cancelReminder();
        }



        if(penanceEnabled) {
            schedulePenanceReminder();
        } else {
            cancelPenanceReminder();
        }
    }

    private void autoSaveReminderSettings() {
        if(checkNotificationPermission()) {
            saveReminderSettings();
        }
    }



    private void scheduleReminder() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build();

        PeriodicWorkRequest reminderWorkRequest =
                new PeriodicWorkRequest.Builder(
                        ConfessionReminderWorker.class,
                        selectedFrequencyDays,
                        TimeUnit.DAYS
                )
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "confession_reminder",
                androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                reminderWorkRequest
        );
    }



    private void cancelReminder() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("confession_reminder");
    }



    private void schedulePenanceReminder() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build();

        PeriodicWorkRequest penanceWorkRequest =
                new PeriodicWorkRequest.Builder(
                        PenanceReminderWorker.class,
                        selectedPenanceFrequencyDays,
                        TimeUnit.DAYS
                )
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "penance_reminder",
                androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                penanceWorkRequest
        );
    }



    private void cancelPenanceReminder() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("penance_reminder");
    }

    private boolean checkNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveReminderSettings();
                requireActivity().onBackPressed();
            }
        }
    }

}
