package com.openlycatholic.app.missal;


import android.text.NoCopySpan;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


import com.openlycatholic.app.R;


import org.json.JSONObject;

import java.io.IOException;



public class MissalManager {

    private static final String TAG = "MissalManager";

    private final View rootView;
    private volatile boolean active = true;
    private WebView readingsWebView;
    private int httpErrorCode;


    private JSONObject missal;



    private TextView dailyReadingsTitle;


    private TextView firstReadingTitle;
    private TextView firstReadingContent;


    private TextView psalmTitle;
    private TextView psalmContent;


    private TextView secondReadingTitle;
    private TextView secondReadingContent;


    private TextView alleluiaTitle;
    private TextView alleluiaContent;


    private TextView gospelTitle;
    private TextView gospelContent;





    public MissalManager(View rootView){

        this.rootView = rootView;

    }







    public void initialize(){


        dailyReadingsTitle =
                rootView.findViewById(
                        R.id.dailyReadingsTitle
                );


        firstReadingTitle =
                rootView.findViewById(
                        R.id.firstReadingTitle
                );


        firstReadingContent =
                rootView.findViewById(
                        R.id.firstReadingContent
                );


        psalmTitle =
                rootView.findViewById(
                        R.id.psalmTitle
                );


        psalmContent =
                rootView.findViewById(
                        R.id.psalmContent
                );


        secondReadingTitle =
                rootView.findViewById(
                        R.id.secondReadingTitle
                );


        secondReadingContent =
                rootView.findViewById(
                        R.id.secondReadingContent
                );


        alleluiaTitle =
                rootView.findViewById(
                        R.id.alleluiaTitle
                );


        alleluiaContent =
                rootView.findViewById(
                        R.id.alleluiaContent
                );


        gospelTitle =
                rootView.findViewById(
                        R.id.gospelTitle
                );


        gospelContent =
                rootView.findViewById(
                        R.id.gospelContent
                );



        loadMissal();

    }






    private void loadMissal(){


        loadLiveReadings();


        String json =
                MissalLoader.load(
                        rootView.getContext(),
                        "missal.json"
                );



        try {


            if(json == null){


                dailyReadingsTitle.setText(
                        "Could not load missal.json"
                );


                return;

            }



            missal =
                    new JSONObject(json);



            loadTodaysReadings();



        }
        catch(Exception e){


            dailyReadingsTitle.setText(
                    "Missal loading error"
            );


        }


    }







    private void loadTodaysReadings(){


        try {


            JSONObject data =
                    missal.getJSONObject(
                            "data"
                    );


            String key =
                    LiturgicalCalendar.getMonthDayKey();



            if(!data.has(key)){


                dailyReadingsTitle.setText(
                        "No readings found for " + key
                );


                return;


            }



            JSONObject day =
                    data.getJSONObject(
                            key
                    );



            JSONObject mass;



            if(LiturgicalCalendar.isSunday()){


                JSONObject sunday =
                        day.getJSONObject(
                                "Sunday"
                        );


                JSONObject cycle =
                        sunday.getJSONObject(
                                LiturgicalCalendar.getSundayCycle()
                        );



                mass =
                        cycle.getJSONObject(
                                "readings"
                        )
                        .getJSONObject(
                                "Mass"
                        );



                dailyReadingsTitle.setText(
                        cycle.optString(
                                "title",
                                "Sunday Mass"
                        )
                );


            }
            else{


                JSONObject weekday =
                        day.getJSONObject(
                                "Weekday"
                        );


                JSONObject cycle =
                        weekday.getJSONObject(
                                LiturgicalCalendar.getWeekdayCycle()
                        );



                mass =
                        cycle.getJSONObject(
                                "readings"
                        )
                        .getJSONObject(
                                "Mass"
                        );



                dailyReadingsTitle.setText(
                        cycle.optString(
                                "title",
                                "Weekday Mass"
                        )
                );


            }



            displayReadings(
                    mass
            );


        }
        catch(Exception e){


            dailyReadingsTitle.setText(
                    "Error loading readings"
            );


        }


    }
    private void displayReadings(
            JSONObject mass
    )
    throws Exception {


        displayReading(
                mass,
                "first_reading",
                firstReadingTitle,
                firstReadingContent,
                "First Reading"
        );


        displayReading(
                mass,
                "psalm",
                psalmTitle,
                psalmContent,
                "Responsorial Psalm",
                true
        );


        displayReading(
                mass,
                "second_reading",
                secondReadingTitle,
                secondReadingContent,
                "Second Reading"
        );


        displayReading(
                mass,
                "alleluia",
                alleluiaTitle,
                alleluiaContent,
                "Alleluia",
                true
        );


        displayReading(
                mass,
                "gospel",
                gospelTitle,
                gospelContent,
                "Gospel"
        );


    }

    private void loadLiveReadings() {
        readingsWebView = rootView.findViewById(R.id.usccbReadingsWebView);
        WebSettings settings = readingsWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        readingsWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceResponse errorResponse
            ) {
                if (request.isForMainFrame()) {
                    httpErrorCode = errorResponse.getStatusCode();
                }
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {
                if (request.isForMainFrame()) {
                    showSavedReadingsStatus("network error");
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!active) {
                    return;
                }
                if (httpErrorCode >= 400) {
                    showSavedReadingsStatus("HTTP " + httpErrorCode);
                    return;
                }
                view.evaluateJavascript(
                        "(function(){"
                                + "var sections=Array.from(document.querySelectorAll('main .innerblock'))"
                                + ".map(function(section){"
                                + "var heading=section.querySelector('.content-header h3.name');"
                                + "var address=section.querySelector('.content-header .address a');"
                                + "var paragraphs=Array.from(section.querySelectorAll('.content-body p'));"
                                + "return {heading:heading?heading.innerText.trim():'',"
                                + "reference:address?address.innerText.trim():'',"
                                + "text:paragraphs.map(function(p){return p.innerText.trim()})"
                                + ".filter(Boolean).join('\\n\\n')};});"
                                + "var title=document.querySelector('main h2');"
                                + "return JSON.stringify({title:title?title.innerText.trim():'Daily Readings',"
                                + "sections:sections});"
                                + "})()",
                        value -> showLiveReadings(value)
                );
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                String host = request.getUrl().getHost();
                return host == null
                        || !(host.equals("bible.usccb.org") || host.endsWith(".usccb.org"));
            }
        });
        readingsWebView.loadUrl("https://bible.usccb.org/daily-bible-reading");
    }

    private void showLiveReadings(String response) {
        if (!active) {
            return;
        }
        try {
            UsccbReadingsLoader.Readings readings =
                    UsccbReadingsLoader.parse(response);
            dailyReadingsTitle.setText(readings.title);
            displayReadings(readings.mass);
        } catch (IOException | org.json.JSONException | IllegalArgumentException exception) {
            Log.w(TAG, "Unable to parse the USCCB page; keeping the saved readings.", exception);
            showSavedReadingsStatus(
                    httpErrorCode >= 400
                            ? "HTTP " + httpErrorCode
                            : getErrorMessage(exception)
            );
        } catch (Exception exception) {
            Log.e(TAG, "Unable to display the USCCB readings.", exception);
            showSavedReadingsStatus("display error");
        }
    }

    private String getErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return exception.getClass().getSimpleName();
        }
        return message.replace('\n', ' ').trim();
    }

    private void showSavedReadingsStatus(String reason) {
        if (active) {
            dailyReadingsTitle.setText(
                    dailyReadingsTitle.getText()
                            + "\nUSCCB unavailable ("
                            + reason
                            + "); showing saved readings."
            );
        }
    }

    public void destroy() {
        active = false;
        if (readingsWebView != null) {
            readingsWebView.stopLoading();
            readingsWebView.setWebViewClient(new WebViewClient());
            readingsWebView.destroy();
            readingsWebView = null;
        }
    }








    private void displayReading(
            JSONObject mass,
            String key,
            TextView title,
            TextView content,
            String label
    )
    throws Exception {


        displayReading(
                mass,
                key,
                title,
                content,
                label,
                false
        );


    }






    private void displayReading(
            JSONObject mass,
            String key,
            TextView title,
            TextView content,
            String label,
            boolean isPsalm
    )
    throws Exception {


        if(mass.has(key)){


            JSONObject reading =
                    mass.getJSONObject(
                            key
                    );


            title.setVisibility(
                    View.VISIBLE
            );


            content.setVisibility(
                    View.VISIBLE
            );


            title.setText(
                    label
            );



            String reference =
                    reading.optString(
                            "reference",
                            ""
                    );


            String text =
                    reading.optString(
                            "text",
                            ""
                    );



            SpannableString formattedText;



            if(isPsalm){


                formattedText =
                        formatPsalmText(
                                reference,
                                text
                        );


            }
            else{


                formattedText =
                        formatReadingText(
                                reference,
                                text
                        );


            }



            content.setText(
                    formattedText
            );


        }
        else{


            title.setVisibility(
                    View.GONE
            );


            content.setVisibility(
                    View.GONE
            );


        }


    }








    private SpannableString formatPsalmText(
            String reference,
            String text
    ){


        String formattedBody =
                formatPsalmBody(
                        text
                );



        String fullText =
                reference
                + "\n\n"
                + formattedBody;



        SpannableString spannable =
                new SpannableString(
                        fullText
                );



        if(!reference.isEmpty()){


            spannable.setSpan(
                    new StyleSpan(
                            android.graphics.Typeface.ITALIC
                    ),
                    0,
                    reference.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );


            spannable.setSpan(
                    new ForegroundColorSpan(
                            rootView.getContext()
                                    .getResources()
                                    .getColor(
                                            R.color.primary
                                    )
                    ),
                    0,
                    reference.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );


        }



        highlightPsalmResponses(
                spannable,
                formattedBody,
                reference.length() + 2
        );



        return spannable;


    }








    private SpannableString formatReadingText(
            String reference,
            String text
    ){


        String formattedBody =
                formatBodyText(
                        text
                );



        String fullText =
                reference
                + "\n\n"
                + formattedBody;



        SpannableString spannable =
                new SpannableString(
                        fullText
                );



        if(!reference.isEmpty()){


            spannable.setSpan(
                    new StyleSpan(
                            android.graphics.Typeface.ITALIC
                    ),
                    0,
                    reference.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );


            spannable.setSpan(
                    new ForegroundColorSpan(
                            rootView.getContext()
                                    .getResources()
                                    .getColor(
                                            R.color.primary
                                    )
                    ),
                    0,
                    reference.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );


        }



        return spannable;


    }
    private String formatPsalmBody(
            String text
    ){


        String formatted =
                text.trim();



        formatted =
                formatted.replace(
                        "R.(",
                        "R. ("
                );



        StringBuilder result =
                new StringBuilder();



        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile(
                        "(R\\.\\s*(?:\\([^)]*\\)\\s*)?[^.]+\\.)"
                );



        java.util.regex.Matcher matcher =
                pattern.matcher(
                        formatted
                );



        int lastEnd = 0;

        boolean firstResponse = true;



        while(matcher.find()){


            /*
             * Add the psalm verses before this response.
             * This is what keeps the actual psalm text.
             */
            if(matcher.start() > lastEnd){


                String verse =
                        formatted.substring(
                                lastEnd,
                                matcher.start()
                        )
                        .trim();



                if(!verse.isEmpty()){


                    verse =
                            formatPsalmVerseLines(
                                    verse
                            );


                    result.append(
                            verse
                    );


                    result.append(
                            "\n\n"
                    );


                }


            }



            if(!firstResponse){

                result.append(
                        "\n\n"
                );

            }



            result.append(
                    matcher.group()
                            .trim()
            );



            result.append(
                    "\n\n"
            );



            lastEnd =
                    matcher.end();



            firstResponse = false;


        }





        /*
         * Add anything after the final response.
         */
        if(lastEnd < formatted.length()){


            String verse =
                    formatted.substring(
                            lastEnd
                    )
                    .trim();



            if(!verse.isEmpty()){


                verse =
                        formatPsalmVerseLines(
                                verse
                        );


                result.append(
                        verse
                );


            }


        }



        return result.toString()
                .trim();


    }









    private String formatPsalmVerseLines(
            String verse
    ){


        String formatted =
                verse;



        formatted =
                formatted.replace(
                        ". ",
                        ".\n"
                );



        return formatted;


    }








    private String formatBodyText(
            String text
    ){


        String formatted =
                text;



        formatted =
                formatted.replace(
                        ". ",
                        ".\n\n"
                );


        formatted =
                formatted.replace(
                        "? ",
                        "?\n\n"
                );


        formatted =
                formatted.replace(
                        "! ",
                        "!\n\n"
                );



        return formatted;


    }
    private void highlightPsalmResponses(
            SpannableString spannable,
            String text,
            int offset
    ){


        String[] lines =
                text.split(
                        "\n"
                );



        int currentPos =
                offset;



        for(String line : lines){


            if(line.trim().startsWith("R.")){


                int start =
                        currentPos;



                int end =
                        currentPos
                        + line.length();



                spannable.setSpan(
                        new ForegroundColorSpan(
                                rootView.getContext()
                                        .getResources()
                                        .getColor(
                                                R.color.primary
                                        )
                        ),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );



                spannable.setSpan(
                        new StyleSpan(
                                android.graphics.Typeface.BOLD
                        ),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );


            }



            currentPos +=
                    line.length()
                    + 1;


        }


    }








    private static class NoBreakSpan implements NoCopySpan {


        @Override
        public String toString() {


            return "NoBreakSpan";


        }


    }


}
