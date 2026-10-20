package com.openlycatholic.app.rosary;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;



public class AudioSettingsFragment extends Fragment {



    private static final String PREFS_NAME = "AudioRosaryPrefs";
    private static final String KEY_AUDIO_ENABLED = "audio_enabled";
    private static final String KEY_FOLDER_URI = "folder_uri";

    private static final int FOLDER_PICKER_REQUEST_CODE = 1001;

    private TextView folderPathText;

    private Button selectFolderButton;

    private Switch audioEnabledSwitch;

    private Button saveButton;

    private SharedPreferences sharedPreferences;

    private Uri selectedFolderUri = null;



    public AudioSettingsFragment() {

    }



    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_audio_settings,
                container,
                false
        );



        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        folderPathText = view.findViewById(R.id.folderPathText);
        selectFolderButton = view.findViewById(R.id.selectFolderButton);
        audioEnabledSwitch = view.findViewById(R.id.audioEnabledSwitch);
        saveButton = view.findViewById(R.id.saveButton);



        boolean isEnabled = sharedPreferences.getBoolean(KEY_AUDIO_ENABLED, false);
        String savedUriString = sharedPreferences.getString(KEY_FOLDER_URI, null);

        audioEnabledSwitch.setChecked(isEnabled);

        if(savedUriString != null) {
            selectedFolderUri = Uri.parse(savedUriString);
            folderPathText.setText("Folder selected");
        } else {
            folderPathText.setText("No folder selected");
        }



        selectFolderButton.setOnClickListener(v -> {
            openFolderPicker();
        });



        saveButton.setOnClickListener(v -> {
            saveSettings();
            requireActivity().onBackPressed();
        });



        return view;

    }



    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, FOLDER_PICKER_REQUEST_CODE);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FOLDER_PICKER_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                selectedFolderUri = data.getData();

                requireActivity().getContentResolver().takePersistableUriPermission(
                        selectedFolderUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );

                folderPathText.setText("Folder selected");
            }
        }
    }



    private void saveSettings() {
        boolean isEnabled = audioEnabledSwitch.isChecked();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_AUDIO_ENABLED, isEnabled);
        if(selectedFolderUri != null) {
            editor.putString(KEY_FOLDER_URI, selectedFolderUri.toString());
        } else {
            editor.remove(KEY_FOLDER_URI);
        }
        editor.apply();
    }

}
