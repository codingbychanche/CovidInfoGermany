package com.berthold.covidinfo.ui.home;

import android.content.res.Resources;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.berthold.covidinfo.R;

public class CovidDataCasesColorCoder {

    /**
     * Determines the color codes resource id based on
     * the cases per 100K.
     *
     * @param casesPer1K
     * @return Associated color codes resource id.
     */
    public static int getColor(int casesPer1K){
        if (casesPer1K <= 50)
           return R.color.green;
        if (casesPer1K > 50 && casesPer1K <= 100)
            return R.color.yellow;
        if (casesPer1K > 100)
            return R.color.red;

        return Color.BLACK;
    }
}

