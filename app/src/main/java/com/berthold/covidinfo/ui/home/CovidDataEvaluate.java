package com.berthold.covidinfo.ui.home;

import android.util.Log;

import com.berthold.covidinfo.CovidDataBase;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import berthold.trendAnalyzer.DataToAnalyze;

public class CovidDataEvaluate {

    private static int NUMBER_OF_DAYS_TO_ANALYZE=30;

    /**
     * Builds a string containing a short description of of the
     * latest development on the basis of the available data.
     *
     * @param name
     * @param bundesland
     * @param bez
     * @param covidDataBase
     * @return
     */
    public static String getTrend(String name, String bundesland, String bez, Connection covidDataBase) {

        List<CovidSearchResultData> covidDataSet = new ArrayList<>();
        covidDataSet = CovidDataBase.getEntry(name, bundesland, bez, covidDataBase);
        StringBuilder result = new StringBuilder();
        DataToAnalyze covidData=new DataToAnalyze(NUMBER_OF_DAYS_TO_ANALYZE);

        // Get data and init statistics tool.
        for (CovidSearchResultData d : covidDataSet)
            covidData.add(d.getCasesPer10K());

        int numberOfDaysAdded=covidData.getDataset().size();

        // Build description.....
        if (numberOfDaysAdded>1) {

            // Total average value...
            double avrTotal = covidData.getAverage();
            String avrTotalFormated = String.format("%.2f", avrTotal);

            // Days stored...
            String daysAnalyzed = numberOfDaysAdded + " Tage erfasst. <b>&empty;" + avrTotalFormated + "</b>. Tendenz ";
            result.append(daysAnalyzed);

            // What's the trend?
            double trend = covidData.getAverageSlope();
            String trendDescription = "(zu wenig Daten)";

            if (trend > 0)
                trendDescription = "&uarr; ";
            if (trend == 0)
                trendDescription = "&rarr; ";
            if (trend < 0)
                trendDescription = "&darr; ";

            result.append(trendDescription);

            // Average sum of changes...
            double avrSumOfChanges = covidData.getAverageSlope();
            String avrSumOfChangesFormated = String.format("%.2f", avrSumOfChanges);

            String avrDesc;
            if (trend>0 || trend <0)
                avrDesc = "mit &empty;" + avrSumOfChangesFormated + "/Tag. ";
            else
                avrDesc="";

            result.append(avrDesc);

            // Last value and date is was saved...
            double lastValue=covidDataSet.get(covidDataSet.size()-2).getCasesPer10K();
            String lastDate=covidDataSet.get(covidDataSet.size()-2).getLastUpdate();
            result.append("<b>Zuletzt erfasst: "+lastDate+" &rArr;"+lastValue+"</b>");

            return result.toString();
        }
        return "Zu wenig Daten erfasst, keine Zusammenfassung möglich.";
    }
}
