package com.openlycatholic.app.examination;


import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;


import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;


import java.util.ArrayList;



public class ExaminationManager {



    private final View rootView;

    private final Fragment fragment;



    private TextView examTitle;

    private TextView categoryTitle;


    private LinearLayout questionsContainer;


    private Button previousButton;

    private Button nextButton;

    private Button addCustomSinButton;

    private int currentCategory = 0;



    private ArrayList<CheckBox[]> savedChecks =
            new ArrayList<>();

    private ArrayList<EditText[]> savedCountEdits =
            new ArrayList<>();

    private ArrayList<String> selectedSins =
            new ArrayList<>();









    public ExaminationManager(
            Fragment fragment,
            View view
    ) {


        this.fragment = fragment;

        this.rootView = view;


    }









    public void initialize() {



        examTitle =
                rootView.findViewById(
                        R.id.examTitle
                );



        categoryTitle =
                rootView.findViewById(
                        R.id.commandmentTitle
                );



        questionsContainer =
                rootView.findViewById(
                        R.id.questionsContainer
                );



        previousButton =
                rootView.findViewById(
                        R.id.previousCommandmentButton
                );



        nextButton =
                rootView.findViewById(
                        R.id.nextCommandmentButton
                );



        addCustomSinButton =
                rootView.findViewById(
                        R.id.addCustomSinButton
                );





        createStorage();






        previousButton.setOnClickListener(v -> {


            saveCurrentSelections();



            if(currentCategory > 0) {


                currentCategory--;


                loadCategory();


            }


        });









        nextButton.setOnClickListener(v -> {


            saveCurrentSelections();



            if(currentCategory <
                    ExaminationData.categories.length - 1) {



                currentCategory++;


                loadCategory();



            }

            else {


                openConfessionGuide();


            }


        });







        loadCategory();
        addCustomSinButton.setOnClickListener(v -> {
            showCustomSinDialog();
        });


    }









    private void createStorage() {



        for(int i = 0;
            i < ExaminationData.questions.length;
            i++) {



            CheckBox[] boxes =
                    new CheckBox[
                            ExaminationData.questions[i].length
                    ];



            savedChecks.add(
                    boxes
            );

            EditText[] countEdits =
                    new EditText[
                            ExaminationData.questions[i].length
                    ];

            savedCountEdits.add(
                    countEdits
            );


        }


    }









    private void loadCategory() {



        questionsContainer.removeAllViews();




        categoryTitle.setText(
                ExaminationData.categories[currentCategory]
        );





        String[] questions =
                ExaminationData.questions[currentCategory];







        for(int i = 0;
            i < questions.length;
            i++) {

            CheckBox checkBox =
                    new CheckBox(
                            rootView.getContext()
                    );

            checkBox.setText(
                    questions[i]
            );

            checkBox.setTextColor(
                    rootView.getContext().getColor(R.color.text_primary)
            );

            checkBox.setTextSize(
                    16
            );

            checkBox.setPadding(
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
            checkBox.setLayoutParams(checkboxParams);

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

            rowLayout.addView(checkBox);
            rowLayout.addView(countEdit);

            savedChecks
                    .get(currentCategory)[i]
                    =
                    checkBox;

            savedCountEdits
                    .get(currentCategory)[i]
                    =
                    countEdit;

            questionsContainer.addView(
                    rowLayout
            );

        }

        updateButtons();

    }

    private void saveCurrentSelections() {

        CheckBox[] boxes =
                savedChecks.get(
                        currentCategory
                );

        EditText[] countEdits =
                savedCountEdits.get(
                        currentCategory
                );

        for(int i = 0; i < boxes.length; i++) {
            CheckBox box = boxes[i];
            EditText countEdit = countEdits[i];

            if(box != null && box.isChecked()) {
                String sin = box.getText().toString();
                String count = countEdit.getText().toString().trim();

                if(!count.isEmpty()) {
                    sin = sin + " x" + count;
                }

                if(!selectedSins.contains(sin)) {
                    selectedSins.add(sin);
                }
            }
        }

    }














    private void updateButtons() {



        previousButton.setEnabled(
                currentCategory > 0
        );






        if(currentCategory ==
                ExaminationData.categories.length - 1) {



            nextButton.setText(
                    "Finish"
            );


        }

        else {



            nextButton.setText(
                    "Next"
            );


        }


    }









    private void openConfessionGuide() {



        ConfessionGuideFragment guide =
                ConfessionGuideFragment
                        .newInstance(
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

        CheckBox checkBox = new CheckBox(rootView.getContext());
        checkBox.setText(customSin);
        checkBox.setTextColor(rootView.getContext().getColor(R.color.text_primary));
        checkBox.setTextSize(16);
        checkBox.setPadding(0, 20, 0, 20);
        checkBox.setChecked(true);

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
        checkBox.setLayoutParams(checkboxParams);

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

        rowLayout.addView(checkBox);
        rowLayout.addView(countEdit);

        questionsContainer.addView(rowLayout);

        CheckBox[] currentBoxes = savedChecks.get(currentCategory);
        CheckBox[] newBoxes = new CheckBox[currentBoxes.length + 1];
        System.arraycopy(currentBoxes, 0, newBoxes, 0, currentBoxes.length);
        newBoxes[currentBoxes.length] = checkBox;
        savedChecks.set(currentCategory, newBoxes);

        EditText[] currentEdits = savedCountEdits.get(currentCategory);
        EditText[] newEdits = new EditText[currentEdits.length + 1];
        System.arraycopy(currentEdits, 0, newEdits, 0, currentEdits.length);
        newEdits[currentEdits.length] = countEdit;
        savedCountEdits.set(currentCategory, newEdits);
    }



}
