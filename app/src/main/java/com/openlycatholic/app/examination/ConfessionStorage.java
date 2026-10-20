package com.openlycatholic.app.examination;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;



public class ConfessionStorage {


    private static final String PREFS =
            "confession_history";


    private static final String KEY =
            "records";





    public static void saveRecord(
            Context context,
            ConfessionRecord record
    ) {


        ArrayList<ConfessionRecord> records =
                getRecords(context);



        records.add(
                0,
                record
        );



        saveRecords(
                context,
                records
        );


    }







    public static ArrayList<ConfessionRecord> getRecords(
            Context context
    ) {


        String json =

                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                )
                .getString(
                        KEY,
                        null
                );



        if(json == null){

            return new ArrayList<>();

        }




        Type type =

                new TypeToken<ArrayList<ConfessionRecord>>() {}

                .getType();




        ArrayList<ConfessionRecord> records =

                new Gson().fromJson(
                        json,
                        type
                );



        if(records == null){

            records = new ArrayList<>();

        }



        return records;


    }








    public static void deleteRecord(
            Context context,
            int position
    ){


        ArrayList<ConfessionRecord> records =

                getRecords(context);



        if(position >= 0 &&
                position < records.size()){


            records.remove(position);


        }




        saveRecords(
                context,
                records
        );


    }
    public static void updateRecord(
            Context context,
            int position,
            String newGuide,
            String newPenance,
            String newNotes,
            ArrayList<String> newSins
    ){
        ArrayList<ConfessionRecord> records = getRecords(context);

        if(position >= 0 && position < records.size()){
            ConfessionRecord record = records.get(position);
            record.confessionGuide = newGuide;
            record.penance = newPenance;
            record.notes = newNotes;
            record.sins = newSins;

            saveRecords(context, records);
        }
    }








    public static void clearAll(
            Context context
    ){


        context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        )
        .edit()
        .remove(KEY)
        .apply();


    }








    private static void saveRecords(
            Context context,
            ArrayList<ConfessionRecord> records
    ){


        String json =

                new Gson().toJson(
                        records
                );



        context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        )
        .edit()
        .putString(
                KEY,
                json
        )
        .apply();


    }



}
