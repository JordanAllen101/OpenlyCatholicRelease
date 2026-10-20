package com.openlycatholic.app.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.openlycatholic.app.R;
import com.openlycatholic.app.settings.SettingsFragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        CardView settingsButton = view.findViewById(R.id.settingsCard);
        settingsButton.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainContainer, new SettingsFragment())
                        .addToBackStack(null)
                        .commit()
        );
        return view;
    }
}
