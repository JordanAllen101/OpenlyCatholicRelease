package com.openlycatholic.app.examination;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;



public class ExaminationFragment extends Fragment {



    public ExaminationFragment(){

    }





    @Nullable
    @Override
    public View onCreateView(

            @NonNull LayoutInflater inflater,

            ViewGroup container,

            Bundle savedInstanceState

    ) {


        View view =

                inflater.inflate(

                        R.layout.fragment_examination,

                        container,

                        false

                );





        CardView fullCard =

                view.findViewById(

                        R.id.fullExaminationCard

                );





        CardView quickCard =

                view.findViewById(

                        R.id.quickExaminationCard

                );





        CardView historyCard =

                view.findViewById(

                        R.id.confessionHistoryCard

                );






        fullCard.setOnClickListener(v -> {


            getParentFragmentManager()

                    .beginTransaction()

                    .replace(

                            R.id.mainContainer,

                            new FullExaminationFragment()

                    )

                    .addToBackStack(null)

                    .commit();


        });






        quickCard.setOnClickListener(v -> {


            getParentFragmentManager()

                    .beginTransaction()

                    .replace(

                            R.id.mainContainer,

                            new QuickExaminationFragment()

                    )

                    .addToBackStack(null)

                    .commit();


        });






        historyCard.setOnClickListener(v -> {


            getParentFragmentManager()

                    .beginTransaction()

                    .replace(

                            R.id.mainContainer,

                            new ConfessionHistoryFragment()

                    )

                    .addToBackStack(null)

                    .commit();
        });




        return view;


    }


}
