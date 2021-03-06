package com.berthold.covidinfo.ui.home;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javaUtils.FormatTimeStamp;

/**
 * A collection of methods to decode the search result from
 * https://public.opendatasoft.com Covid 19 data for germany, landkreise.
 */

public class DecodeJsonResult {

    /**
     * Gets all records from a json string and decodes them into {@link CovidSearchResultData}
     * objects and adds them to a list.
     *
     * @param jsonString
     * @return List of {@link CovidSearchResultData} objects containing the field's of each record from the specified json string.
     */
    public static List<CovidSearchResultData> getResult(String jsonString){
        int numberOfResults=getHits(jsonString);
        List <CovidSearchResultData> result=new ArrayList<>();
        JSONObject json;

        for (int n=0;n<=numberOfResults-1;n++){
            //String record=getRecordNr(jsonString,n);
            //Log.v("JSONJSON",record);

            try {
                json = new JSONObject(getRecordNr(jsonString,n));
                //String recordID=json.getString("datasetid");
                String bl = json.getString("bl");
                String name=json.getString("name");
                String bez=json.getString("bez");
                int casesPer100K=json.getInt("cases7_per_100k");

                String lastUpdate=json.getString("last_update");
                String lastUpdateFormated=FormatTimeStamp.german(lastUpdate,false);

                CovidSearchResultData d=new CovidSearchResultData("-",bl,name,bez,casesPer100K,lastUpdateFormated);

                //Log.v("RECORDID",recordID+"--");

                result.add(d);

            }catch(JSONException e){
                Log.v("JSONJSON",e.toString());
            }
        }
       return result;
    }

    /**
     * Number of results (records) matching the search query.
     *
     * @param jsonString
     * @return Results found for the associated query.
     */
    public static int getHits(String jsonString) {

       int hits;

        try {
            JSONObject json = new JSONObject(jsonString);
            hits = json.getInt("nhits");
        } catch (JSONException e) {
            Log.v("JSONJSON", e + "");
            hits=0;
        }
        return hits;
    }

    /**
     * Gets all records from the result.
     *
     * @param jsonString
     * @return Records matching the query.
     */
    public static String getRecords(String jsonString){
        String records;

        try {
            JSONObject json = new JSONObject(jsonString);
            records = json.getJSONArray("records").toString();

        } catch (JSONException e) {
            Log.v("JSONJSON", e + "");
            records="-";
        }
        return records;
    }

    /**
     * Gets a specified record number from the result of a query.
     *
     * @param jsonString Result.
     * @param recordNr  The record number were first record=0
     * @return The specified record or '-' if this record does not exist.
     */
    public static String getRecordNr(String jsonString,int recordNr){
        String record,bl;

        try {
            JSONObject json = new JSONObject(jsonString);
            record = json.getJSONArray("records").getJSONObject(recordNr).getString("fields");

            json=new JSONObject(record);
            bl=json.getString("bl");
            Log.v("BLBL",bl);

        } catch (JSONException e) {
            Log.v("JSONJSON", e + "");
            record="-";
        }
        return record;
    }
}
