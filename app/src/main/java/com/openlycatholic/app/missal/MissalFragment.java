package com.openlycatholic.app.missal;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.openlycatholic.app.R;



public class MissalFragment extends Fragment {



    private MissalManager missalManager;




    public MissalFragment(){

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
                        R.layout.fragment_missal,
                        container,
                        false
                );



        missalManager =
                new MissalManager(
                        view
                );



        missalManager.initialize();



        return view;


    }

    @Override
    public void onDestroyView() {
        if (missalManager != null) {
            missalManager.destroy();
            missalManager = null;
        }
        super.onDestroyView();
    }



}
