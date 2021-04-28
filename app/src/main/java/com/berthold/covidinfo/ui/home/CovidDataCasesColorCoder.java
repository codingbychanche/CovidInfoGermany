package com.berthold.covidinfo.ui.home;

import android.graphics.Color;

public class CovidDataCasesColorCoder {

    public static int getColor(int casesPer1K){
        if (casesPer1K < 50)
           return Color.GREEN;
        if (casesPer1K > 50 && casesPer1K <= 100)
            return Color.YELLOW;
        if (casesPer1K > 100)
            return Color.RED;

        return Color.BLACK;
    }
}

