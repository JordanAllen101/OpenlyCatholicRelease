package com.openlycatholic.app.examination;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;


import java.util.ArrayList;



public class ConfessionHistoryFragment extends Fragment {


    private ArrayList<ConfessionRecord> records;

    private ArrayAdapter<String> adapter;

    private ArrayList<String> display;



    public ConfessionHistoryFragment(){

    }





    @Override
    public void onResume(){
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(

            @NonNull LayoutInflater inflater,

            ViewGroup container,

            Bundle savedInstanceState

    ){


        View view = inflater.inflate(

                R.layout.fragment_confession_history,

                container,

                false

        );

        loadHistory();





        ListView listView =

                view.findViewById(

                        R.id.confessionHistoryList

                );





        Button clearButton =

                view.findViewById(

                        R.id.clearConfessionHistoryButton

                );






        adapter = new ArrayAdapter<>(

                requireContext(),

                android.R.layout.simple_list_item_1,

                display

        );





        listView.setAdapter(adapter);






        listView.setOnItemClickListener(

                (parent, itemView, position, id) -> {



                    ConfessionGuideFragment guide =

                            ConfessionGuideFragment.newInstance(

                                    records.get(position),

                                    position

                            );



                    getParentFragmentManager()

                            .beginTransaction()

                            .replace(

                                    R.id.mainContainer,

                                    guide

                            )

                            .addToBackStack(null)

                            .commit();



                }

        );







        listView.setOnItemLongClickListener(

                (parent, itemView, position, id) -> {


                    showDeleteDialog(position);


                    return true;


                }

        );






        clearButton.setOnClickListener(v -> {


            showClearAllDialog();


        });




        return view;


    }





    private void loadHistory(){


        records =

                ConfessionStorage.getRecords(

                        requireContext()

                );




        display =

                new ArrayList<>();






        for(ConfessionRecord record : records){



            int count = 0;



            if(record.sins != null){

                count = record.sins.size();

            }




            display.add(

                    record.date +

                    "\n" +

                    count +

                    " sins selected"

            );


        }


    }





    private void showDeleteDialog(
            int position
    ){



        new AlertDialog.Builder(
                requireContext()
        )

        .setTitle(
                "Delete Confession?"
        )

        .setMessage(
                "This will permanently delete this confession record."
        )

        .setNegativeButton(
                "Cancel",
                null
        )

        .setPositiveButton(
                "Delete",
                (dialog, which) -> {



                    ConfessionStorage.deleteRecord(

                            requireContext(),

                            position

                    );




                    refresh();




                    Toast.makeText(

                            requireContext(),

                            "Confession deleted",

                            Toast.LENGTH_SHORT

                    ).show();



                }

        )

        .show();



    }





    private void showClearAllDialog(){



        new AlertDialog.Builder(
                requireContext()
        )

        .setTitle(
                "Clear All Confessions?"
        )

        .setMessage(
                "This will permanently delete all saved confession records."
        )

        .setNegativeButton(
                "Cancel",
                null
        )

        .setPositiveButton(
                "Delete All",
                (dialog, which) -> {



                    ConfessionStorage.clearAll(

                            requireContext()

                    );




                    refresh();




                    Toast.makeText(

                            requireContext(),

                            "All confession records deleted",

                            Toast.LENGTH_SHORT

                    ).show();



                }

        )

        .show();



    }





    private void refresh(){


        loadHistory();


        adapter.clear();


        adapter.addAll(
                display
        );


        adapter.notifyDataSetChanged();



    }



}
