package com.openlycatholic.app.rosary;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.fragment.app.Fragment;
import androidx.documentfile.provider.DocumentFile;

import com.openlycatholic.app.R;


import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;



public class RosaryManager {



    private final View rootView;

    private Fragment fragment;

    private int currentStep = 0;

    private int selectedDayIndex = 0;



    private Spinner daySpinner;


    private ImageView mysteryImage;



    private TextView mysteryTitle;

    private TextView mysteryDescription;


    private TextView stepTitle;

    private TextView prayerText;

    private TextView prayerContent;

    private LinearLayout audioControlsLayout;

    private Button playPauseButton;

    private MediaPlayer mediaPlayer;

    private boolean isPlaying = false;

    private static final String PREFS_NAME = "AudioRosaryPrefs";
    private static final String KEY_AUDIO_ENABLED = "audio_enabled";
    private static final String KEY_FOLDER_URI = "folder_uri";






    public RosaryManager(View rootView, Fragment fragment){

        this.rootView = rootView;
        this.fragment = fragment;

    }







    public void initialize(){



        daySpinner =
                rootView.findViewById(
                        R.id.daySpinner
                );


        mysteryImage =
                rootView.findViewById(
                        R.id.mysteryImage
                );


        mysteryTitle =
                rootView.findViewById(
                        R.id.mysteryTitle
                );


        mysteryDescription =
                rootView.findViewById(
                        R.id.mysteryDescription
                );


        stepTitle =
                rootView.findViewById(
                        R.id.stepTitle
                );


        prayerText =
                rootView.findViewById(
                        R.id.prayerText
                );


        prayerContent =
                rootView.findViewById(
                        R.id.prayerContent
                );



        audioControlsLayout =
                rootView.findViewById(
                        R.id.audioControlsLayout
                );

        playPauseButton =
                rootView.findViewById(
                        R.id.playPauseButton
                );



        setupDaySpinner();

        setupButtons();

        setupAudioControls();



        updateDayMystery();

        updateStep();


    }








    private void setupDaySpinner(){



        String[] days = {

                "Sunday",
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday"

        };



        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        rootView.getContext(),
                        android.R.layout.simple_spinner_item,
                        days
                );



        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );



        daySpinner.setAdapter(
                adapter
        );




        Calendar calendar =
                Calendar.getInstance();



        selectedDayIndex =
                calendar.get(Calendar.DAY_OF_WEEK) - 1;



        daySpinner.setSelection(
                selectedDayIndex
        );





        daySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener(){



                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ){


                        selectedDayIndex =
                                position;


                        currentStep = 0;


                        updateDayMystery();

                        updateStep();


                    }





                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ){

                    }



                }
        );



    }








    private void setupButtons(){



        Button prevButton =
                rootView.findViewById(
                        R.id.prevButton
                );



        Button nextButton =
                rootView.findViewById(
                        R.id.nextButton
                );



        Button resetButton =
                rootView.findViewById(
                        R.id.resetButton
                );



        Button audioSettingsButton =
                rootView.findViewById(
                        R.id.audioSettingsButton
                );





        prevButton.setOnClickListener(v -> {


            if(currentStep > 0){


                currentStep--;


                updateStep();


            }


        });





        nextButton.setOnClickListener(v -> {


            if(currentStep < RosaryData.STEPS.length - 1){


                currentStep++;


                updateStep();


            }


        });






        resetButton.setOnClickListener(v -> {


            currentStep = 0;


            updateStep();


        });
        audioSettingsButton.setOnClickListener(v -> {
            fragment.getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.mainContainer,
                            new AudioSettingsFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });



    }
    private void setupAudioControls(){
        SharedPreferences prefs = rootView.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean audioEnabled = prefs.getBoolean(KEY_AUDIO_ENABLED, false);
        
        if(audioEnabled){
            audioControlsLayout.setVisibility(View.VISIBLE);
        } else {
            audioControlsLayout.setVisibility(View.GONE);
        }
        
        playPauseButton.setOnClickListener(v -> {
            if(isPlaying){
                pauseAudio();
            } else {
                playAudio();
            }
        });
    }
    
    private void playAudio(){
        SharedPreferences prefs = rootView.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String folderUriString = prefs.getString(KEY_FOLDER_URI, null);
        
        if(folderUriString == null){
            return;
        }
        
        try{
            Uri folderUri = Uri.parse(folderUriString);
            String audioFileName = getAudioFileNameForStep(currentStep);
            
            if(audioFileName != null){
                DocumentFile pickedDir = DocumentFile.fromTreeUri(rootView.getContext(), folderUri);
                
                if(pickedDir != null && pickedDir.isDirectory()) {
                    DocumentFile audioFile = pickedDir.findFile(audioFileName);
                    
                    if(audioFile != null && audioFile.exists()) {
                        Uri audioUri = audioFile.getUri();
                        
                        if(mediaPlayer == null){
                            mediaPlayer = new MediaPlayer();
                        }
                        
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(rootView.getContext(), audioUri);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        
                        isPlaying = true;
                        playPauseButton.setText("Pause");
                        
                        mediaPlayer.setOnCompletionListener(mp -> {
                            isPlaying = false;
                            playPauseButton.setText("Play");
                        });
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void pauseAudio(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
        isPlaying = false;
        playPauseButton.setText("Play");
    }
    
    private String getAudioFileNameForStep(int step){
        String[] audioFileNames = {
            "sign_of_cross.mp3",
            "apostles_creed.mp3",
            "our_father.mp3",
            "hail_mary.mp3",
            "glory_be.mp3",
            "mystery_1.mp3",
            "our_father.mp3",
            "hail_mary.mp3",
            "glory_be.mp3",
            "fatima.mp3",
            "mystery_2.mp3",
            "our_father.mp3",
            "hail_mary.mp3",
            "glory_be.mp3",
            "fatima.mp3",
            "mystery_3.mp3",
            "our_father.mp3",
            "hail_mary.mp3",
            "glory_be.mp3",
            "fatima.mp3",
            "mystery_4.mp3",
            "our_father.mp3",
            "hail_mary.mp3",
            "glory_be.mp3",
            "fatima.mp3",
            "mystery_5.mp3",
            "our_father.mp3",
            "hail_mary.mp3",
            "glory_be.mp3",
            "fatima.mp3",
            "hail_holy_queen.mp3"
        };
        
        if(step >= 0 && step < audioFileNames.length){
            return audioFileNames[step];
        }
        return null;
    }








    private void updateDayMystery(){



        mysteryTitle.setText(
                RosaryData.MYSTERY_DAYS[selectedDayIndex][0]
        );



        mysteryDescription.setText(
                RosaryData.MYSTERY_DAYS[selectedDayIndex][1]
        );



    }








    private void updateStep(){



        if(currentStep < 0 ||
                currentStep >= RosaryData.STEPS.length){

            currentStep = 0;

        }




        stepTitle.setText(
                "Step " + (currentStep + 1)
        );



        prayerText.setText(
                RosaryData.STEPS[currentStep]
        );



        String content =
                RosaryData.PRAYER_CONTENTS[currentStep];



        mysteryImage.setVisibility(
                View.GONE
        );



        if(content.isEmpty()){


            int mysteryIndex =
                    getMysteryIndexForStep(
                            currentStep
                    );



            String[][] mysteries =
                    getCurrentMysterySet();



            if(mysteryIndex >= 0 &&
                    mysteryIndex < mysteries.length){



                prayerText.setText(
                        mysteries[mysteryIndex][0]
                );



                prayerContent.setText(
                        mysteries[mysteryIndex][1]
                );



                setImageResource(
                        mysteries[mysteryIndex][2]
                );


            }



        }
        else {



            prayerContent.setText(
                    content
            );


        }



    }









    private int getMysteryIndexForStep(
            int step
    ){


        switch(step){


            case 5:
                return 0;


            case 10:
                return 1;


            case 15:
                return 2;


            case 20:
                return 3;


            case 25:
                return 4;



            default:

                return -1;


        }


    }









    private String[][] getCurrentMysterySet(){



        String type =
                RosaryData.MYSTERY_DAYS[selectedDayIndex][0];



        switch(type){



            case "Joyful Mysteries":

                return RosaryData.JOYFUL_MYSTERIES;




            case "Sorrowful Mysteries":

                return RosaryData.SORROWFUL_MYSTERIES;




            case "Glorious Mysteries":

                return RosaryData.GLORIOUS_MYSTERIES;




            case "Luminous Mysteries":

                return RosaryData.LUMINOUS_MYSTERIES;




            default:

                return RosaryData.JOYFUL_MYSTERIES;


        }


    }









    private void setImageResource(
            String imageName
    ){



        mysteryImage.setVisibility(
                View.VISIBLE
        );



        int resourceId =
                rootView.getResources()
                        .getIdentifier(
                                imageName,
                                "drawable",
                                rootView.getContext()
                                        .getPackageName()
                        );




        if(resourceId != 0){


            mysteryImage.setImageResource(
                    resourceId
            );


        }
        else{


            mysteryImage.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );


        }



    }



}
