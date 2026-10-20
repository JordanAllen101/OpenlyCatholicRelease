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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;

import java.util.ArrayList;



public class ConfessionDetailFragment extends Fragment {


    private ConfessionRecord record;

    private int recordPosition = -1;

    private TextView content;

    private String guide;
    private String penance;
    private String notes;




    public ConfessionDetailFragment(){

    }






    public static ConfessionDetailFragment newInstance(
            ConfessionRecord confessionRecord,
            int position
    ){


        ConfessionDetailFragment fragment =
                new ConfessionDetailFragment();



        Bundle bundle =
                new Bundle();



        bundle.putString(
                "date",
                confessionRecord.date
        );


        bundle.putString(
                "guide",
                confessionRecord.confessionGuide
        );


        bundle.putString(
                "penance",
                confessionRecord.penance
        );


        bundle.putString(
                "notes",
                confessionRecord.notes
        );

        bundle.putInt(
                "position",
                position
        );



        fragment.setArguments(
                bundle
        );



        return fragment;


    }








    @Override
    public void onCreate(
            Bundle savedInstanceState
    ){

        super.onCreate(
                savedInstanceState
        );


    }









    @Nullable
    @Override
    public View onCreateView(

            @NonNull LayoutInflater inflater,

            ViewGroup container,

            Bundle savedInstanceState

    ){


        View view =

                inflater.inflate(

                        R.layout.fragment_confession_detail,

                        container,

                        false

                );





        TextView title =

                view.findViewById(

                        R.id.confessionDetailTitle

                );





        TextView content =

                view.findViewById(

                        R.id.confessionDetailContent

                );

        this.content = content;

        Button editButton =
                view.findViewById(
                        R.id.editConfessionButton
                );




        if(getArguments() != null){



            String date =

                    getArguments()

                    .getString(
                            "date"
                    );



            String guide =

                    getArguments()

                    .getString(
                            "guide"
                    );

            this.guide = guide;

            String penance =

                    getArguments()

                    .getString(
                            "penance"
                    );

            this.penance = penance;

            String notes =

                    getArguments()

                    .getString(
                            "notes"
                    );

            this.notes = notes;

            recordPosition =
                    getArguments()
                    .getInt("position", -1);




            title.setText(

                    "Confession - " + date

            );





            StringBuilder builder =

                    new StringBuilder();




            builder.append(
                    guide
            );




            if(penance != null &&
                    !penance.isEmpty()){


                builder.append(
                        "\n\nPenance\n\n"
                );


                builder.append(
                        penance
                );


            }





            if(notes != null &&
                    !notes.isEmpty()){


                builder.append(
                        "\n\nNotes\n\n"
                );


                builder.append(
                        notes
                );


            }





            content.setText(

                    builder.toString()

            );


        }

        editButton.setOnClickListener(v -> {
            showEditDialog(guide, penance, notes);
        });




        return view;


    }

    private void showEditDialog(String currentGuide, String currentPenance, String currentNotes){
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        EditText guideInput = new EditText(requireContext());
        guideInput.setHint("Confession Guide");
        guideInput.setText(currentGuide);
        guideInput.setMinLines(5);
        LinearLayout.LayoutParams guideParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        guideInput.setLayoutParams(guideParams);

        EditText penanceInput = new EditText(requireContext());
        penanceInput.setHint("Penance (optional)");
        penanceInput.setText(currentPenance);
        penanceInput.setMinLines(2);
        LinearLayout.LayoutParams penanceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        penanceParams.setMargins(0, 20, 0, 0);
        penanceInput.setLayoutParams(penanceParams);

        EditText notesInput = new EditText(requireContext());
        notesInput.setHint("Notes (optional)");
        notesInput.setText(currentNotes);
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

            if(recordPosition >= 0){
                ConfessionStorage.updateRecord(
                    requireContext(),
                    recordPosition,
                    newGuide,
                    newPenance,
                    newNotes,
                    new ArrayList<>()
                );

                // Update local variables
                guide = newGuide;
                penance = newPenance;
                notes = newNotes;

                StringBuilder builder = new StringBuilder();
                builder.append(newGuide);

                if(newPenance != null && !newPenance.isEmpty()){
                    builder.append("\n\nPenance\n\n");
                    builder.append(newPenance);
                }

                if(newNotes != null && !newNotes.isEmpty()){
                    builder.append("\n\nNotes\n\n");
                    builder.append(newNotes);
                }

                content.setText(builder.toString());
            }
        })
        .show();
    }



}
