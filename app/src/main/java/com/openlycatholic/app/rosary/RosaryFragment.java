package com.openlycatholic.app.rosary;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;



public class RosaryFragment extends Fragment {



    private RosaryManager rosaryManager;




    public RosaryFragment(){

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
                        R.layout.fragment_rosary,
                        container,
                        false
                );




        rosaryManager =
                new RosaryManager(
                        view,
                        this
                );



        rosaryManager.initialize();



        return view;


    }



}
