package com.openlycatholic.app.examination;


import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;


import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;


import java.util.ArrayList;



public class QuickExaminationManager {



    private final Fragment fragment;

    private final View rootView;


    private LinearLayout sinsContainer;

    private Button finishButton;

    private Button addCustomSinButton;

    private ArrayList<EditText> savedCountEdits =
            new ArrayList<>();

    private ArrayList<String> selectedSins =
            new ArrayList<>();





    public QuickExaminationManager(

            Fragment fragment,

            View view

    ) {


        this.fragment = fragment;

        this.rootView = view;


    }







    public void initialize() {



        sinsContainer =
                rootView.findViewById(

                        R.id.sinsContainer

                );



        finishButton =
                rootView.findViewById(

                        R.id.quickFinishButton

                );



        addCustomSinButton =
                rootView.findViewById(
                        R.id.addQuickCustomSinButton
                );




        loadSins();





        finishButton.setOnClickListener(v -> {



            openConfessionGuide();



        });



        addCustomSinButton.setOnClickListener(v -> {
            showCustomSinDialog();
        });



    }







    private void loadSins() {



        for(String sin : QuickExaminationData.sins) {



            CheckBox box =
                    new CheckBox(

                            rootView.getContext()

                    );



            box.setText(sin);



            box.setTextColor(
                    rootView.getContext().getColor(R.color.text_primary)
            );



            box.setTextSize(

                    16

            );



            box.setPadding(
                    0,
                    20,
                    0,
                    20
            );

            LinearLayout rowLayout = new LinearLayout(rootView.getContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            box.setLayoutParams(checkboxParams);

            EditText countEdit = new EditText(rootView.getContext());
            countEdit.setHint("x");
            countEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            countEdit.setMinWidth(60);
            countEdit.setMaxWidth(80);
            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            editParams.setMargins(16, 0, 0, 0);
            countEdit.setLayoutParams(editParams);

            rowLayout.addView(box);
            rowLayout.addView(countEdit);

            savedCountEdits.add(countEdit);

            box.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if(isChecked) {
                            String sinWithCount = sin;
                            String count = countEdit.getText().toString().trim();
                            if(!count.isEmpty()) {
                                sinWithCount = sin + " x" + count;
                            }
                            if(!selectedSins.contains(sinWithCount)) {
                                selectedSins.add(sinWithCount);
                            }
                        }
                        else {
                            String sinWithCount = sin;
                            String count = countEdit.getText().toString().trim();
                            if(!count.isEmpty()) {
                                sinWithCount = sin + " x" + count;
                            }
                            selectedSins.remove(sinWithCount);
                        }
                    }
            );

            sinsContainer.addView(
                    rowLayout
            );



        }


    }








    private void refreshSelectedSins() {
        selectedSins.clear();
        sinsContainer = rootView.findViewById(R.id.sinsContainer);
        for(int i = 0; i < sinsContainer.getChildCount(); i++) {
            View child = sinsContainer.getChildAt(i);
            if(child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                CheckBox box = null;
                EditText countEdit = null;
                for(int j = 0; j < row.getChildCount(); j++) {
                    View rowChild = row.getChildAt(j);
                    if(rowChild instanceof CheckBox) {
                        box = (CheckBox) rowChild;
                    } else if(rowChild instanceof EditText) {
                        countEdit = (EditText) rowChild;
                    }
                }
                if(box != null && box.isChecked()) {
                    String sin = box.getText().toString();
                    String sinWithCount = sin;
                    if(countEdit != null) {
                        String count = countEdit.getText().toString().trim();
                        if(!count.isEmpty()) {
                            sinWithCount = sin + " x" + count;
                        }
                    }
                    if(!selectedSins.contains(sinWithCount)) {
                        selectedSins.add(sinWithCount);
                    }
                }
            }
        }
    }

    private void openConfessionGuide() {
        refreshSelectedSins();

        ConfessionGuideFragment guide =
                ConfessionGuideFragment.newInstance(

                        selectedSins

                );



        fragment.requireActivity()

                .getSupportFragmentManager()

                .beginTransaction()

                .replace(

                        R.id.mainContainer,

                        guide

                )

                .addToBackStack(null)

                .commit();



    }



    private void showCustomSinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireActivity());
        builder.setTitle("Add Custom Sin");

        final EditText input = new EditText(fragment.requireActivity());
        input.setHint("Enter your custom sin here...");
        input.setTextColor(rootView.getContext().getColor(R.color.text_primary));
        input.setHintTextColor(rootView.getContext().getColor(R.color.text_secondary));
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String customSin = input.getText().toString().trim();
            if (!customSin.isEmpty()) {
                addCustomSin(customSin);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });

        builder.show();
    }



    private void addCustomSin(String customSin) {
        if (!selectedSins.contains(customSin)) {
            selectedSins.add(customSin);
        }

        LinearLayout rowLayout = new LinearLayout(rootView.getContext());
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        CheckBox box = new CheckBox(rootView.getContext());
        box.setText(customSin);
        box.setTextColor(rootView.getContext().getColor(R.color.text_primary));
        box.setTextSize(16);
        box.setPadding(0, 20, 0, 20);
        box.setChecked(true);

        LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        box.setLayoutParams(checkboxParams);

        EditText countEdit = new EditText(rootView.getContext());
        countEdit.setHint("x");
        countEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        countEdit.setMinWidth(60);
        countEdit.setMaxWidth(80);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editParams.setMargins(16, 0, 0, 0);
        countEdit.setLayoutParams(editParams);

        rowLayout.addView(box);
        rowLayout.addView(countEdit);

        savedCountEdits.add(countEdit);

        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                String sinWithCount = customSin;
                String count = countEdit.getText().toString().trim();
                if(!count.isEmpty()) {
                    sinWithCount = customSin + " x" + count;
                }
                if(!selectedSins.contains(sinWithCount)) {
                    selectedSins.add(sinWithCount);
                }
            }
            else {
                String sinWithCount = customSin;
                String count = countEdit.getText().toString().trim();
                if(!count.isEmpty()) {
                    sinWithCount = customSin + " x" + count;
                }
                selectedSins.remove(sinWithCount);
            }
        });

        sinsContainer.addView(rowLayout);
    }



}
