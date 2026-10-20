package com.openlycatholic.app.missal;



import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;



public class LiturgicalCalendar {






    public static String getMonthDayKey(){


        Calendar calendar =
                Calendar.getInstance();



        SimpleDateFormat format =
                new SimpleDateFormat(
                        "MMMM d",
                        Locale.US
                );



        return format.format(
                calendar.getTime()
        );


    }







    public static boolean isSunday(){


        Calendar calendar =
                Calendar.getInstance();



        return calendar.get(
                Calendar.DAY_OF_WEEK
        )
        ==
        Calendar.SUNDAY;


    }








    public static int getLiturgicalYear(){


        Calendar today =
                Calendar.getInstance();



        int year =
                today.get(
                        Calendar.YEAR
                );



        Calendar advent =
                getFirstSundayOfAdvent(
                        year
                );



        if(today.before(advent)){


            return year;


        }



        return year + 1;


    }








    private static Calendar getFirstSundayOfAdvent(
            int year
    ){


        Calendar advent =
                Calendar.getInstance();



        advent.clear();



        advent.set(
                year,
                Calendar.DECEMBER,
                3
        );



        while(
                advent.get(Calendar.DAY_OF_WEEK)
                !=
                Calendar.SUNDAY
        ){


            advent.add(
                    Calendar.DAY_OF_MONTH,
                    -1
            );


        }



        return advent;


    }









    public static String getSundayCycle(){



        int year =
                getLiturgicalYear();



        int cycle =
                (year - 2023) % 3;



        if(cycle < 0){

            cycle += 3;

        }



        switch(cycle){


            case 0:

                return "A";


            case 1:

                return "B";


            default:

                return "C";


        }


    }








    public static String getWeekdayCycle(){



        int year =
                getLiturgicalYear();



        if(year % 2 == 0){


            return "I";


        }



        return "II";


    }



}
