package com.openlycatholic.app.examination;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openlycatholic.app.R;
import com.openlycatholic.app.settings.PasswordProtectionFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ConfessionGuideFragment extends Fragment {

    private ArrayList<String> sins = new ArrayList<>();

    private ConfessionRecord existingRecord;
    private int recordPosition = -1;
    private TextView selectedSins;
    private TextView confessionGuideText;
    private TextView penanceText;
    private TextView notesText;
    private TextView beginningConfessionHeader;
    private TextView beginningConfessionText;
    private TextView afterConfessionHeader;
    private TextView afterConfessionText;
    private TextView actOfContritionHeader;
    private TextView actOfContritionText;

    private final String actOfContrition =
            "O my God, I am heartily sorry for having offended You, " +
            "and I detest all my sins because of Your just punishments, " +
            "but most of all because they offend You, my God, " +
            "who are all good and deserving of all my love.\n\n" +
            "I firmly resolve, with the help of Your grace, " +
            "to sin no more and to avoid the near occasions of sin.\n\n" +
            "Amen.";

    public static ConfessionGuideFragment newInstance(ArrayList<String> selectedSins) {
        ConfessionGuideFragment fragment = new ConfessionGuideFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("sins", selectedSins);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ConfessionGuideFragment newInstance(ConfessionRecord record, int position) {
        ConfessionGuideFragment fragment = new ConfessionGuideFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("record", record);
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ArrayList<String> received = getArguments().getStringArrayList("sins");
            if (received != null) {
                sins = received;
            }
            ConfessionRecord record = (ConfessionRecord) getArguments().getSerializable("record");
            if (record != null) {
                existingRecord = record;
                recordPosition = getArguments().getInt("position", -1);
                sins = record.sins;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_confession_guide,
                container,
                false
        );

        selectedSins = view.findViewById(R.id.selectedSins);
        confessionGuideText = view.findViewById(R.id.confessionGuideText);
        penanceText = view.findViewById(R.id.penanceText);
        notesText = view.findViewById(R.id.notesText);
        beginningConfessionHeader = view.findViewById(R.id.beginningConfessionHeader);
        beginningConfessionText = view.findViewById(R.id.beginningConfessionText);
        afterConfessionHeader = view.findViewById(R.id.afterConfessionHeader);
        afterConfessionText = view.findViewById(R.id.afterConfessionText);
        actOfContritionHeader = view.findViewById(R.id.actOfContritionHeader);
        actOfContritionText = view.findViewById(R.id.actOfContritionText);

        if (existingRecord != null) {
            checkPasswordAndDisplay();
        } else {
            displaySins(selectedSins);
            selectedSins.setVisibility(View.VISIBLE);
            confessionGuideText.setVisibility(View.GONE);
            penanceText.setVisibility(View.GONE);
            notesText.setVisibility(View.GONE);
        }

        Button saveButton = view.findViewById(R.id.saveConfessionButton);
        Button editButton = view.findViewById(R.id.editConfessionButton);

        if (existingRecord != null) {
            saveButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
        }

        saveButton.setOnClickListener(v -> showSaveDialog());
        editButton.setOnClickListener(v -> showEditDialog());

        return view;
    }

    private String buildSinList() {
        StringBuilder builder = new StringBuilder();
        builder.append("The sins I am confessing:\n\n");

        for (String sin : sins) {
            String displaySin = sin;
            if (sin.contains(" x")) {
                int lastIndex = sin.lastIndexOf(" x");
                String sinName = sin.substring(0, lastIndex);
                String count = sin.substring(lastIndex + 2);
                displaySin = sinName + " (" + count + " times)";
            }

            builder.append("• ").append(displaySin).append("\n\n");
        }

        builder.append("For these and all my sins, I am heartily sorry.");
        return builder.toString();
    }

    private void displaySins(TextView textView) {
        if (sins.isEmpty()) {
            textView.setText("No sins selected.");
            return;
        }
        textView.setText(buildSinList());
    }

    private void checkPasswordAndDisplay() {
        if (PasswordProtectionFragment.isPasswordEnabled(requireContext())) {
            showPasswordDialog();
        } else {
            displayExistingRecord();
        }
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Password");

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            if (PasswordProtectionFragment.verifyPassword(requireContext(), password)) {
                displayExistingRecord();
            } else {
                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            requireActivity().onBackPressed();
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void displayExistingRecord() {
        if (existingRecord != null) {
            beginningConfessionHeader.setVisibility(View.VISIBLE);
            beginningConfessionText.setVisibility(View.VISIBLE);
            afterConfessionHeader.setVisibility(View.VISIBLE);
            afterConfessionText.setVisibility(View.VISIBLE);
            actOfContritionHeader.setVisibility(View.VISIBLE);
            actOfContritionText.setVisibility(View.VISIBLE);

            confessionGuideText.setVisibility(View.GONE);

            // Hide the top standalone sins view so it doesn't render twice
            selectedSins.setVisibility(View.GONE);

            if (existingRecord.sins != null && !existingRecord.sins.isEmpty()) {
                sins = existingRecord.sins;
            }

            if (existingRecord.confessionGuide != null && !existingRecord.confessionGuide.isEmpty()) {
                beginningConfessionText.setText(existingRecord.confessionGuide);
            }

            if (existingRecord.penance != null && !existingRecord.penance.isEmpty()) {
                penanceText.setText("Penance: " + existingRecord.penance);
                penanceText.setVisibility(View.VISIBLE);
            } else {
                penanceText.setVisibility(View.GONE);
            }

            if (existingRecord.notes != null && !existingRecord.notes.isEmpty()) {
                notesText.setText("Notes: " + existingRecord.notes);
                notesText.setVisibility(View.VISIBLE);
            } else {
                notesText.setVisibility(View.GONE);
            }
        }
    }

    private void showSaveDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        EditText penance = new EditText(requireContext());
        penance.setHint("Penance (optional)");

        EditText notes = new EditText(requireContext());
        notes.setHint("Notes (optional)");
        notes.setMinLines(3);

        layout.addView(penance);
        layout.addView(notes);

        new AlertDialog.Builder(requireContext())
                .setTitle("Save Confession Record")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String date = new SimpleDateFormat(
                            "MMMM d, yyyy",
                            Locale.getDefault()
                    ).format(new Date());

                    String guide = "Bless me, Father, for I have sinned. " +
                            "It has been ______ since my last confession.\n\n" +
                            buildSinList();

                    ConfessionRecord record = new ConfessionRecord(
                            date,
                            sins,
                            guide,
                            actOfContrition,
                            penance.getText().toString(),
                            notes.getText().toString()
                    );

                    ConfessionStorage.saveRecord(requireContext(), record);

                    Toast.makeText(
                            requireContext(),
                            "Confession saved",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

    private void showEditDialog() {
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        EditText guideInput = new EditText(requireContext());
        guideInput.setHint("Confession Guide");
        guideInput.setText(existingRecord.confessionGuide);
        guideInput.setMinLines(5);
        LinearLayout.LayoutParams guideParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        guideInput.setLayoutParams(guideParams);

        EditText penanceInput = new EditText(requireContext());
        penanceInput.setHint("Penance (optional)");
        penanceInput.setText(existingRecord.penance);
        penanceInput.setMinLines(2);
        LinearLayout.LayoutParams penanceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        penanceParams.setMargins(0, 20, 0, 0);
        penanceInput.setLayoutParams(penanceParams);

        EditText notesInput = new EditText(requireContext());
        notesInput.setHint("Notes (optional)");
        notesInput.setText(existingRecord.notes);
        notesInput.setMinLines(3);
        LinearLayout.LayoutParams notesParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        notesParams.setMargins(0, 20, 0, 0);
        notesInput.setLayoutParams(notesParams);

        layout.addView(guideInput);
        layout.addView(penanceInput);
        layout.addView(notesInput);

        scrollView.addView(layout);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Confession")
                .setView(scrollView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newGuide = guideInput.getText().toString();
                    String newPenance = penanceInput.getText().toString();
                    String newNotes = notesInput.getText().toString();

                    if (recordPosition >= 0) {
                        ConfessionStorage.updateRecord(
                                requireContext(),
                                recordPosition,
                                newGuide,
                                newPenance,
                                newNotes,
                                sins
                        );

                        existingRecord.confessionGuide = newGuide;
                        existingRecord.penance = newPenance;
                        existingRecord.notes = newNotes;
                        existingRecord.sins = sins;

                        displayExistingRecord();

                        Toast.makeText(
                                requireContext(),
                                "Confession updated",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }
}
