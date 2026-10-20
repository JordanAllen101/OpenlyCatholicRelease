package com.openlycatholic.app.examination;

import java.io.Serializable;
import java.util.ArrayList;

public class ConfessionRecord implements Serializable {

    public String date;

    public ArrayList<String> sins;

    public String confessionGuide;

    public String actOfContrition;

    public String penance;

    public String notes;

    public ConfessionRecord() {

        sins = new ArrayList<>();

    }

    public ConfessionRecord(
            String date,
            ArrayList<String> sins,
            String confessionGuide,
            String actOfContrition,
            String penance,
            String notes
    ) {

        this.date = date;

        this.sins = sins;

        this.confessionGuide = confessionGuide;

        this.actOfContrition = actOfContrition;

        this.penance = penance;

        this.notes = notes;

    }

}
