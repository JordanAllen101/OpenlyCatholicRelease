package com.openlycatholic.app.examination;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;



public class QuickExaminationFragment extends Fragment {



    private QuickExaminationManager manager;



    public QuickExaminationFragment() {

    }





    @Nullable
    @Override
    public View onCreateView(

            @NonNull LayoutInflater inflater,

            ViewGroup container,

            Bundle savedInstanceState

    ) {



        View view = inflater.inflate(

                R.layout.fragment_quick_examination,

                container,

                false

        );





        manager = new QuickExaminationManager(

                this,

                view

        );



        manager.initialize();





        return view;

    }



}
