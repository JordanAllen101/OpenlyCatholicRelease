package com.openlycatholic.app.missal;


import android.content.Context;


import java.io.InputStream;



public class MissalLoader {





    public static String load(
            Context context,
            String filename
    ){


        try{


            InputStream input =
                    context.getAssets()
                            .open(filename);



            int size =
                    input.available();



            byte[] buffer =
                    new byte[size];



            input.read(buffer);


            input.close();



            return new String(
                    buffer,
                    "UTF-8"
            );



        }
        catch(Exception e){


            e.printStackTrace();


            return null;


        }



    }



}
